package com.chinapost.postage.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PostGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PostGreenContainer,
    onPrimaryContainer = PostOnGreenContainer,
    secondary = PostYellow,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = PostYellowContainer,
    onSecondaryContainer = PostOnYellowContainer,
    background = SurfaceLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PostGreenLight,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = PostGreenDark,
    onPrimaryContainer = PostGreenContainer,
    secondary = PostYellow,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = PostOnYellowContainer,
    onSecondaryContainer = PostYellowContainer,
    background = SurfaceDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

@Composable
fun ChinaPostCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
