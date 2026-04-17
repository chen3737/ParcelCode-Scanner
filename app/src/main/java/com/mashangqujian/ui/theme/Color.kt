package com.mashangqujian.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== 方案一：简约蓝色方案 (iOS系统风格) ====================

// 主要颜色
val IOSBluePrimary = Color(0xFF007AFF)
val IOSBlueSecondary = Color(0xFF5AC8FA)
val IOSGreen = Color(0xFF34C759)
val IOSOrange = Color(0xFFFF9500)
val IOSRed = Color(0xFFFF3B30)
val IOSPurple = Color(0xFFAF52DE)
val IOSYellow = Color(0xFFFFCC00)

// 背景和表面颜色
val BackgroundWhite = Color(0xFFFFFFFF)
val BackgroundLightGray = Color(0xFFF5F5F7)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFF2F2F7)
val SurfaceDark = Color(0xFF1C1C1E)

// 文字颜色
val TextPrimary = Color(0xFF1D1D1F)
val TextSecondary = Color(0xFF8E8E93)
val TextTertiary = Color(0xFFC7C7CC)
val TextOnPrimary = Color(0xFFFFFFFF)
val TextOnDark = Color(0xFFFFFFFF)

// 状态颜色
val SuccessGreen = Color(0xFF34C759)
val WarningOrange = Color(0xFFFF9500)
val ErrorRed = Color(0xFFFF3B30)
val InfoBlue = Color(0xFF007AFF)

// ==================== 方案二：清新绿色方案 (iOS健康应用风格) ====================

// 主要颜色
val IOSGreenPrimary = Color(0xFF34C759)
val IOSGreenSecondary = Color(0xFF30D158)
val IOSGreenLight = Color(0xFF4CD964)
val IOSBlue = Color(0xFF007AFF)
val IOSOrangeAlt = Color(0xFFFF9500)
val IOSRedAlt = Color(0xFFFF3B30)
val IOSPurpleAlt = Color(0xFFAF52DE)

// 背景颜色（绿色方案）
val BackgroundWhiteAlt = Color(0xFFFFFFFF)
val BackgroundOffWhite = Color(0xFFF8F8F8)
val SurfaceWhiteAlt = Color(0xFFFFFFFF)
val SurfaceOffWhite = Color(0xFFF2F2F2)

// ==================== 传统颜色（兼容现有代码） ====================

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ==================== 快递公司颜色 ====================

// 快递公司颜色映射
val CompanyColorMap = mapOf(
    "顺丰" to Color(0xFF2D7CF2),
    "京东" to Color(0xFFE1251B),
    "中通" to Color(0xFF2AA344),
    "圆通" to Color(0xFF00A0E9),
    "韵达" to Color(0xFF0070C0),
    "菜鸟驿站" to Color(0xFFF15A22),
    "邮政" to Color(0xFF0066CC)
)

// ==================== 便捷颜色变量（使用方案一作为默认） ====================

val PrimaryColor = IOSBluePrimary
val SecondaryColor = IOSBlueSecondary
val BackgroundColor = BackgroundWhite
val CardColor = SurfaceWhite
val TextColor = TextPrimary
val SubTextColor = TextSecondary
val ErrorColor = IOSRed
val SuccessColor = IOSGreen
val WarningColor = IOSOrange
