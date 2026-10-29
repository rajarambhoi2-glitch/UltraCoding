package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkCodeForgeColorScheme = darkColorScheme(
    primary = VsCodeBlueLight,
    onPrimary = Color.Black,
    primaryContainer = VsCodeBlue,
    onPrimaryContainer = Color.White,
    secondary = VsCodeCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E3A3A),
    onSecondaryContainer = VsCodeCyan,
    tertiary = VsCodeYellow,
    onTertiary = Color.Black,
    background = VsCodeBg,
    onBackground = Color(0xFFCCCCCC),
    surface = VsCodeSurface,
    onSurface = Color(0xFFD4D4D4),
    surfaceVariant = VsCodeTabInactive,
    onSurfaceVariant = Color(0xFF969696),
    outline = VsCodeBorder,
    error = DiagnosticError,
    onError = Color.White
)

val LightCodeForgeColorScheme = lightColorScheme(
    primary = VsCodeBlue,
    onPrimary = Color.White,
    secondary = Color(0xFF005FB8),
    onSecondary = Color.White,
    background = VsCodeLightBg,
    onBackground = Color(0xFF1E1E1E),
    surface = VsCodeLightSurface,
    onSurface = Color(0xFF1E1E1E),
    surfaceVariant = VsCodeLightSidebar,
    onSurfaceVariant = Color(0xFF616161),
    outline = VsCodeLightBorder,
    error = DiagnosticError,
    onError = Color.White
)

@Composable
fun CodeForgeTheme(
    darkTheme: Boolean = true, // Default to VS Code Dark+
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkCodeForgeColorScheme else LightCodeForgeColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CodeForgeTheme(darkTheme = darkTheme, content = content)
}
