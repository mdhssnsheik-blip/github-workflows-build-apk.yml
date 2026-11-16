package com.example.data

import android.app.ActivityManager
import android.app.GameManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.net.Socket

data class InstalledGameInfo(
    val packageName: String,
    val displayName: String,
    val versionName: String,
    val isInstalled: Boolean
)

data class DeviceTelemetry(
    val availableRamMb: Long,
    val totalRamMb: Long,
    val ramUsagePercent: Int,
    val freeStorageGb: Double,
    val totalStorageGb: Double,
    val batteryPercent: Int,
    val batteryTempCelsius: Float,
    val thermalStatusLabel: String,
    val isThermalWarning: Boolean,
    val networkLabel: String,
    val downstreamMbps: Int,
    val pingMs: Int?,
    val gameModeLabel: String,
    val freeFireInfo: InstalledGameInfo,
    val freeFireMaxInfo: InstalledGameInfo,
    val deviceModel: String,
    val androidVersion: String,
    val primaryAbi: String
)

sealed class LaunchOutcome {
    data class Launched(val gameName: String, val packageName: String) : LaunchOutcome()
    data object NotInstalled : LaunchOutcome()
    data class Failed(val reason: String) : LaunchOutcome()
}

data class BoostPassMetrics(
    val freedCacheKb: Long,
    val beforeRamMb: Long,
    val afterRamMb: Long,
    val pingMs: Int?,
    val thermalLabel: String
)

data class SelfApkInfo(
    val packageName: String,
    val versionName: String,
    val sizeBytes: Long,
    val sourceAvailable: Boolean
) {
    val sizeMbFormatted: String
        get() = if (sizeBytes > 0L) {
            String.format(java.util.Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0))
        } else {
            "--"
        }
}

class SystemDiagnosticsRepository(private val context: Context) {

    companion object {
        const val PKG_FREE_FIRE = "com.dts.freefireth"
        const val PKG_FREE_FIRE_MAX = "com.dts.freefiremax"
    }

    suspend fun collectTelemetry(measurePing: Boolean = true): DeviceTelemetry =
        withContext(Dispatchers.IO) {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)

            val totalRamMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
            val availRamMb = memInfo.availMem / (1024L * 1024L)
            val usedPercent = (((totalRamMb - availRamMb).toFloat() / totalRamMb) * 100f)
                .toInt()
                .coerceIn(0, 100)

            val stat = StatFs(Environment.getDataDirectory().path)
            val freeStorageGb =
                (stat.availableBlocksLong * stat.blockSizeLong).toDouble() / (1024.0 * 1024.0 * 1024.0)
            val totalStorageGb =
                (stat.blockCountLong * stat.blockSizeLong).toDouble() / (1024.0 * 1024.0 * 1024.0)

