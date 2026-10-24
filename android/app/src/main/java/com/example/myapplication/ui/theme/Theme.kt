package com.example.myapplication.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = OkanPrimary,
    onPrimary = OkanSurface,
    primaryContainer = OkanPrimaryContainer,
    onPrimaryContainer = OkanTextPrimary,
    background = OkanBackground,
    onBackground = OkanTextPrimary,
    surface = OkanSurface,
    onSurface = OkanTextPrimary,
    surfaceVariant = OkanSurfaceVariant,
    onSurfaceVariant = OkanTextSecondary,
    outline = OkanBorder,
    error = OkanDanger,
    onError = OkanSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = BlockinPurplePrimary,
    secondary = BlockinCyanSecondary,
    background = BlockinDarkBackground,
    surface = BlockinDarkSurface,
    onPrimary = BlockinTextPrimary,
    onBackground = BlockinTextPrimary,
    onSurface = BlockinTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = (if (darkTheme) BlockinDarkBackground else OkanBackground).toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}

// 互換性エイリアス
@Composable
fun BlockinTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}
