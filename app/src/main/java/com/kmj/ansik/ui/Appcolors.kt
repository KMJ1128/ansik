package com.kmj.ansik.ui

import androidx.compose.ui.graphics.Color

object AppColors {

    val PrimaryDark = Color(0xFF194E49)
    val Primary = Color(0xFF28766C)
    val PrimaryLight = Color(0xFF8BCABB)
    val PrimarySoft = Color(0xFFE1F1E9)

    val Accent = Color(0xFFF2B75D)
    val AccentLight = Color(0xFFFFE4AE)

    val Background = Color(0xFFFFF9F0)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFF2EEE8)

    val TextPrimary = Color(0xFF272A43)
    val TextSecondary = Color(0xFF626477)
    val Divider = Color(0xFFE7E1D8)
    val BorderStrong = Color(0xFFD2CBBD)

    val Success = Primary
    val SuccessDark = PrimaryDark
    val SuccessSoft = PrimarySoft

    val Info = Color(0xFF6857A6)
    val InfoDark = Color(0xFF483A80)

    val Warning = Color(0xFFF2B75D)

    val Danger = Color(0xFFB9473C)
    val DangerDark = Color(0xFF8E332C)
}

private val DayColorPalette = listOf(
    Color(0xFFE53935),
    Color(0xFFFF7043),
    Color(0xFFFFCA28),
    Color(0xFF43A047),
    Color(0xFF1E88E5),
    Color(0xFF3949AB),
    Color(0xFF8E24AA),
    Color(0xFFD81B60),
    Color(0xFF6D4C41),
    Color(0xFF546E7A)
)

fun getDayColor(day: Int): Color {
    if (day < 1) {
        return Color.Gray
    }

    return DayColorPalette[
        (day - 1) % DayColorPalette.size
    ]
}
