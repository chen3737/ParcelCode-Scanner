package com.mashangqujian.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

// ==================== 方案一：简约蓝色方案配色 ====================

private val BlueLightColorScheme = lightColorScheme(
    primary = IOSBluePrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = IOSBlueSecondary,
    onPrimaryContainer = TextPrimary,
    
    secondary = IOSGreen,
    onSecondary = TextOnPrimary,
    secondaryContainer = IOSGreen.copy(alpha = 0.1f),
    onSecondaryContainer = IOSGreen,
    
    tertiary = IOSPurple,
    onTertiary = TextOnPrimary,
    tertiaryContainer = IOSPurple.copy(alpha = 0.1f),
    onTertiaryContainer = IOSPurple,
    
    background = BackgroundWhite,
    onBackground = TextPrimary,
    
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    
    outline = TextTertiary,
    outlineVariant = TextTertiary.copy(alpha = 0.5f),
    
    error = IOSRed,
    onError = TextOnPrimary,
    errorContainer = IOSRed.copy(alpha = 0.1f),
    onErrorContainer = IOSRed
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = IOSBluePrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = IOSBlueSecondary,
    onPrimaryContainer = TextOnDark,
    
    secondary = IOSGreen,
    onSecondary = TextOnPrimary,
    secondaryContainer = IOSGreen.copy(alpha = 0.2f),
    onSecondaryContainer = IOSGreen,
    
    tertiary = IOSPurple,
    onTertiary = TextOnPrimary,
    tertiaryContainer = IOSPurple.copy(alpha = 0.2f),
    onTertiaryContainer = IOSPurple,
    
    background = SurfaceDark,
    onBackground = TextOnDark,
    
    surface = SurfaceDark,
    onSurface = TextOnDark,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = TextTertiary,
    
    outline = TextTertiary,
    outlineVariant = TextTertiary.copy(alpha = 0.3f),
    
    error = IOSRed,
    onError = TextOnPrimary,
    errorContainer = IOSRed.copy(alpha = 0.2f),
    onErrorContainer = IOSRed
)

// ==================== 方案二：清新绿色方案配色 ====================

private val GreenLightColorScheme = lightColorScheme(
    primary = IOSGreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = IOSGreenLight,
    onPrimaryContainer = TextPrimary,
    
    secondary = IOSBlue,
    onSecondary = TextOnPrimary,
    secondaryContainer = IOSBlue.copy(alpha = 0.1f),
    onSecondaryContainer = IOSBlue,
    
    tertiary = IOSOrangeAlt,
    onTertiary = TextOnPrimary,
    tertiaryContainer = IOSOrangeAlt.copy(alpha = 0.1f),
    onTertiaryContainer = IOSOrangeAlt,
    
    background = BackgroundWhiteAlt,
    onBackground = TextPrimary,
    
    surface = SurfaceWhiteAlt,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceOffWhite,
    onSurfaceVariant = TextSecondary,
    
    outline = TextTertiary,
    outlineVariant = TextTertiary.copy(alpha = 0.5f),
    
    error = IOSRedAlt,
    onError = TextOnPrimary,
    errorContainer = IOSRedAlt.copy(alpha = 0.1f),
    onErrorContainer = IOSRedAlt
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = IOSGreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = IOSGreenSecondary,
    onPrimaryContainer = TextOnDark,
    
    secondary = IOSBlue,
    onSecondary = TextOnPrimary,
    secondaryContainer = IOSBlue.copy(alpha = 0.2f),
    onSecondaryContainer = IOSBlue,
    
    tertiary = IOSOrangeAlt,
    onTertiary = TextOnPrimary,
    tertiaryContainer = IOSOrangeAlt.copy(alpha = 0.2f),
    onTertiaryContainer = IOSOrangeAlt,
    
    background = SurfaceDark,
    onBackground = TextOnDark,
    
    surface = SurfaceDark,
    onSurface = TextOnDark,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = TextTertiary,
    
    outline = TextTertiary,
    outlineVariant = TextTertiary.copy(alpha = 0.3f),
    
    error = IOSRedAlt,
    onError = TextOnPrimary,
    errorContainer = IOSRedAlt.copy(alpha = 0.2f),
    onErrorContainer = IOSRedAlt
)

// ==================== 主题选择 ====================

/**
 * 默认主题 - 使用简约蓝色方案
 */
@Composable
fun MashangqujianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useGreenScheme: Boolean = false, // 是否使用绿色方案
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useGreenScheme && darkTheme -> GreenDarkColorScheme
        useGreenScheme -> GreenLightColorScheme
        darkTheme -> BlueDarkColorScheme
        else -> BlueLightColorScheme
    }

    // iOS 液态风格：增加背景模糊效果
    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * 蓝色方案主题（简约iOS风格）
 */
@Composable
fun MashangqujianBlueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        BlueDarkColorScheme
    } else {
        BlueLightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * 绿色方案主题（清新健康风格）
 */
@Composable
fun MashangqujianGreenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        GreenDarkColorScheme
    } else {
        GreenLightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// 应用排版定义
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)

// 预览主题包装器

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LightThemePreview() {
    MashangqujianTheme(darkTheme = false) {
        // 这里可以放置预览组件
        // 例如: EmptyState()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DarkThemePreview() {
    MashangqujianTheme(darkTheme = true) {
        // 这里可以放置预览组件
        // 例如: EmptyState()
    }
}
