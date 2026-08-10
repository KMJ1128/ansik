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
}

val DayColorPalette = listOf(
    Color(0xFFEF4C4C),
    Color(0xFFFF8A3D),
    Color(0xFFFFC93D),
    Color(0xFF4CC97A),
    Color(0xFF33B0E0),
    Color(0xFF6C7BE0),
    Color(0xFFB05CE0),
    Color(0xFFE85BA6),
    Color(0xFFA97155),
    Color(0xFF6E8494)
)

fun getDayColor(day: Int): Color {
    if (day < 1) return Color.Gray

    val index = (day - 1) % DayColorPalette.size

    return DayColorPalette[index]
}