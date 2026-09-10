package com.app.pose.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Accent500,
    onPrimary = SurfaceWhite,
    primaryContainer = Ink800,
    onPrimaryContainer = Accent400,
    secondary = Good400,
    onSecondary = Ink900,
    background = Ink900,
    onBackground = SurfaceWhite,
    surface = Ink800,
    onSurface = SurfaceWhite,
    surfaceVariant = Ink700,
    onSurfaceVariant = Ink300,
    outline = LineBorderDark,
    error = Bad400,
    onError = SurfaceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Accent500,
    onPrimary = SurfaceWhite,
    primaryContainer = Accent50,
    onPrimaryContainer = Accent700,
    secondary = Good500,
    onSecondary = SurfaceWhite,
    background = CanvasBackground,
    onBackground = Ink900,
    surface = SurfaceWhite,
    onSurface = Ink900,
    surfaceVariant = CanvasBackground,
    onSurfaceVariant = MutedText,
    outline = LineBorder,
    error = Bad500,
    onError = SurfaceWhite
)

@Composable
fun PoseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}