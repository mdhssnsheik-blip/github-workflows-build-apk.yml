package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BoostProfile
import com.example.data.BoosterSettings
import com.example.data.FreeFireVariant
import com.example.data.SelfApkInfo
import com.example.ui.ApkExportStatus
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonElevated
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DeepEmber
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.IvoryText
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ScarletGlow

@Composable
fun SettingsScreen(
    settings: BoosterSettings,
    selfApkInfo: SelfApkInfo?,
    exportStatus: ApkExportStatus,
    onExportApkToUri: (Uri) -> Unit,
    onPrepareAndShareApk: ((Uri) -> Unit) -> Unit,
    onSelectProfile: (BoostProfile) -> Unit,
    onSelectVariant: (FreeFireVariant) -> Unit,
    onToggleAutoLaunch: (Boolean) -> Unit,
    onToggleTrimCache: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val createApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        if (uri != null) {
            onExportApkToUri(uri)
        }
    }

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
                Column {
                    Text(
                        text = "SETTINGS & APK DOWNLOAD",
                        style = MaterialTheme.typography.headlineMedium,
                        color = IvoryText
                    )
                    Text(
                        text = "Save or share FF-Turbo.apk & configure booster rules",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                }
            }

            // 1. REAL IN-APP APK EXPORT & SHARE CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.6f)),
                    colors = CardDefaults.cardColors(containerColor = CarbonElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Android,
                                contentDescription = "APK Package",
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "FF-TURBO.APK EXPORT",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = IvoryText
                                )
                                Text(
                                    text = "Direct .apk package (not .zip)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ScarletGlow
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = CarbonSurface,
                            border = BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InfoLine("File Name", "FF-Turbo.apk")
                                InfoLine("Package ID", selfApkInfo?.packageName ?: "--")
                                InfoLine("Version", selfApkInfo?.versionName ?: "--")
                                InfoLine("APK Size", selfApkInfo?.sizeMbFormatted ?: "--")
                            }
                        }

                        val isWorking = exportStatus is ApkExportStatus.Working
                        val canExport = selfApkInfo?.sourceAvailable == true && !isWorking

                        Button(
                            onClick = { createApkLauncher.launch("FF-Turbo.apk") },
                            enabled = canExport,
                            modifier = Modifier
                                .testTag("save_apk_button")
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            if (isWorking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "Save APK",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "DOWNLOAD / SAVE FF-Turbo.apk",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onPrepareAndShareApk { shareUri ->
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/vnd.android.package-archive"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        putExtra(Intent.EXTRA_SUBJECT, "FF-Turbo.apk")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooser = Intent.createChooser(shareIntent, "Share FF-Turbo.apk")
                                    context.startActivity(chooser)
                                }
                            },
                            enabled = canExport,
                            modifier = Modifier
                                .testTag("share_apk_button")
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CrimsonContainer.copy(alpha = 0.4f),
                                contentColor = IvoryText
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share APK",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "SHARE FF-Turbo.apk",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        when (exportStatus) {
                            is ApkExportStatus.Success -> {
                                Text(
                                    text = exportStatus.message,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldReady
                                )
                            }

                            is ApkExportStatus.Error -> {
                                Text(
                                    text = exportStatus.message,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AmberWarn
                                )
                            }

                            else -> Unit
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = ObsidianBlack,
                            border = BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Text(
                                text = "Note: If you are using the browser preview emulator, saving exports to the cloud emulator's Downloads folder. Once installed on an Android phone, this saves or shares the real FF-Turbo.apk directly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AshMuted,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // 2. BOOSTER PREFERENCES
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                text = "BOOSTER CONFIGURATION",
                                style = MaterialTheme.typography.titleLarge,
                                color = IvoryText
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BoostProfile.entries.forEach { profile ->
                                val selected = settings.profile == profile
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectProfile(profile) },
                                    modifier = Modifier.weight(1f),
                                    label = {
                                        Text(
                                            text = profile.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = CarbonElevated,
                                        labelColor = AshMuted,
                                        selectedContainerColor = CrimsonContainer,
                                        selectedLabelColor = ScarletGlow
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = CarbonBorder,
                                        selectedBorderColor = CrimsonPrimary
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FreeFireVariant.entries.forEach { variant ->
                                val selected = settings.preferredVariant == variant
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectVariant(variant) },
                                    modifier = Modifier.weight(1f),
                                    label = {
                                        Text(
                                            text = variant.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = CarbonElevated,
                                        labelColor = AshMuted,
                                        selectedContainerColor = CrimsonContainer,
                                        selectedLabelColor = ScarletGlow
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = CarbonBorder,
                                        selectedBorderColor = CrimsonPrimary
                                    )
                                )
                            }
                        }

                        SettingsSwitchRow(
                            title = "Auto-Launch Free Fire (5s)",
                            subtitle = "Automatically opens Free Fire after the 5-second coding sequence",
                            checked = settings.autoLaunchAfterBoost,
                            onCheckedChange = onToggleAutoLaunch
                        )

                        SettingsSwitchRow(
                            title = "Trim App Cache & GC Pass",
                            subtitle = "Clears temporary cache and compacts heap memory before launch",
                            checked = settings.trimCacheOnBoost,
                            onCheckedChange = onToggleTrimCache
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
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
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonElevated)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CrimsonPrimary,
                uncheckedThumbColor = AshMuted,
                uncheckedTrackColor = DeepEmber.copy(alpha = 0.35f)
            )
        )
    }
}