            val batteryIntent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val batteryPercent = if (level >= 0 && scale > 0) (level * 100) / scale else 100
            val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) ?: 300
            val batteryTempC = tempTenths / 10f

            val (thermalLabel, thermalWarn) = readThermalStatus()
            val (networkLabel, downstreamMbps) = readNetworkSummary()
            val pingMs = if (measurePing && networkLabel != "Offline") measureTcpPingMs() else null

            val gameModeLabel = readGameModeLabel()
            val ffInfo = inspectPackage(PKG_FREE_FIRE, "Free Fire")
            val ffMaxInfo = inspectPackage(PKG_FREE_FIRE_MAX, "Free Fire MAX")

            DeviceTelemetry(
                availableRamMb = availRamMb,
                totalRamMb = totalRamMb,
                ramUsagePercent = usedPercent,
                freeStorageGb = freeStorageGb,
                totalStorageGb = totalStorageGb,
                batteryPercent = batteryPercent,
                batteryTempCelsius = batteryTempC,
                thermalStatusLabel = thermalLabel,
                isThermalWarning = thermalWarn,
                networkLabel = networkLabel,
                downstreamMbps = downstreamMbps,
                pingMs = pingMs,
                gameModeLabel = gameModeLabel,
                freeFireInfo = ffInfo,
                freeFireMaxInfo = ffMaxInfo,
                deviceModel = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}",
                androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
            )
        }

    fun inspectPackage(packageName: String, displayName: String): InstalledGameInfo {
        return try {
            val pm = context.packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            InstalledGameInfo(
                packageName = packageName,
                displayName = displayName,
                versionName = info.versionName ?: "Installed",
                isInstalled = true
            )
        } catch (_: Exception) {
            InstalledGameInfo(
                packageName = packageName,
                displayName = displayName,
                versionName = "Not Installed",
                isInstalled = false
            )
        }
    }

    suspend fun performOptimizationPass(trimCache: Boolean): BoostPassMetrics =
        withContext(Dispatchers.IO) {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val beforeMem = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(beforeMem)
            val beforeMb = beforeMem.availMem / (1024L * 1024L)

            var freedBytes = 0L
            if (trimCache) {
                freedBytes += clearDirectoryContents(context.cacheDir)
                freedBytes += clearDirectoryContents(context.codeCacheDir)
                context.externalCacheDir?.let { freedBytes += clearDirectoryContents(it) }
            }

            // Trigger JVM garbage collection to reclaim runtime heap headroom
            Runtime.getRuntime().gc()
            System.gc()

            val afterMem = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(afterMem)
            val afterMb = afterMem.availMem / (1024L * 1024L)

            val pingMs = measureTcpPingMs()
            val (thermalLabel, _) = readThermalStatus()

            BoostPassMetrics(
                freedCacheKb = (freedBytes / 1024L).coerceAtLeast(0L),
                beforeRamMb = beforeMb,
                afterRamMb = afterMb,
                pingMs = pingMs,
                thermalLabel = thermalLabel
            )
        }

    fun resolveTargetGame(preferred: FreeFireVariant): InstalledGameInfo? {
        val ff = inspectPackage(PKG_FREE_FIRE, "Free Fire")
        val ffMax = inspectPackage(PKG_FREE_FIRE_MAX, "Free Fire MAX")
        return when (preferred) {
            FreeFireVariant.FREE_FIRE -> if (ff.isInstalled) ff else null
            FreeFireVariant.FREE_FIRE_MAX -> if (ffMax.isInstalled) ffMax else null
            FreeFireVariant.AUTO -> when {
                ff.isInstalled -> ff
                ffMax.isInstalled -> ffMax
                else -> null
            }
        }
    }

    fun launchFreeFire(preferred: FreeFireVariant): LaunchOutcome {
        val target = resolveTargetGame(preferred) ?: return LaunchOutcome.NotInstalled
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(target.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
                LaunchOutcome.Launched(target.displayName, target.packageName)
            } else {
                LaunchOutcome.NotInstalled
            }
        } catch (e: Exception) {
            LaunchOutcome.Failed(e.localizedMessage ?: "Unable to launch ${target.displayName}")
        }
    }

    fun openPlayStoreForFreeFire(preferred: FreeFireVariant) {
        val pkg = when (preferred) {
            FreeFireVariant.FREE_FIRE_MAX -> PKG_FREE_FIRE_MAX
            else -> PKG_FREE_FIRE
        }
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            runCatching { context.startActivity(webIntent) }
        }
    }

    fun openSystemSettings(action: String) {
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(fallback) }
        }
    }

    fun openAppBatteryDetails() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun getSelfApkInfo(): SelfApkInfo {
        return runCatching {
            val appInfo = context.applicationInfo
            val apkFile = File(appInfo.sourceDir)
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            SelfApkInfo(
                packageName = context.packageName,
                versionName = pkgInfo.versionName ?: "1.0",
                sizeBytes = if (apkFile.exists()) apkFile.length() else 0L,
                sourceAvailable = apkFile.exists() && apkFile.canRead()
            )
        }.getOrDefault(
            SelfApkInfo(
                packageName = context.packageName,
                versionName = "1.0",
                sizeBytes = 0L,
                sourceAvailable = false
            )
        )
    }

    suspend fun exportApkToUri(destinationUri: Uri): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val sourceFile = File(context.applicationInfo.sourceDir)
            require(sourceFile.exists() && sourceFile.canRead()) {
                "APK source file is not readable on this device."
            }
            var totalBytes = 0L
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(sourceFile).use { input ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        totalBytes += read
                    }
                    output.flush()
                }
            } ?: error("Unable to open destination file stream.")
            totalBytes
        }
    }

    suspend fun prepareShareableApkUri(): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val sourceFile = File(context.applicationInfo.sourceDir)
            require(sourceFile.exists() && sourceFile.canRead()) {
                "APK source file is not readable on this device."
            }
            val exportDir = File(context.cacheDir, "apk_exports").apply { mkdirs() }
            val outFile = File(exportDir, "FF-Turbo.apk")
            sourceFile.copyTo(outFile, overwrite = true)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
        }
    }

    private fun readThermalStatus(): Pair<String, Boolean> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return "NOMINAL" to false
        }
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return when (powerManager?.currentThermalStatus) {
            PowerManager.THERMAL_STATUS_NONE -> "COOL" to false
            PowerManager.THERMAL_STATUS_LIGHT -> "WARM" to false
            PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE" to true
            PowerManager.THERMAL_STATUS_SEVERE -> "HOT" to true
            PowerManager.THERMAL_STATUS_CRITICAL,
            PowerManager.THERMAL_STATUS_EMERGENCY,
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "THROTTLING" to true
            else -> "NOMINAL" to false
        }
    }

    private fun readNetworkSummary(): Pair<String, Int> {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "Unknown" to 0
        val network = cm.activeNetwork ?: return "Offline" to 0
        val caps = cm.getNetworkCapabilities(network) ?: return "Offline" to 0
        val downMbps = (caps.linkDownstreamBandwidthKbps / 1000).coerceAtLeast(0)
        val type = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }
        return type to downMbps
    }

    private fun readGameModeLabel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return runCatching {
                val gm = context.getSystemService(GameManager::class.java)
                when (gm?.gameMode) {
                    GameManager.GAME_MODE_PERFORMANCE -> "PERFORMANCE"
                    GameManager.GAME_MODE_BATTERY -> "BATTERY"
                    GameManager.GAME_MODE_STANDARD -> "STANDARD"
                    GameManager.GAME_MODE_CUSTOM -> "CUSTOM"
                    else -> "GAME READY"
                }
            }.getOrDefault("GAME READY")
        }
        return "GAME READY"
    }

    private fun measureTcpPingMs(): Int? {
        val targets = listOf(
            InetSocketAddress("1.1.1.1", 443),
            InetSocketAddress("8.8.8.8", 53)
        )
        for (target in targets) {
            val result = runCatching {
                Socket().use { socket ->
                    val startNs = System.nanoTime()
                    socket.connect(target, 1200)
                    val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).toInt()
                    elapsedMs.coerceAtLeast(1)
                }
            }.getOrNull()
            if (result != null) return result
        }
        return null
    }

    private fun clearDirectoryContents(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var bytes = 0L
        dir.listFiles()?.forEach { child ->
            bytes += if (child.isDirectory) {
                val sub = clearDirectoryContents(child)
                child.delete()
                sub
            } else {
                val len = child.length()
                if (child.delete()) len else 0L
            }
        }
        return bytes
    }
}
