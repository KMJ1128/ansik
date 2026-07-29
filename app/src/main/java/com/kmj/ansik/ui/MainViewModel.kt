package com.kmj.ansik.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    var selectedConditions = mutableStateOf(setOf<String>())
        private set

    val travelRoute = mutableStateListOf<PlaceInfo>()
    var recommendedPlaces = mutableStateListOf<PlaceInfo>()

    var nights = mutableStateOf(3)
    var days = mutableStateOf(4)

    // 사용자가 마지막으로 선택/작업 중인 Day를 기억 (기본값 1일차)
    var currentSelectedDay = mutableStateOf(1)

    var searchQuery = mutableStateOf("")
    var isSearchActive = mutableStateOf(false)
    var selectedPlace = mutableStateOf<PlaceInfo?>(null)

    private var searchJob: Job? = null

    // 실시간 검색
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
                                longitude = place.x.toDoubleOrNull() ?: 0.0,
                                day = currentSelectedDay.value
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

    // 지도 심볼 터치 시 선택 장소 정보 로드
    fun selectLocationFromMap(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val searchRes = RetrofitClient.api.searchPlace(query = name)
                val matchedPlace = searchRes.documents.firstOrNull()

                val imageRes = RetrofitClient.api.searchImage(query = name)
                val imageUrl = imageRes.documents.firstOrNull()?.image_url
                    ?: "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"

                val shortTag = matchedPlace?.category_group_name?.split(">")?.last()?.trim() ?: "관심 위치"
                val address = matchedPlace?.road_address_name?.ifEmpty { "주소 없음" } ?: "지도에서 선택한 장소"

                selectedPlace.value = PlaceInfo(
                    name = name,
                    address = address,
                    tag = shortTag,
                    imageUrl = imageUrl,
                    latitude = lat,
                    longitude = lng,
                    day = currentSelectedDay.value
                )

                isSearchActive.value = false
                searchQuery.value = ""
            } catch (e: Exception) {
                android.util.Log.e("MapClick", "지도 심볼 통신 실패: ${e.message}")
            }
        }
    }

    // 주변 10km 맞춤 추천 탐색
    fun searchNearbyRecommendations(centerLat: Double, centerLng: Double) {
        viewModelScope.launch {
            try {
                val customKeyword = when {
                    selectedConditions.value.contains("비건 (엄격한 채식)") -> "채식 식당"
                    selectedConditions.value.contains("당뇨 관리 (저당)") -> "건강식당"
                    else -> "맛집"
                }

                android.util.Log.d("NearbySearch", "주변 10km 맞춤 탐색 시작: 키워드=[$customKeyword], 좌표=($centerLat, $centerLng)")
                val response = RetrofitClient.api.searchPlace(query = customKeyword)
            } catch (e: Exception) {
                android.util.Log.e("NearbySearch", "주변 탐색 실패: ${e.message}")
            }
        }
    }

    // 🚀 [수정 핵심]: 장소의 Day를 바꿀 때 해당 Day로 값을 변경한 후, Day 순서대로 리스트를 즉시 자동 정렬!
    fun changePlaceDay(place: PlaceInfo, newDay: Int) {
        val index = travelRoute.indexOfFirst { it.id == place.id }
        if (index != -1) {
            travelRoute[index] = travelRoute[index].copy(day = newDay)
            currentSelectedDay.value = newDay // 다음 추가 일정을 위해 현재 Day 기억

            // 🚀 Day 변경 즉시 1일차 -> 2일차 -> 3일차 순으로 자동 이동 및 정렬
            travelRoute.sortBy { it.day }
        }
    }

    fun getRouteCoordsForDay(day: Int): List<LatLng> {
        return travelRoute
            .filter { it.day == day }
            .map { LatLng(it.latitude, it.longitude) }
    }

    fun toggleCondition(condition: String) {
        val current = selectedConditions.value
        selectedConditions.value = if (current.contains(condition)) current - condition else current + condition
    }

    // 새로 일정을 추가할 때 마지막으로 선택했던 day로 추가하고 정렬 유지
    fun addPlaceToRoute(place: PlaceInfo) {
        if (!travelRoute.any { it.name == place.name }) {
            travelRoute.add(place.copy(day = currentSelectedDay.value))
            travelRoute.sortBy { it.day } // 추가 후에도 날짜순 정렬 유지
        }
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

    fun movePlaceToTop(targetPlace: PlaceInfo) {
        val currentIndex = travelRoute.indexOfFirst { it.id == targetPlace.id }
        if (currentIndex > 0) {
            val item = travelRoute.removeAt(currentIndex)
            // 날짜(Day) 정렬이 깨지지 않도록 동일한 Day 그룹의 가장 첫 번째 위치로 삽입
            val firstIndexOfSameDay = travelRoute.indexOfFirst { it.day == item.day }
            if (firstIndexOfSameDay != -1) {
                travelRoute.add(firstIndexOfSameDay, item)
            } else {
                travelRoute.add(0, item)
            }
        }
    }
}