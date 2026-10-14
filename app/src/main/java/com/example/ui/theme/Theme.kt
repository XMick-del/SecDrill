package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TacticalDarkColorScheme = darkColorScheme(
    primary = StatusCyan,
    onPrimary = TacticalBackground,
    primaryContainer = StatusCyanDim,
    onPrimaryContainer = TerminalTextPrimary,
    secondary = VerifiedGreen,
    onSecondary = TacticalBackground,
    secondaryContainer = VerifiedGreenDim,
    onSecondaryContainer = VerifiedGreen,
    tertiary = WarningAmber,
    onTertiary = TacticalBackground,
    background = TacticalBackground,
    onBackground = TerminalTextPrimary,
    surface = TacticalSurface,
    onSurface = TerminalTextPrimary,
    surfaceVariant = TacticalSurfaceElevated,
    onSurfaceVariant = TerminalTextSecondary,
    outline = TacticalBorder,
    outlineVariant = TacticalBorderHighlight,
    error = AlertRed,
    onError = TacticalBackground,
    errorContainer = AlertRedDim,
    onErrorContainer = AlertRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = Typography,
        content = content
    )
}
