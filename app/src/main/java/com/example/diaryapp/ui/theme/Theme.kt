package com.example.diaryapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF673AB7),
    onPrimary = Color.White,
    secondary = Color(0xFFE91E63),
    tertiary = Color(0xFFFFA1C1),

    background = Color(0xFFFFF5F5),
    onBackground = Color(0xFF30243A),

    surface = Color.White,
    onSurface = Color(0xFF30243A),

    surfaceVariant = Color(0xFFFFE9F0),
    onSurfaceVariant = Color(0xFF673AB7),

    outline = Color(0xFFD8C7E8)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    secondary = Color(0xFFFFB1C8),
    onSecondary = Color(0xFF5D1135),
    tertiary = Color(0xFFFFA1C1),

    background = Color(0xFF121016),
    onBackground = Color(0xFFF5EFF7),

    surface = Color(0xFF1B1720),
    onSurface = Color(0xFFF5EFF7),

    surfaceVariant = Color(0xFF29232F),
    onSurfaceVariant = Color(0xFFE2D3F5),

    outline = Color(0xFF65566F)
)

@Composable
fun DiaryAppTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
