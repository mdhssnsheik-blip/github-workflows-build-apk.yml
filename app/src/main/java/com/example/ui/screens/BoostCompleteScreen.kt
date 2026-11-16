package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LaunchOutcome
import com.example.ui.BoostPhase
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonElevated
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.IvoryText
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ScarletGlow

@Composable
fun BoostCompleteScreen(
    phase: BoostPhase.Completed,
    onLaunchManual: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onRerunBoost: () -> Unit,
    onBackToCockpit: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToCockpit() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A070B), ObsidianBlack, ObsidianBlack)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(EmeraldReady.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Boost complete",
                    tint = EmeraldReady,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.boost_complete_title),
                style = MaterialTheme.typography.headlineMedium,
                color = IvoryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${phase.profileUsed.title} optimization sequence finished in 5.0s",
                style = MaterialTheme.typography.bodyMedium,
                color = AshMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Metrics Summary Card
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
                    MetricRow("Cache Trimmed", "${phase.metrics.freedCacheKb} KB cleared")
                    MetricRow(
                        "Available RAM",
                        "${phase.metrics.beforeRamMb} MB → ${phase.metrics.afterRamMb} MB"
                    )
                    MetricRow(
                        "Network Ping",
                        phase.metrics.pingMs?.let { "$it ms" } ?: "Offline / Local"
                    )
                    MetricRow("Thermal Status", phase.metrics.thermalLabel)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Launch Status Feedback Card
            val (statusTitle, statusBody, statusAccent) = when (val outcome = phase.launchOutcome) {
                is LaunchOutcome.Launched -> Triple(
                    "${outcome.gameName} Launched",
                    "The game was launched automatically after the 5s sweep. Return here anytime before your next match.",
                    EmeraldReady
                )
                LaunchOutcome.NotInstalled -> Triple(
                    "Free Fire Not Found on Device",
                    "The 5s boost completed 100%, but neither Free Fire nor Free Fire MAX is currently installed on this device.",
                    AmberWarn
                )
                is LaunchOutcome.Failed -> Triple(
                    "Ready to Launch",
                    outcome.reason,
                    ScarletGlow
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonElevated,
                border = BorderStroke(1.dp, statusAccent.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = statusAccent
                    )
                    Text(
                        text = statusBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (phase.launchOutcome is LaunchOutcome.NotInstalled) {
                Button(
                    onClick = onOpenPlayStore,
                    modifier = Modifier
                        .testTag("install_ff_button")
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.install_free_fire),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Button(
                    onClick = onLaunchManual,
                    modifier = Modifier
                        .testTag("launch_ff_button")
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.launch_free_fire),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onRerunBoost,
                modifier = Modifier
                    .testTag("rerun_boost_button")
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CrimsonContainer.copy(alpha = 0.45f),
                    contentColor = IvoryText
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.run_boost_again),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onBackToCockpit,
                modifier = Modifier
                    .testTag("back_to_cockpit_button")
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.back_to_cockpit),
                    style = MaterialTheme.typography.labelLarge,
                    color = AshMuted
                )
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
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
