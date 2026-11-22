package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VyroDarkColorScheme = darkColorScheme(
    primary = VyroElectricPurple,
    onPrimary = Color.White,
    primaryContainer = VyroPurpleContainer,
    onPrimaryContainer = VyroOnPurpleContainer,
    secondary = VyroNeonBlue,
    onSecondary = VyroDeepBlack,
    secondaryContainer = VyroBlueContainer,
    onSecondaryContainer = VyroOnBlueContainer,
    tertiary = VyroElectricPurpleBright,
    onTertiary = Color.White,
    background = VyroDeepBlack,
    onBackground = VyroTextPrimary,
    surface = VyroObsidian,
    onSurface = VyroTextPrimary,
    surfaceVariant = VyroGraphite,
    onSurfaceVariant = VyroTextSecondary,
    surfaceContainerLowest = VyroDeepBlack,
    surfaceContainerLow = VyroObsidian,
    surfaceContainer = VyroGraphite,
    surfaceContainerHigh = VyroGraphiteElevated,
    surfaceContainerHighest = VyroGraphiteHighlight,
    outline = VyroBorderSubtle,
    outlineVariant = Color(0xFF1F2335),
    error = VyroRose,
    onError = Color.White
)

@Composable
fun VyroStudioTheme(
    isArabic: Boolean = false,
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
        colorScheme = VyroDarkColorScheme,
        typography = vyroTypography(isArabic = isArabic),
        shapes = VyroShapes,
        content = content
    )
}
