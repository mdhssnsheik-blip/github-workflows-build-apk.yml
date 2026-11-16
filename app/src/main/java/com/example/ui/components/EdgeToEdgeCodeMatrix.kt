package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.ui.BoostPhase
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DeepEmber
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.IvoryText
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ScarletGlow
import java.util.Locale
import kotlin.math.sin

private val HORIZONTAL_CODE_STREAMS = listOf(
    "0x7FF4A2C0 // ALLOC_FRAME_BUFFER(120Hz) -> LOCK_VSYNC=TRUE :: TRIM_HEAP(0x004F)",
    "SYS.EXEC :: com.dts.freefireth -> FLUSH_L2_CACHE() :: GC_RECLAIM_PAGES(4096)",
    "NET.ROUTE :: TCP_NODELAY=1 :: RTT_PROBE(1.1.1.1:443) :: PACKET_PACER=ACTIVE",
    "GPU.SHADER :: PREWARM_PIPELINE(VULKAN_1_3) :: THERMAL_GUARD=NOMINAL :: 0x99A1",
    "KERNEL.SCHED :: BOOST_UI_THREAD(PRIO=-8) :: ZERO_JANK_VSYNC :: FPS_TARGET=MAX",
    "MEM.SWEEP :: PURGE_TEMP_CACHE() :: FREE_PAGES_VERIFIED :: 0xDEAD_BEEF -> OK",
    "IO.BUFFER :: PREFETCH_ASSET_BUNDLE(FF_MAP_BERMUDA) :: READ_AHEAD=256KB :: READY",
    "NET.SOCKET :: JITTER_FILTER=ON :: MTU_DISCOVERY=1500 :: PING_STABILIZER=100%",
    "GFX.RENDER :: TEXTURE_STREAM_PRIORITY=HIGH :: DROP_STALE_FRAMES(0) :: SYNC_OK",
    "PROC.GUARD :: DND_FOCUS_READY :: BATTERY_THROTTLE_CHECK=CLEAR :: 0x55FF01",
    "SHADER.OPT :: COMPILE_FAST_PATH(GLES_3_2) :: DRAW_CALL_BATCH=64 :: LATENCY_LOW",
    "FINAL.HANDOFF :: VERIFY_SIGNATURE(com.dts.freefireth) :: LAUNCH_INTENT_QUEUED"
)

private const val MATRIX_GLYPHS = "01FF9X#A7C4E2B8D5%$/>_*"

