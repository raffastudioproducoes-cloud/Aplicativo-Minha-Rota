package com.raffastudioproducoes.minharota.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography // CORREÇÃO: Import correto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RoxoPrimary,
    secondary = VerdeEntrada,
    tertiary = ElectricBlue,
    background = FundoDark,
    surface = SurfaceDark,
    onPrimary = TextoPrimary,
    onSecondary = FundoDark,
    onTertiary = TextoPrimary,
    onBackground = TextoPrimary,
    onSurface = TextoPrimary,
    primaryContainer = TealAccent,
    secondaryContainer = VerdeEntrada.copy(alpha = 0.2f),
    tertiaryContainer = ElectricBlue.copy(alpha = 0.2f),
)

private val LightColorScheme = lightColorScheme(
    primary = RoxoPrimary,
    secondary = VerdeEntrada,
    tertiary = ElectricBlue,
    background = FundoLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextoPrimaryLight,
    onSurface = TextoPrimaryLight,
    primaryContainer = TealAccent.copy(alpha = 0.1f),
    secondaryContainer = VerdeEntrada.copy(alpha = 0.1f),
    tertiaryContainer = ElectricBlue.copy(alpha = 0.1f),
)

@Composable
fun MinhaRotaTema(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(), // Garante o uso da tipografia correta
        content = content
    )
}
