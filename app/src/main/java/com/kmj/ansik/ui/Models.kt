package com.kmj.ansik.ui

import java.util.UUID



// 가변적인 여행 장소를 담을 데이터 클래스
data class TravelSpot(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)