@Composable
fun EdgeToEdgeCodeMatrix(
    phase: BoostPhase.Running,
    onAbort: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onAbort() }

    val context = LocalContext.current
    val monoTypeface = remember(context) {
        runCatching { ResourcesCompat.getFont(context, R.font.jetbrains_mono) }
            .getOrNull() ?: Typeface.MONOSPACE
    }

    val infiniteTransition = rememberInfiniteTransition(label = "code_matrix_motion")
    val streamCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stream_cycle"
    )
    val rainCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_cycle"
    )

    val streamPaint = remember(monoTypeface) {
        Paint().apply {
            isAntiAlias = true
            typeface = monoTypeface
        }
    }
    val rainPaint = remember(monoTypeface) {
        Paint().apply {
            isAntiAlias = true
            typeface = monoTypeface
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // FULL EDGE-TO-EDGE MOVING CODE STREAMS & MATRIX RAIN CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle radial crimson atmospheric core
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x38FF1E2D),
                        Color(0x148F0B17),
                        ObsidianBlack
                    ),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = maxOf(w, h) * 0.72f
                )
            )

            // 1. Vertical Matrix Code Rain spanning full width and height
            val colSpacing = 28.dp.toPx()
            val colCount = (w / colSpacing).toInt().coerceAtLeast(8)
            val charStepY = 18.dp.toPx()
            rainPaint.textSize = 12.sp.toPx()

            for (c in 0..colCount) {
                val x = c * colSpacing + 4.dp.toPx()
                val speedSeed = ((c * 37) % 11) / 10f + 0.65f
                val headOffset = ((rainCycle * speedSeed + (c * 0.17f)) % 1.35f) * h
                val trailLength = 10 + (c % 7)

                for (t in 0 until trailLength) {
                    val y = headOffset - t * charStepY
                    if (y < -charStepY || y > h + charStepY) continue
                    val alpha = ((1f - (t.toFloat() / trailLength)) * 0.48f).coerceIn(0.04f, 0.55f)
                    rainPaint.color = if (t == 0) {
                        ScarletGlow.copy(alpha = 0.75f).toArgb()
                    } else {
                        CrimsonPrimary.copy(alpha = alpha).toArgb()
                    }
                    val glyphIndex = (c * 7 + t + (phase.elapsedMs / 90L).toInt()) % MATRIX_GLYPHS.length
                    drawContext.canvas.nativeCanvas.drawText(
                        MATRIX_GLYPHS[glyphIndex].toString(),
                        x,
                        y,
                        rainPaint
                    )
                }
            }

            // 2. Horizontal Edge-to-Edge Moving Code Ticker Rows (left edge -> right edge & reverse)
            val rowCount = HORIZONTAL_CODE_STREAMS.size
            val rowHeight = h / (rowCount + 1)
            streamPaint.textSize = 11.5.sp.toPx()

            for (r in 0 until rowCount) {
                val baseLine = HORIZONTAL_CODE_STREAMS[r]
                val repeatedLine = "$baseLine   :::   $baseLine   :::   $baseLine"
                val textWidth = streamPaint.measureText(baseLine) + 48.dp.toPx()
                val y = (r + 1) * rowHeight
                val isLeftToRight = r % 2 == 0
                val speedFactor = 1f + (r % 4) * 0.28f
                val offsetPx = ((streamCycle * speedFactor) % 1f) * textWidth

                val startX = if (isLeftToRight) {
                    -textWidth + offsetPx
                } else {
                    -offsetPx
                }

                // Brighter crimson closer to the top/bottom edges and muted near the center card
                val distFromCenter = kotlin.math.abs((y / h) - 0.48f)
                val alpha = (0.28f + distFromCenter * 1.15f).coerceIn(0.24f, 0.85f)
                streamPaint.color = if (r % 3 == 0) {
                    ScarletGlow.copy(alpha = alpha).toArgb()
                } else {
                    CrimsonPrimary.copy(alpha = alpha).toArgb()
                }

                // Draw across the entire width from 0 to w without any margin
                var drawX = startX
                while (drawX < w) {
                    drawContext.canvas.nativeCanvas.drawText(repeatedLine, drawX, y, streamPaint)
                    drawX += textWidth * 2f
                }
            }

            // 3. Sweeping Crimson Laser Scanline across full screen width
            val scanY = (0.5f + 0.46f * sin(phase.progress * Math.PI * 4.0).toFloat()) * h
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CrimsonPrimary.copy(alpha = 0.25f),
                        ScarletGlow,
                        Color.White.copy(alpha = 0.9f),
                        ScarletGlow,
                        CrimsonPrimary.copy(alpha = 0.25f)
                    )
                ),
                start = Offset(0f, scanY),
                end = Offset(w, scanY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // FOREGROUND TACTICAL HUD OVERLAY
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Header
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xCC120A0D),
                border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.boost_running_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = IvoryText
                    )
                }
            }

            // Center 5.0s Countdown Ring + Live Execution Console
            Column(
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Countdown Ring
                Box(
                    modifier = Modifier.size(184.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 10.dp.toPx()
                        drawArc(
                            color = DeepEmber.copy(alpha = 0.4f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(CrimsonPrimary, ScarletGlow, Color.White, CrimsonPrimary)
                            ),
                            startAngle = -90f,
                            sweepAngle = phase.progress * 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1fs", phase.remainingSeconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = IvoryText
                        )
                        Text(
                            text = "${(phase.progress * 100).toInt()}% BOOST",
                            style = MaterialTheme.typography.labelMedium,
                            color = ScarletGlow
                        )
                    }
                }

                // Translucent Dark Terminal Console Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xE60B090B))
                        .border(1.dp, CrimsonPrimary.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = phase.stageTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = ScarletGlow
                        )
                        Text(
                            text = "LIVE KERNEL STREAM",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldReady
                        )
                    }

                    LinearProgressIndicator(
                        progress = { phase.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CrimsonPrimary,
                        trackColor = CarbonBorder
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    phase.logLines.forEachIndexed { index, line ->
                        val isLatest = index == phase.logLines.lastIndex
                        Text(
                            text = line,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = if (isLatest) IvoryText else AshMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Bottom Abort Action
            OutlinedButton(
                onClick = onAbort,
                modifier = Modifier
                    .testTag("abort_boost_button")
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xB3120B0D),
                    contentColor = IvoryText
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Abort boost sequence",
                    tint = ScarletGlow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "CANCEL SEQUENCE",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
