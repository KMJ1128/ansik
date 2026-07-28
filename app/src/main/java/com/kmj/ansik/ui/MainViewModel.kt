package com.kmj.ansik.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    var recommendedPlaces = mutableStateListOf<PlaceInfo>()

    var nights = mutableStateOf(3)
    var days = mutableStateOf(4)

    var searchQuery = mutableStateOf("")
    var isSearchActive = mutableStateOf(false)
    var selectedPlace = mutableStateOf<PlaceInfo?>(null)

    private var searchJob: Job? = null

    // 검색창에서 실시간 타이핑으로 검색
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
                val response = RetrofitClient.api.searchPlace(query = query)

                val placesWithImages = coroutineScope {
                    response.documents.map { place ->
                        async {
                            val imageUrl = try {
                                val imageRes = RetrofitClient.api.searchImage(query = place.place_name)
                                imageRes.documents.firstOrNull()?.image_url ?: "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"
                            } catch (e: Exception) {
                                "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"
                            }

                            val shortTag = place.category_group_name.split(">").last().trim()

                            PlaceInfo(
                                name = place.place_name,
                                address = place.road_address_name.ifEmpty { "주소 없음" },
                                tag = shortTag.ifEmpty { "장소" },
                                imageUrl = imageUrl,
                                latitude = place.y.toDoubleOrNull() ?: 0.0,
                                longitude = place.x.toDoubleOrNull() ?: 0.0
                            )
                        }
                    }.awaitAll()
                }

                recommendedPlaces.clear()
                recommendedPlaces.addAll(placesWithImages)

            } catch (e: Exception) {
                android.util.Log.e("KakaoSearch", "검색 통신 실패 원인: ${e.message}")
            }
        }
    }

    // 🚀 네이버 지도에서 건물/지역을 직접 클릭했을 때의 로직
    fun selectLocationFromMap(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                // 1. 클릭한 건물의 이름으로 카카오 장소 검색을 돌려 상세 주소와 태그 획득
                val searchRes = RetrofitClient.api.searchPlace(query = name)
                val matchedPlace = searchRes.documents.firstOrNull()

                // 2. 카카오 이미지 검색으로 건물의 실제 사진 획득
                val imageRes = RetrofitClient.api.searchImage(query = name)
                val imageUrl = imageRes.documents.firstOrNull()?.image_url
                    ?: "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"

                val shortTag = matchedPlace?.category_group_name?.split(">")?.last()?.trim() ?: "관심 위치"
                val address = matchedPlace?.road_address_name?.ifEmpty { "주소 없음" } ?: "지도에서 선택한 장소"

                // 3. UI(바텀 시트) 자동 팝업
                selectedPlace.value = PlaceInfo(
                    name = name,
                    address = address,
                    tag = shortTag,
                    imageUrl = imageUrl,
                    latitude = lat,
                    longitude = lng
                )

                isSearchActive.value = false
                searchQuery.value = ""
            } catch (e: Exception) {
                android.util.Log.e("MapClick", "지도 심볼 통신 실패: ${e.message}")
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