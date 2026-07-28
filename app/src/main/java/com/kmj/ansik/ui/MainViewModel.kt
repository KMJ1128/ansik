package com.kmj.ansik.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class PlaceInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val tag: String,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double
)

class MainViewModel : ViewModel() {
    var selectedConditions = mutableStateOf(setOf<String>())
        private set

    val travelRoute = mutableStateListOf<PlaceInfo>()

    // 더미 데이터 대신 빈 리스트로 시작 (API로 채울 예정)
    var recommendedPlaces = mutableStateListOf<PlaceInfo>()

    var nights = mutableStateOf(3)
    var days = mutableStateOf(4)

    var searchQuery = mutableStateOf("")
    var isSearchActive = mutableStateOf(false)
    var selectedPlace = mutableStateOf<PlaceInfo?>(null)

    // 사용자가 타이핑을 너무 빨리할 때 서버 폭주를 막기 위한 타이머 변수
    private var searchJob: Job? = null

    // 🚀 실시간 검색 API 호출 함수
    fun searchPlacesRealtime(query: String) {
        searchQuery.value = query
        isSearchActive.value = query.isNotEmpty()

        if (query.isBlank()) {
            recommendedPlaces.clear()
            return
        }

        // Debounce: 사용자가 타이핑을 멈추고 0.5초가 지나면 API를 호출합니다.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            try {
                // Network.kt에 만든 카카오 API 호출
                val response = RetrofitClient.api.searchPlace(query = query)
                recommendedPlaces.clear()

                // 받아온 결과를 우리 앱 형식(PlaceInfo)에 맞게 변환해서 리스트에 넣음
                recommendedPlaces.addAll(
                    response.documents.map {
                        PlaceInfo(
                            name = it.place_name,
                            address = it.road_address_name.ifEmpty { "주소 없음" },
                            tag = it.category_group_name.ifEmpty { "장소" },
                            // 카카오 API는 이미지를 제공하지 않아 임시 이미지 적용
                            imageUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400",
                            latitude = it.y.toDouble(),
                            longitude = it.x.toDouble()
                        )
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace() // 에러 발생 시 무시
            }
        }
    }

    fun toggleCondition(condition: String) {
        val current = selectedConditions.value
        selectedConditions.value = if (current.contains(condition)) current - condition else current + condition
    }

    fun addPlaceToRoute(place: PlaceInfo) {
        if (!travelRoute.any { it.name == place.name }) travelRoute.add(place)
        selectedPlace.value = null
        searchQuery.value = ""
        isSearchActive.value = false
    }

    fun removePlace(place: PlaceInfo) {
        travelRoute.remove(place)
    }

    fun movePlace(fromIndex: Int, toIndex: Int) {
        val item = travelRoute.removeAt(fromIndex)
        travelRoute.add(toIndex, item)
    }
}