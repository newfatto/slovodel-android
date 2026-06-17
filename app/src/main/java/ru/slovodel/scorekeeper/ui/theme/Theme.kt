package ru.slovodel.scorekeeper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF0B3D12),
    secondary = Color(0xFF546E7A),
    background = Color(0xFFFAFCFA),
    surface = Color(0xFFFFFFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF06350B),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFE6F4E7),
    secondary = Color(0xFFB0BEC5),
    background = Color(0xFF101510),
    surface = Color(0xFF171D17),
)

@Composable
fun SlovodelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
