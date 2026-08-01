package com.kmj.ansik.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    var selectedConditions = mutableStateOf(setOf<String>())
        private set

    val travelRoute = mutableStateListOf<PlaceInfo>()
    var recommendedPlaces = mutableStateListOf<PlaceInfo>()

    var nights = mutableStateOf(3)
    var days = mutableStateOf(4)

    var currentSelectedDay = mutableStateOf(1)

    var searchQuery = mutableStateOf("")
    var isSearchActive = mutableStateOf(false)
    var selectedPlace = mutableStateOf<PlaceInfo?>(null)

    private var searchJob: Job? = null

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
                val placesWithImages = withContext(Dispatchers.IO) {
                    val response = RetrofitClient.api.searchPlace(query = query)

                    coroutineScope {
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
                }

                recommendedPlaces.clear()
                recommendedPlaces.addAll(placesWithImages)

            } catch (e: Exception) {
                android.util.Log.e("KakaoSearch", "검색 통신 실패 원인: ${e.message}")
            }
        }
    }

    fun selectLocationFromMap(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val newPlace = withContext(Dispatchers.IO) {
                    val searchRes = RetrofitClient.api.searchPlace(query = name)
                    val matchedPlace = searchRes.documents.firstOrNull()

                    val imageRes = RetrofitClient.api.searchImage(query = name)
                    val imageUrl = imageRes.documents.firstOrNull()?.image_url
                        ?: "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"

                    val shortTag = matchedPlace?.category_group_name?.split(">")?.last()?.trim() ?: "관심 위치"
                    val address = matchedPlace?.road_address_name?.ifEmpty { "주소 없음" } ?: "지도에서 선택한 장소"

                    PlaceInfo(
                        name = name,
                        address = address,
                        tag = shortTag,
                        imageUrl = imageUrl,
                        latitude = lat,
                        longitude = lng,
                        day = currentSelectedDay.value
                    )
                }

                selectedPlace.value = newPlace
                isSearchActive.value = false
                searchQuery.value = ""
            } catch (e: Exception) {
                android.util.Log.e("MapClick", "지도 심볼 통신 실패: ${e.message}")
            }
        }
    }

    fun changePlaceDay(place: PlaceInfo, newDay: Int) {
        val index = travelRoute.indexOfFirst { it.id == place.id }
        if (index != -1) {
            travelRoute[index] = travelRoute[index].copy(day = newDay)
            currentSelectedDay.value = newDay
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

    fun addPlaceToRoute(place: PlaceInfo) {
        if (!travelRoute.any { it.name == place.name }) {
            travelRoute.add(place.copy(day = currentSelectedDay.value))
            travelRoute.sortBy { it.day }
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

    // 🚀 [수정 완료]: 일정을 꼬이게 만들었던 강제 상단 정렬 함수(movePlaceToTop) 삭제!
}