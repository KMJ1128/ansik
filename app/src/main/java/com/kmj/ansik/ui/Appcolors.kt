package com.kmj.ansik.ui

import androidx.compose.ui.graphics.Color

object AppColors {

    val PrimaryDark = Color(0xFF46A302)
    val Primary = Color(0xFF58CC02)
    val PrimaryLight = Color(0xFF89E219)
    val PrimarySoft = Color(0xFFE8F7D5)

    val Accent = Color(0xFFFFC800)
    val AccentLight = Color(0xFFFFD900)

    val Background = Color(0xFFF7F7F7)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFF1F1F1)

    val TextPrimary = Color(0xFF4B4B4B)
    val TextSecondary = Color(0xFF777777)
    val Divider = Color(0xFFE5E5E5)
    val BorderStrong = Color(0xFFD7D7D7)

    val Success = Primary
    val SuccessDark = PrimaryDark
    val SuccessSoft = PrimarySoft

    val Info = Color(0xFF1CB0F6)
    val InfoDark = Color(0xFF1899D6)

    val Warning = Color(0xFFFFC800)

    val Danger = Color(0xFFFF4B4B)
    val DangerDark = Color(0xFFEA2B2B)
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
