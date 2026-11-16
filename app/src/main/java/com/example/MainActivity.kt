package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BoostPhase
import com.example.ui.BoosterViewModel
import com.example.ui.CockpitTab
import com.example.ui.components.EdgeToEdgeCodeMatrix
import com.example.ui.screens.BoostCockpitScreen
import com.example.ui.screens.BoostCompleteScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SystemDiagnosticsScreen
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.FFTurboTheme
import com.example.ui.theme.IvoryText
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ScarletGlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FFTurboTheme {
                FFTurboRoot()
            }
        }
    }
}

@Composable
fun FFTurboRoot(viewModel: BoosterViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val phase = state.boostPhase) {
        is BoostPhase.Running -> {
            EdgeToEdgeCodeMatrix(
                phase = phase,
                onAbort = viewModel::cancelBoost
            )
        }

        is BoostPhase.Completed -> {
            BoostCompleteScreen(
                phase = phase,
                onLaunchManual = viewModel::launchFreeFireManual,
                onOpenPlayStore = viewModel::openPlayStoreForFreeFire,
                onRerunBoost = viewModel::startFiveSecondBoost,
                onBackToCockpit = viewModel::returnToCockpit
            )
        }

        BoostPhase.Idle -> {
            if (state.activeTab != CockpitTab.BOOST) {
                BackHandler { viewModel.selectTab(CockpitTab.BOOST) }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ObsidianBlack,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (!useNavigationRail) {
                            NavigationBar(
                                containerColor = CarbonSurface,
                                contentColor = IvoryText
                            ) {
                                NavigationBarItem(
                                    selected = state.activeTab == CockpitTab.BOOST,
                                    onClick = { viewModel.selectTab(CockpitTab.BOOST) },
                                    modifier = Modifier.testTag("tab_boost"),
                                    icon = {
                                        Icon(
                                            imageVector = if (state.activeTab == CockpitTab.BOOST) {
                                                Icons.Filled.Bolt
                                            } else {
                                                Icons.Outlined.Bolt
                                            },
                                            contentDescription = stringResource(R.string.tab_boost)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.tab_boost)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ScarletGlow,
                                        selectedTextColor = ScarletGlow,
                                        indicatorColor = CrimsonContainer,
                                        unselectedIconColor = AshMuted,
                                        unselectedTextColor = AshMuted
                                    )
                                )
                                NavigationBarItem(
                                    selected = state.activeTab == CockpitTab.SYSTEM,
                                    onClick = { viewModel.selectTab(CockpitTab.SYSTEM) },
                                    modifier = Modifier.testTag("tab_system"),
                                    icon = {
                                        Icon(
                                            imageVector = if (state.activeTab == CockpitTab.SYSTEM) {
                                                Icons.Filled.DeveloperBoard
                                            } else {
                                                Icons.Outlined.DeveloperBoard
                                            },
                                            contentDescription = stringResource(R.string.tab_diagnostics)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.tab_diagnostics)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ScarletGlow,
                                        selectedTextColor = ScarletGlow,
                                        indicatorColor = CrimsonContainer,
                                        unselectedIconColor = AshMuted,
                                        unselectedTextColor = AshMuted
                                    )
                                )
                                NavigationBarItem(
                                    selected = state.activeTab == CockpitTab.SETTINGS,
                                    onClick = { viewModel.selectTab(CockpitTab.SETTINGS) },
                                    modifier = Modifier.testTag("tab_settings"),
                                    icon = {
                                        Icon(
                                            imageVector = if (state.activeTab == CockpitTab.SETTINGS) {
                                                Icons.Filled.Settings
                                            } else {
                                                Icons.Outlined.Settings
                                            },
                                            contentDescription = stringResource(R.string.tab_settings)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.tab_settings)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ScarletGlow,
                                        selectedTextColor = ScarletGlow,
                                        indicatorColor = CrimsonContainer,
                                        unselectedIconColor = AshMuted,
                                        unselectedTextColor = AshMuted
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = CarbonSurface,
                                contentColor = IvoryText
                            ) {
                                NavigationRailItem(
                                    selected = state.activeTab == CockpitTab.BOOST,
                                    onClick = { viewModel.selectTab(CockpitTab.BOOST) },
                                    modifier = Modifier.testTag("rail_tab_boost"),
                                    icon = {
                                        Icon(
                                            imageVector = if (state.activeTab == CockpitTab.BOOST) {
                                                Icons.Filled.Bolt
                                            } else {
                                                Icons.Outlined.Bolt
                                            },
                                            contentDescription = stringResource(R.string.tab_boost)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.tab_boost)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = ScarletGlow,
                                        selectedTextColor = ScarletGlow,
                                        indicatorColor = CrimsonContainer,
                                        unselectedIconColor = AshMuted,
                                        unselectedTextColor = AshMuted
                                    )
                                )
                                NavigationRailItem(
                                    selected = state.activeTab == CockpitTab.SYSTEM,
                                    onClick = { viewModel.selectTab(CockpitTab.SYSTEM) },
                                    modifier = Modifier.testTag("rail_tab_system"),
                                    icon = {
                                        Icon(
                                            imageVector = if (state.activeTab == CockpitTab.SYSTEM) {
                                                Icons.Filled.DeveloperBoard
                                            } else {
                                                Icons.Outlined.DeveloperBoard
                                            },
                                            contentDescription = stringResource(R.string.tab_diagnostics)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.tab_diagnostics)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = ScarletGlow,
                                        selectedTextColor = ScarletGlow,
                                        indicatorColor = CrimsonContainer,
                                        unselectedIconColor = AshMuted,
                                        unselectedTextColor = AshMuted
                                    )
                                )
                                NavigationRailItem(
                                    selected = state.activeTab == CockpitTab.SETTINGS,
                                    onClick = { viewModel.selectTab(CockpitTab.SETTINGS) },
                                    modifier = Modifier.testTag("rail_tab_settings"),
                                    icon = {
                                        Icon(
                                            imageVector = if (state.activeTab == CockpitTab.SETTINGS) {
                                                Icons.Filled.Settings
                                            } else {
                                                Icons.Outlined.Settings
                                            },
                                            contentDescription = stringResource(R.string.tab_settings)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.tab_settings)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = ScarletGlow,
                                        selectedTextColor = ScarletGlow,
                                        indicatorColor = CrimsonContainer,
                                        unselectedIconColor = AshMuted,
                                        unselectedTextColor = AshMuted
                                    )
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                            when (state.activeTab) {
                                CockpitTab.BOOST -> BoostCockpitScreen(
                                    settings = state.settings,
                                    telemetry = state.telemetry,
                                    isRefreshing = state.isRefreshingTelemetry,
                                    onRefreshTelemetry = { viewModel.refreshTelemetry(measurePing = true) },
                                    onSelectProfile = viewModel::setProfile,
                                    onSelectVariant = viewModel::setPreferredVariant,
                                    onToggleAutoLaunch = viewModel::setAutoLaunch,
                                    onToggleTrimCache = viewModel::setTrimCache,
                                    onStartBoost = viewModel::startFiveSecondBoost
                                )

                                CockpitTab.SYSTEM -> SystemDiagnosticsScreen(
                                    telemetry = state.telemetry,
                                    isRefreshing = state.isRefreshingTelemetry,
                                    onRefresh = { viewModel.refreshTelemetry(measurePing = true) },
                                    onOpenSettings = viewModel::openSystemSettings,
                                    onOpenAppBattery = viewModel::openAppBatteryDetails
                                )

                                CockpitTab.SETTINGS -> SettingsScreen(
                                    settings = state.settings,
                                    selfApkInfo = state.selfApkInfo,
                                    exportStatus = state.apkExportStatus,
                                    onExportApkToUri = viewModel::exportApkToUri,
                                    onPrepareAndShareApk = viewModel::prepareAndShareApk,
                                    onSelectProfile = viewModel::setProfile,
                                    onSelectVariant = viewModel::setPreferredVariant,
                                    onToggleAutoLaunch = viewModel::setAutoLaunch,
                                    onToggleTrimCache = viewModel::setTrimCache
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
