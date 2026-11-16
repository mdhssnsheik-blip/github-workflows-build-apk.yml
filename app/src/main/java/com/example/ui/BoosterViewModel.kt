package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BoostPassMetrics
import com.example.data.BoostProfile
import com.example.data.BoosterPreferences
import com.example.data.BoosterSettings
import com.example.data.DeviceTelemetry
import com.example.data.FreeFireVariant
import com.example.data.LaunchOutcome
import com.example.data.SystemDiagnosticsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import android.net.Uri
import com.example.data.SelfApkInfo

enum class CockpitTab {
    BOOST,
    SYSTEM,
    SETTINGS
}

sealed interface BoostPhase {
    data object Idle : BoostPhase

    data class Running(
        val elapsedMs: Long,
        val totalMs: Long = 5000L,
        val stageTitle: String,
        val logLines: List<String>
    ) : BoostPhase {
        val progress: Float get() = (elapsedMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        val remainingSeconds: Float get() = ((totalMs - elapsedMs).coerceAtLeast(0L) / 1000f)
    }

    data class Completed(
        val metrics: BoostPassMetrics,
        val launchOutcome: LaunchOutcome,
        val profileUsed: BoostProfile
    ) : BoostPhase
}

sealed interface ApkExportStatus {
    data object Idle : ApkExportStatus
    data object Working : ApkExportStatus
    data class Success(val message: String) : ApkExportStatus
    data class Error(val message: String) : ApkExportStatus
}

data class BoosterUiState(
    val activeTab: CockpitTab = CockpitTab.BOOST,
    val settings: BoosterSettings = BoosterSettings(),
    val telemetry: DeviceTelemetry? = null,
    val isRefreshingTelemetry: Boolean = false,
    val boostPhase: BoostPhase = BoostPhase.Idle,
    val selfApkInfo: SelfApkInfo? = null,
    val apkExportStatus: ApkExportStatus = ApkExportStatus.Idle
)

class BoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = BoosterPreferences(application.applicationContext)
    private val repository = SystemDiagnosticsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(BoosterUiState())
    val uiState: StateFlow<BoosterUiState> = _uiState.asStateFlow()

    private var boostJob: Job? = null

