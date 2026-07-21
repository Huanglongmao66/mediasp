package com.mpvp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mpvp.model.ThemeColor
import com.mpvp.model.ThemeMode

/**
 * 亮色主题颜色方案（默认蓝色）
 */
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFFFF5722),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFCCBC),
    onSecondaryContainer = Color(0xFFBF360C),
    tertiary = Color(0xFF26A69A),
    onTertiary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0)
)

/**
 * 暗色主题颜色方案（默认蓝色）
 */
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFFFFAB91),
    onSecondary = Color(0xFFBF360C),
    secondaryContainer = Color(0xFFD84315),
    onSecondaryContainer = Color(0xFFFFCCBC),
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF004D40),
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFEF5350),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color(0xFFFFCDD2),
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF616161)
)

/**
 * 本地主题模式提供者
 */
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

/**
 * 本地主题颜色提供者
 */
val LocalThemeColor = staticCompositionLocalOf { ThemeColor.BLUE }

/**
 * 根据主题颜色生成亮色方案
 */
fun buildLightColorScheme(themeColor: ThemeColor): ColorScheme {
    val primary = Color(themeColor.lightPrimary)
    return LightColorScheme.copy(
        primary = primary,
        primaryContainer = primary.copy(alpha = 0.15f).compositeOver(Color.White),
        onPrimaryContainer = primary,
        secondary = primary.copy(alpha = 0.8f),
        tertiary = primary.copy(alpha = 0.6f)
    )
}

/**
 * 根据主题颜色生成暗色方案
 */
fun buildDarkColorScheme(themeColor: ThemeColor): ColorScheme {
    val primary = Color(themeColor.darkPrimary)
    return DarkColorScheme.copy(
        primary = primary,
        primaryContainer = primary.copy(alpha = 0.25f).compositeOver(Color(0xFF1E1E1E)),
        secondary = primary.copy(alpha = 0.7f),
        tertiary = primary.copy(alpha = 0.5f)
    )
}

/**
 * 颜色混合辅助函数
 */
private fun Color.compositeOver(background: Color): Color {
    val alpha = this.alpha
    if (alpha == 1f) return this
    val a = alpha + background.alpha * (1f - alpha)
    if (a == 0f) return Color.Transparent
    val r = (this.red * alpha + background.red * background.alpha * (1f - alpha)) / a
    val g = (this.green * alpha + background.green * background.alpha * (1f - alpha)) / a
    val b = (this.blue * alpha + background.blue * background.alpha * (1f - alpha)) / a
    return Color(r, g, b, a)
}

/**
 * 应用主题组件
 *
 * 根据主题模式和主题颜色配置提供对应的颜色方案
 *
 * @param themeMode 主题模式（亮色/暗色/跟随系统）
 * @param themeColor 主题颜色（8种预设颜色）
 * @param content 子组件内容
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themeColor: ThemeColor = ThemeColor.BLUE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) buildDarkColorScheme(themeColor) else buildLightColorScheme(themeColor)

    CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalThemeColor provides themeColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
