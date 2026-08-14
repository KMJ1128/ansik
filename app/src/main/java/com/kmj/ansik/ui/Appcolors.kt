package com.kmj.ansik.ui

import androidx.compose.ui.graphics.Color

object AppColors {

    val PrimaryDark = Color(0xFF0A6E60)
    val Primary = Color(0xFF0FA98E)
    val PrimaryLight = Color(0xFF6EE7C9)
    val PrimarySoft = Color(0xFFE6F3F0)

    val Accent = Color(0xFFFF6B5B)
    val AccentLight = Color(0xFFFF8A65)

    val Background = Color(0xFFFAFAF6)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFF2F3EE)

    val TextPrimary = Color(0xFF1C1F1E)
    val TextSecondary = Color(0xFF7A8079)
    val Divider = Color(0xFFEAEBE6)

    val Success = Color(0xFF2E7D32)
    val SuccessSoft = Color(0xFFE8F5E9)

    val Info = Color(0xFF1976D2)

    val Warning = Color(0xFFFBC02D)

    val Danger = Color(0xFFE53935)
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