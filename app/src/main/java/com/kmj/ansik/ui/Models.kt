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