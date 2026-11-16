package com.example.ui.screens

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.DeviceTelemetry
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonElevated
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.IvoryText
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ScarletGlow
import java.util.Locale

@Composable
fun SystemDiagnosticsScreen(
    telemetry: DeviceTelemetry?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: (String) -> Unit,
    onOpenAppBattery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SYSTEM DIAGNOSTICS",
                            style = MaterialTheme.typography.headlineMedium,
                            color = IvoryText
                        )
                        Text(
                            text = "Live hardware telemetry & Android gaming shortcuts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AshMuted
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                        modifier = Modifier.testTag("diag_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh diagnostics",
                            tint = if (isRefreshing) AshMuted else CrimsonPrimary
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeveloperBoard,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "DEVICE HARDWARE",
                                style = MaterialTheme.typography.titleLarge,
                                color = IvoryText
                            )
                        }

                        DiagRow("Device", telemetry?.deviceModel ?: "--")
                        DiagRow("OS Build", telemetry?.androidVersion ?: "--")
                        DiagRow("CPU ABI", telemetry?.primaryAbi ?: "--")
                        DiagRow(
                            "Memory",
                            telemetry?.let { "${it.availableRamMb} MB free / ${it.totalRamMb} MB" } ?: "--"
                        )
                        DiagRow(
                            "Storage",
                            telemetry?.let {
                                String.format(
                                    Locale.US,
                                    "%.1f GB free / %.1f GB",
                                    it.freeStorageGb,
                                    it.totalStorageGb
                                )
                            } ?: "--"
                        )
                        DiagRow(
                            "Battery & Temp",
                            telemetry?.let {
                                String.format(
                                    Locale.US,
                                    "%d%% • %.1f°C (%s)",
                                    it.batteryPercent,
                                    it.batteryTempCelsius,
                                    it.thermalStatusLabel
                                )
                            } ?: "--"
                        )
                        DiagRow(
                            "Network & Ping",
                            telemetry?.let {
                                val pingPart = it.pingMs?.let { ms -> "${ms} ms" } ?: "No response"
                                "${it.networkLabel} (${it.downstreamMbps} Mbps) • $pingPart"
                            } ?: "--"
                        )
                        DiagRow("Game Mode API", telemetry?.gameModeLabel ?: "--")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ANDROID PERFORMANCE SHORTCUTS",
                                style = MaterialTheme.typography.titleLarge,
                                color = IvoryText
                            )
                        }

                        ShortcutRow(
                            icon = Icons.Filled.BatteryChargingFull,
                            title = "App Battery Optimization",
                            subtitle = "Ensure FF Turbo isn't restricted in background",
                            onClick = onOpenAppBattery,
                            testTag = "shortcut_battery"
                        )
                        ShortcutRow(
                            icon = Icons.Filled.Wifi,
                            title = "Wi-Fi & Network Settings",
                            subtitle = "Switch to 5GHz Wi-Fi for lower Free Fire ping",
                            onClick = { onOpenSettings(Settings.ACTION_WIFI_SETTINGS) },
                            testTag = "shortcut_wifi"
                        )
                        ShortcutRow(
                            icon = Icons.Filled.DoNotDisturbOn,
                            title = "Do Not Disturb Access",
                            subtitle = "Silence heads-up notifications during ranked matches",
                            onClick = { onOpenSettings(Settings.ACTION_SOUND_SETTINGS) },
                            testTag = "shortcut_dnd"
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = CarbonElevated)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "HOW ANDROID OPTIMIZATION WORKS",
                            style = MaterialTheme.typography.labelMedium,
                            color = ScarletGlow
                        )
                        Text(
                            text = "Without root access, Android protects other apps' private memory. FF Turbo performs real cache trimming, JVM GC, live thermal/RAM/ping checks, and clean Free Fire launch handoff.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AshMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AshMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = IvoryText
        )
    }
}

@Composable
private fun ShortcutRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonElevated)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = EmeraldReady,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = IvoryText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshMuted
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AshMuted
        )
    }
}
