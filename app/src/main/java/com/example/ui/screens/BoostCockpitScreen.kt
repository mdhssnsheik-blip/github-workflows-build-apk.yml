package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BoostProfile
import com.example.data.BoosterSettings
import com.example.data.DeviceTelemetry
import com.example.data.FreeFireVariant
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
import java.util.Locale

@Composable
fun BoostCockpitScreen(
    settings: BoosterSettings,
    telemetry: DeviceTelemetry?,
    isRefreshing: Boolean,
    onRefreshTelemetry: () -> Unit,
    onSelectProfile: (BoostProfile) -> Unit,
    onSelectVariant: (FreeFireVariant) -> Unit,
    onToggleAutoLaunch: (Boolean) -> Unit,
    onToggleTrimCache: (Boolean) -> Unit,
    onStartBoost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cockpit_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HERO COCKPIT HEADER WITH GENERATED RED-BLACK ARTWORK
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_booster_hero),
                            contentDescription = "Red and black gaming booster backdrop",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x88070709),
                                            Color(0xCC070709),
                                            Color(0xF50A080A)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = CrimsonContainer,
                                    border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "FREE FIRE ENGINE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ScarletGlow,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onRefreshTelemetry,
                                    enabled = !isRefreshing,
                                    modifier = Modifier.testTag("refresh_telemetry_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Refresh device telemetry",
                                        tint = if (isRefreshing) AshMuted else IvoryText
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "FF TURBO",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 32.sp
                                    ),
                                    color = IvoryText
                                )
                                val ffDetected = telemetry?.let {
                                    when {
                                        it.freeFireInfo.isInstalled -> "Free Fire Ready (${it.freeFireInfo.versionName})"
                                        it.freeFireMaxInfo.isInstalled -> "Free Fire MAX Ready (${it.freeFireMaxInfo.versionName})"
                                        else -> "Free Fire not installed • Diagnostic Mode Ready"
                                    }
                                } ?: "Scanning device packages..."

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (telemetry?.freeFireInfo?.isInstalled == true ||
                                                    telemetry?.freeFireMaxInfo?.isInstalled == true
                                                ) EmeraldReady else AmberWarn
                                            )
                                    )
                                    Text(
                                        text = ffDetected,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AshMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. LIVE TELEMETRY STRIP (REAL RAM, THERMAL, NETWORK PING)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryMiniCard(
                        icon = Icons.Filled.Memory,
                        label = "FREE RAM",
                        value = telemetry?.let {
                            String.format(Locale.US, "%.1f GB", it.availableRamMb / 1024f)
                        } ?: "--",
                        sub = telemetry?.let { "${100 - it.ramUsagePercent}% free" } ?: "Reading",
                        accentColor = CrimsonPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniCard(
                        icon = Icons.Filled.Thermostat,
                        label = "THERMAL",
                        value = telemetry?.thermalStatusLabel ?: "--",
                        sub = telemetry?.let {
                            String.format(Locale.US, "%.1f°C", it.batteryTempCelsius)
                        } ?: "Reading",
                        accentColor = if (telemetry?.isThermalWarning == true) AmberWarn else EmeraldReady,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniCard(
                        icon = Icons.Filled.NetworkPing,
                        label = "LATENCY",
                        value = telemetry?.pingMs?.let { "${it}ms" }
                            ?: (telemetry?.networkLabel ?: "--"),
                        sub = telemetry?.networkLabel ?: "Probing",
                        accentColor = if ((telemetry?.pingMs ?: 999) < 80) EmeraldReady else ScarletGlow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. PRIMARY "REDUCE LAG & BOOST FF" TRIGGER CARD (5-SECOND CODING SWEEP)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.55f)),
                    colors = CardDefaults.cardColors(containerColor = CarbonElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x38FF1E2D),
                                        Color(0x00191417)
                                    )
                                )
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LAG REDUCTION PIPELINE",
                                style = MaterialTheme.typography.labelLarge,
                                color = ScarletGlow
                            )
                            Text(
                                text = "RUNS: ${settings.totalBoostCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AshMuted
                            )
                        }

                        // Pulsing Outer Aura + Big Crimson Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(pulseScale),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = onStartBoost,
                                modifier = Modifier
                                    .testTag("lag_boost_button")
                                    .fillMaxWidth()
                                    .height(74.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrimsonPrimary,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 10.dp,
                                    pressedElevation = 2.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = "Boost icon",
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Column {
                                        Text(
                                            text = stringResource(R.string.boost_button_label),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = stringResource(R.string.boost_button_sub),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFFD6D9)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Tap to trigger the 5-second edge-to-edge code matrix, trim runtime cache, probe ping & auto-launch Free Fire.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AshMuted
                        )
                    }
                }
            }

            // 4. BOOST PROFILE SELECTOR
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
                                imageVector = Icons.Filled.Speed,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "BOOST PROFILE",
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("profile_chip_${profile.name.lowercase()}"),
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

                        Text(
                            text = "${settings.profile.subtitle} — ${settings.profile.targetNote}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AshMuted
                        )
                    }
                }
            }

            // 5. TARGET GAME & PIPELINE SWITCHES
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
                                imageVector = Icons.Filled.RocketLaunch,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "LAUNCH TARGET & TWEAKS",
                                style = MaterialTheme.typography.titleLarge,
                                color = IvoryText
                            )
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("variant_chip_${variant.name.lowercase()}"),
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

                        SwitchRow(
                            title = "Auto-Launch Free Fire (5s)",
                            subtitle = "Automatically opens the game right after the 5-second boost finishes",
                            checked = settings.autoLaunchAfterBoost,
                            onCheckedChange = onToggleAutoLaunch,
                            testTag = "auto_launch_switch"
                        )

                        SwitchRow(
                            title = "Trim App Cache & GC Pass",
                            subtitle = "Flushes cached files and triggers JVM memory compaction during boost",
                            checked = settings.trimCacheOnBoost,
                            onCheckedChange = onToggleTrimCache,
                            testTag = "trim_cache_switch"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMiniCard(
    icon: ImageVector,
    label: String,
    value: String,
    sub: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSurface)
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AshMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = IvoryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
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
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CrimsonPrimary,
                uncheckedThumbColor = AshMuted,
                uncheckedTrackColor = DeepEmber.copy(alpha = 0.35f)
            )
        )
    }
}
