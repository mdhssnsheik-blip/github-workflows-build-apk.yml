package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RedBlackPremiumColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = CrimsonContainer,
    onPrimaryContainer = ScarletGlow,
    secondary = ScarletGlow,
    onSecondary = ObsidianBlack,
    secondaryContainer = CrimsonMuted,
    onSecondaryContainer = IvoryText,
    tertiary = EmeraldReady,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = IvoryText,
    surface = CarbonSurface,
    onSurface = IvoryText,
    surfaceVariant = CarbonElevated,
    onSurfaceVariant = AshMuted,
    surfaceContainerLowest = ObsidianBlack,
    surfaceContainerLow = CarbonSurface,
    surfaceContainer = CarbonElevated,
    surfaceContainerHigh = Color(0xFF221A1D),
    surfaceContainerHighest = Color(0xFF2B2023),
    outline = CarbonBorder,
    outlineVariant = Color(0xFF261418),
    error = ScarletGlow,
    onError = ObsidianBlack
)

@Composable
fun FFTurboTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = RedBlackPremiumColorScheme,
        typography = FFTurboTypography,
        content = content
    )
}
