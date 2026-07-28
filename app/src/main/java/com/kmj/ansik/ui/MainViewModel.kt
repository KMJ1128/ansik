package com.kmj.ansik.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.UUID

// 장소 상세 정보 데이터 클래스
data class PlaceInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val tag: String,
    val imageUrl: String
)

class MainViewModel : ViewModel() {
    // 1. 건강/식단 프로필 상태
    var selectedConditions = mutableStateOf(setOf<String>())
        private set

    // 2. 가변적인 여행 동선 리스트 (카드 리스트)
    val travelRoute = mutableStateListOf<PlaceInfo>()

    // 3. 몇 박 몇 일 상태
    var nights = mutableStateOf(3)
    var days = mutableStateOf(4)

    // 4. 검색 관련 상태
    var searchQuery = mutableStateOf("")
    var isSearchActive = mutableStateOf(false)
    var selectedPlace = mutableStateOf<PlaceInfo?>(null)

    // 추천 검색어 모음 (더미 데이터)
    val recommendedPlaces = listOf(
        PlaceInfo(name = "강릉 짬뽕순두부 동화가든", address = "강원 강릉시 초당순두부길77번길 15", tag = "저나트륨 옵션", imageUrl = "https://images.unsplash.com/photo-1588168333986-5078d3ae3976?w=400"),
        PlaceInfo(name = "테라로사 커피공장", address = "강원 강릉시 구정면 현천길 25", tag = "비건 디저트", imageUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"),
        PlaceInfo(name = "경포대 해수욕장", address = "강원 강릉시 안현동 산1-1", tag = "관광지", imageUrl = "https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?w=400")
    )

    fun toggleCondition(condition: String) {
        val current = selectedConditions.value
        selectedConditions.value = if (current.contains(condition)) current - condition else current + condition
    }

    // 장소 추가
    fun addPlaceToRoute(place: PlaceInfo) {
        if (!travelRoute.any { it.name == place.name }) {
            travelRoute.add(place)
        }
        selectedPlace.value = null // 추가 후 카드 닫기
        searchQuery.value = ""
        isSearchActive.value = false
    }

    fun removePlace(place: PlaceInfo) {
        travelRoute.remove(place)
    }

    // 🚀 드래그 앤 드롭 순서 변경 함수
    fun movePlace(fromIndex: Int, toIndex: Int) {
        val item = travelRoute.removeAt(fromIndex)
        travelRoute.add(toIndex, item)
    }
}