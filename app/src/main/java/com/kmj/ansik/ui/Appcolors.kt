package com.kmj.ansik.ui

import androidx.compose.ui.graphics.Color

// =========================================================================
// 🎨 앱 전역 컬러 팔레트 (플랫 모던 스타일 — 그라데이션 없이 단색 위주)
// =========================================================================
object AppColors {
    val PrimaryDark = Color(0xFF0A6E60)
    val Primary = Color(0xFF0FA98E)
    val PrimaryLight = Color(0xFF6EE7C9)
    val PrimarySoft = Color(0xFFE6F3F0) // Primary의 아주 옅은 배경 톤

    val Accent = Color(0xFFFF6B5B)
    val AccentLight = Color(0xFFFF8A65)

    val Background = Color(0xFFFAFAF6)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFF2F3EE)

    val TextPrimary = Color(0xFF1C1F1E)
    val TextSecondary = Color(0xFF7A8079)
    val Divider = Color(0xFFEAEBE6)
}

// 무지개 순서 Day 색상 팔레트 (조금 더 채도 높은 모던 톤)
val DayColorPalette = listOf(
    Color(0xFFEF4C4C), // 1일차
    Color(0xFFFF8A3D), // 2일차
    Color(0xFFFFC93D), // 3일차
    Color(0xFF4CC97A), // 4일차
    Color(0xFF33B0E0), // 5일차
    Color(0xFF6C7BE0), // 6일차
    Color(0xFFB05CE0), // 7일차
    Color(0xFFE85BA6), // 8일차
    Color(0xFFA97155), // 9일차
    Color(0xFF6E8494)  // 10일차
)

fun getDayColor(day: Int): Color {
    if (day < 1) return Color.Gray
    val index = (day - 1) % DayColorPalette.size
    return DayColorPalette[index]
}