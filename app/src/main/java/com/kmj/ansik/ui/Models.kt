package com.kmj.ansik.ui

import java.util.UUID

// 건강 조건 데이터
enum class HealthCondition(val label: String) {
    DIABETES("당뇨 관리 (저당)"),
    HYPERTENSION("고혈압 관리 (저나트륨)")
}

// 가변적인 여행 장소를 담을 데이터 클래스
data class TravelSpot(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

// 🚀 장소 정보 데이터 클래스 (day 속성 추가)
data class PlaceInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val tag: String,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double,
    var day: Int = 1 // 🚀 몇 째날 일정인지 구분 (기본값 1일차)
)