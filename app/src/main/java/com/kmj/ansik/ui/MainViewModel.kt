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

    // 🚀 실시간 검색 API 호출 함수 (디버깅용 로그 추가)
    fun searchPlacesRealtime(query: String) {
        searchQuery.value = query
        isSearchActive.value = query.isNotEmpty()

        if (query.isBlank()) {
            recommendedPlaces.clear()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            try {
                android.util.Log.d("KakaoSearch", "서버로 검색 요청 날아감: $query")

                val response = RetrofitClient.api.searchPlace(query = query)

                android.util.Log.d("KakaoSearch", "검색 성공! 찾은 개수: ${response.documents.size}개")

                recommendedPlaces.clear()
                recommendedPlaces.addAll(
                    response.documents.map {
                        PlaceInfo(
                            name = it.place_name,
                            address = it.road_address_name.ifEmpty { "주소 없음" },
                            tag = it.category_group_name.ifEmpty { "장소" },
                            imageUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400",
                            latitude = it.y.toDoubleOrNull() ?: 0.0,
                            longitude = it.x.toDoubleOrNull() ?: 0.0
                        )
                    }
                )
            } catch (e: Exception) {
                // 🚨 검색이 안 되는 진짜 이유를 로그캣에 빨간색으로 출력!
                android.util.Log.e("KakaoSearch", "검색 통신 💥대실패💥 원인: ${e.message}")
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