    init {
        viewModelScope.launch {
            preferences.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        _uiState.update { it.copy(selfApkInfo = repository.getSelfApkInfo()) }
        refreshTelemetry(measurePing = true)
    }

    fun selectTab(tab: CockpitTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun refreshTelemetry(measurePing: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingTelemetry = true) }
            val telemetry = repository.collectTelemetry(measurePing = measurePing)
            _uiState.update {
                it.copy(telemetry = telemetry, isRefreshingTelemetry = false)
            }
        }
    }

    fun setProfile(profile: BoostProfile) {
        viewModelScope.launch { preferences.setProfile(profile) }
    }

    fun setPreferredVariant(variant: FreeFireVariant) {
        viewModelScope.launch { preferences.setPreferredVariant(variant) }
    }

    fun setAutoLaunch(enabled: Boolean) {
        viewModelScope.launch { preferences.setAutoLaunch(enabled) }
    }

    fun setTrimCache(enabled: Boolean) {
        viewModelScope.launch { preferences.setTrimCache(enabled) }
    }

    fun startFiveSecondBoost() {
        if (_uiState.value.boostPhase is BoostPhase.Running) return
        boostJob?.cancel()

        val currentSettings = _uiState.value.settings
        val targetGame = repository.resolveTargetGame(currentSettings.preferredVariant)
        val targetLabel = targetGame?.packageName ?: "com.dts.freefireth"

        boostJob = viewModelScope.launch {
            val totalDurationMs = 5000L
            val startTime = System.currentTimeMillis()

            // Run real optimization & network probe concurrently inside the 5-second window
            val passDeferred = async {
                repository.performOptimizationPass(trimCache = currentSettings.trimCacheOnBoost)
            }

            val baseLogs = mutableListOf(
                "[0.0s] INIT >> Locking FF Turbo ${currentSettings.profile.title.uppercase()} pipeline",
                "[0.4s] SCAN >> Target binary: $targetLabel"
            )

            var addedMemLog = false
            var addedNetLog = false
            var addedGpuLog = false
            var addedFinalLog = false
            var cachedMetrics: BoostPassMetrics? = null

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= totalDurationMs) break

                if (cachedMetrics == null && passDeferred.isCompleted) {
                    cachedMetrics = passDeferred.await()
                }

                if (elapsed >= 1100L && !addedMemLog) {
                    addedMemLog = true
                    val freedNote = cachedMetrics?.let {
                        "freed ${it.freedCacheKb} KB | avail ${it.afterRamMb} MB"
                    } ?: "reclaiming runtime heap & trimming cache"
                    baseLogs += "[1.1s] MEM  >> Cache flush & GC pass ($freedNote)"
                }
                if (elapsed >= 2300L && !addedNetLog) {
                    addedNetLog = true
                    val pingNote = cachedMetrics?.pingMs?.let { "${it} ms TCP RTT" } ?: "probing route"
                    baseLogs += "[2.3s] NET  >> Socket latency probe ($pingNote)"
                }
                if (elapsed >= 3500L && !addedGpuLog) {
                    addedGpuLog = true
                    val thermNote = cachedMetrics?.thermalLabel ?: "NOMINAL"
                    baseLogs += "[3.5s] GPU  >> Thermal guard [$thermNote] & frame pace sync"
                }
                if (elapsed >= 4500L && !addedFinalLog) {
                    addedFinalLog = true
                    baseLogs += if (currentSettings.autoLaunchAfterBoost) {
                        "[4.5s] EXEC >> Pipeline 100% ready — handing off to Free Fire..."
                    } else {
                        "[4.5s] EXEC >> Pipeline 100% ready — awaiting manual launch"
                    }
                }

                val stageTitle = when {
                    elapsed < 1100L -> "STAGE 1/4 • TARGET LOCK"
                    elapsed < 2300L -> "STAGE 2/4 • MEMORY & CACHE TRIM"
                    elapsed < 3500L -> "STAGE 3/4 • LATENCY PROBE"
                    elapsed < 4500L -> "STAGE 4/4 • THERMAL & FPS SYNC"
                    else -> "FINALIZING • LAUNCH HANDOFF"
                }

                _uiState.update {
                    it.copy(
                        boostPhase = BoostPhase.Running(
                            elapsedMs = elapsed,
                            totalMs = totalDurationMs,
                            stageTitle = stageTitle,
                            logLines = baseLogs.toList()
                        )
                    )
                }

                delay(16L)
            }

            val finalMetrics = cachedMetrics ?: passDeferred.await()
            preferences.recordBoostRun()
            val refreshedTelemetry = repository.collectTelemetry(measurePing = false)

            val launchOutcome = if (currentSettings.autoLaunchAfterBoost) {
                repository.launchFreeFire(currentSettings.preferredVariant)
            } else {
                LaunchOutcome.NotInstalled.let {
                    if (repository.resolveTargetGame(currentSettings.preferredVariant) != null) {
                        LaunchOutcome.Failed("Auto-launch disabled — tap Launch Free Fire below.")
                    } else {
                        LaunchOutcome.NotInstalled
                    }
                }
            }

            _uiState.update {
                it.copy(
                    telemetry = refreshedTelemetry,
                    boostPhase = BoostPhase.Completed(
                        metrics = finalMetrics,
                        launchOutcome = launchOutcome,
                        profileUsed = currentSettings.profile
                    )
                )
            }
        }
    }

    fun cancelBoost() {
        boostJob?.cancel()
        boostJob = null
        _uiState.update { it.copy(boostPhase = BoostPhase.Idle) }
    }

    fun returnToCockpit() {
        _uiState.update { it.copy(boostPhase = BoostPhase.Idle) }
        refreshTelemetry(measurePing = true)
    }

    fun launchFreeFireManual() {
        val outcome = repository.launchFreeFire(_uiState.value.settings.preferredVariant)
        _uiState.update { state ->
            val current = state.boostPhase
            if (current is BoostPhase.Completed) {
                state.copy(boostPhase = current.copy(launchOutcome = outcome))
            } else {
                state
            }
        }
    }

    fun openPlayStoreForFreeFire() {
        repository.openPlayStoreForFreeFire(_uiState.value.settings.preferredVariant)
    }

    fun openSystemSettings(action: String) {
        repository.openSystemSettings(action)
    }

    fun openAppBatteryDetails() {
        repository.openAppBatteryDetails()
    }

    fun exportApkToUri(destinationUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(apkExportStatus = ApkExportStatus.Working) }
            repository.exportApkToUri(destinationUri).fold(
                onSuccess = { bytes ->
                    val mb = String.format(java.util.Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
                    _uiState.update {
                        it.copy(
                            apkExportStatus = ApkExportStatus.Success(
                                "FF-Turbo.apk saved ($mb) to selected folder."
                            )
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            apkExportStatus = ApkExportStatus.Error(
                                err.localizedMessage ?: "Failed to save APK file."
                            )
                        )
                    }
                }
            )
        }
    }

    fun prepareAndShareApk(onShareReady: (Uri) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(apkExportStatus = ApkExportStatus.Working) }
            repository.prepareShareableApkUri().fold(
                onSuccess = { uri ->
                    _uiState.update {
                        it.copy(
                            apkExportStatus = ApkExportStatus.Success(
                                "FF-Turbo.apk prepared for direct sharing."
                            )
                        )
                    }
                    onShareReady(uri)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            apkExportStatus = ApkExportStatus.Error(
                                err.localizedMessage ?: "Failed to prepare APK for sharing."
                            )
                        )
                    }
                }
            )
        }
    }

    fun clearApkExportStatus() {
        _uiState.update { it.copy(apkExportStatus = ApkExportStatus.Idle) }
    }
}
