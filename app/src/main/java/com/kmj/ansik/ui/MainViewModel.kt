package com.kmj.ansik.ui

import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kmj.ansik.R
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    var selectedConditions = mutableStateOf<Set<String>>(emptySet())
        private set

    val travelRoute = mutableStateListOf<PlaceInfo>()
    val recommendedPlaces = mutableStateListOf<PlaceInfo>()

    var nights = mutableStateOf(3)
    var days = mutableStateOf(4)
    var currentSelectedDay = mutableStateOf(1)

    private val sharedPreferences = context.getSharedPreferences("AnsikPrefs", Context.MODE_PRIVATE)

    var searchRadius = mutableIntStateOf(sharedPreferences.getInt("searchRadius", 2000))
        private set

    var showReviewSheet = mutableStateOf(false)
    val selectedPlaceReviews = mutableStateListOf<BlogReview>()
    var isFetchingReviews = mutableStateOf(false)

    // 리뷰 무한 스크롤 상태 관리
    var reviewStartPage = 1
    var hasMoreReviews = mutableStateOf(true)
    var currentReviewPlaceName = ""
    var currentReviewAddress = ""

    fun fetchPlaceReviews(placeName: String, address: String, isLoadMore: Boolean = false) {
        if (isFetchingReviews.value) return

        if (!isLoadMore) {
            selectedPlaceReviews.clear()
            reviewStartPage = 1
            hasMoreReviews.value = true
            currentReviewPlaceName = placeName
            currentReviewAddress = address
            showReviewSheet.value = true
        }

        if (!hasMoreReviews.value) return

        viewModelScope.launch {
            isFetchingReviews.value = true
            try {
                val reviews = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getPlaceReviews(currentReviewPlaceName, currentReviewAddress, reviewStartPage)
                }
                if (reviews.isEmpty()) {
                    hasMoreReviews.value = false
                } else {
                    selectedPlaceReviews.addAll(reviews)
                    reviewStartPage += 5
                }
            } catch (e: Exception) {
                Log.e("Reviews", "리뷰 불러오기 실패", e)
            } finally {
                isFetchingReviews.value = false
            }
        }
    }

    private suspend fun fetchExactImages(tourId: String?, title: String?, mapX: Double?, mapY: Double?): List<String> {
        return try {
            RetrofitClient.api.getExactImages(tourId, title, mapX, mapY)
        } catch (e: Exception) {
            Log.e("ImageFetch", "이미지 요청 실패: $title", e)
            emptyList()
        }
    }

    private suspend fun getValidTourImages(urls: List<String>): List<String> {
        return withContext(Dispatchers.IO) {
            urls.filter { it.isNotBlank() }
                .map { it.replace("http://", "https://") }
                .distinct()
        }
    }

    fun updateSearchRadius(radius: Int) {
        searchRadius.intValue = radius
        sharedPreferences.edit().putInt("searchRadius", radius).apply()
    }

    fun increaseDays() {
        if (days.value < 14) {
            days.value += 1
            nights.value = days.value - 1
        }
    }

    fun decreaseDays() {
        if (days.value > 1) {
            days.value -= 1
            nights.value = days.value - 1

            travelRoute.forEachIndexed { index, place ->
                if (place.day > days.value) {
                    travelRoute[index] = place.copy(day = days.value)
                }
            }
            if (currentSelectedDay.value > days.value) {
                currentSelectedDay.value = days.value
            }
        }
    }

    var searchQuery = mutableStateOf("")
    var isSearchActive = mutableStateOf(false)
    var selectedPlace = mutableStateOf<PlaceInfo?>(null)
    private var searchJob: Job? = null

    val nearbyRestaurants = mutableStateListOf<TourRestaurant>()
    var isFetchingRestaurants = mutableStateOf(false)
    var selectedRestaurantDetail = mutableStateOf<TourRestaurantDetail?>(null)

    fun searchPlacesRealtime(query: String) {
        searchQuery.value = query
        isSearchActive.value = query.isNotEmpty()

        if (query.isBlank()) {
            recommendedPlaces.clear()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            try {
                val placesWithImages = withContext(Dispatchers.IO) {
                    val response = RetrofitClient.api.searchPlace(query = query, mapX = null, mapY = null)
                    response.documents.map { place ->
                        val shortTag = place.category_group_name.split(">").lastOrNull()?.trim().orEmpty()
                        PlaceInfo(
                            id = place.id,
                            name = place.place_name,
                            address = place.road_address_name.ifEmpty { context.getString(R.string.no_address) },
                            tag = shortTag.ifEmpty { context.getString(R.string.place) },
                            imageUrl = DEFAULT_IMAGE_URL,
                            imageUrls = listOf(DEFAULT_IMAGE_URL),
                            latitude = place.y.toDoubleOrNull() ?: 0.0,
                            longitude = place.x.toDoubleOrNull() ?: 0.0,
                            day = currentSelectedDay.value
                        )
                    }
                }
                recommendedPlaces.clear()
                recommendedPlaces.addAll(placesWithImages)
            } catch (e: Exception) {
                Log.e("Search", "검색 통신 실패", e)
            }
        }
    }

    // 🔥 에러 수정: ApiService 파라미터명과 일치시키고 (mapX, mapY), 카카오 API에 없는 필드(address_name) 제거
    fun selectLocationFromMap(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val newPlace = withContext(Dispatchers.IO) {
                    val searchRes = RetrofitClient.api.searchPlace(query = name, mapX = lng, mapY = lat)
                    val matchedPlace = searchRes.documents.firstOrNull()

                    val images = fetchExactImages(null, name, lng, lat)
                    val finalImages = images.ifEmpty { listOf(DEFAULT_IMAGE_URL) }

                    val shortTag = matchedPlace?.category_group_name?.split(">")?.lastOrNull()?.trim() ?: context.getString(R.string.poi)
                    val finalAddress = matchedPlace?.road_address_name?.ifEmpty { context.getString(R.string.no_address) } ?: context.getString(R.string.selected_from_map)

                    PlaceInfo(
                        id = matchedPlace?.id ?: "",
                        name = name,
                        address = finalAddress,
                        tag = shortTag,
                        imageUrl = finalImages.first(),
                        imageUrls = finalImages,
                        latitude = lat,
                        longitude = lng,
                        day = currentSelectedDay.value
                    )
                }

                selectedPlace.value = newPlace
                isSearchActive.value = false
                searchQuery.value = ""
                nearbyRestaurants.clear()
                selectedRestaurantDetail.value = null

            } catch (e: Exception) {
                Log.e("MapClick", "지도 심볼 통신 실패", e)
            }
        }
    }

    fun searchNearbyRestaurants(lat: Double, lng: Double) {
        viewModelScope.launch {
            isFetchingRestaurants.value = true
            selectedRestaurantDetail.value = null
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getNearbyRestaurants(lng, lat, searchRadius.intValue)
                }
                val items = response.response?.body?.items?.item ?: emptyList()

                val updatedItems = withContext(Dispatchers.IO) {
                    coroutineScope {
                        items.map { restaurant ->
                            async {
                                val tourImages = getValidTourImages(listOf(restaurant.firstimage, restaurant.firstimage2))

                                val exactImages = if (tourImages.isEmpty()) {
                                    fetchExactImages(restaurant.contentid, restaurant.title, restaurant.mapx.toDoubleOrNull(), restaurant.mapy.toDoubleOrNull())
                                } else {
                                    emptyList()
                                }

                                val finalImages = (tourImages + exactImages).distinct().ifEmpty { listOf(DEFAULT_IMAGE_URL) }

                                restaurant.copy(
                                    firstimage = finalImages.first(),
                                    imageUrls = finalImages
                                )
                            }
                        }.awaitAll()
                    }
                }

                nearbyRestaurants.clear()
                nearbyRestaurants.addAll(updatedItems)

                selectedPlace.value = null
                isSearchActive.value = false
                searchQuery.value = ""
            } catch (e: retrofit2.HttpException) {
                Log.e("TourAPI", "HTTP 오류 코드 = ${e.code()}")
            } catch (e: Exception) {
                Log.e("TourAPI", "주변 식당 검색 실패", e)
            } finally {
                isFetchingRestaurants.value = false
            }
        }
    }

    fun fetchRestaurantDetail(contentId: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getRestaurantDetails(contentId = contentId)
                }
                val items = response.response?.body?.items?.item
                selectedRestaurantDetail.value = items?.firstOrNull()
            } catch (e: retrofit2.HttpException) {
                Log.e("TourAPI_DETAIL", "HTTP 오류 코드 = ${e.code()}", e)
                selectedRestaurantDetail.value = null
            } catch (e: Exception) {
                Log.e("TourAPI_DETAIL", "식당 상세 검색 실패", e)
                selectedRestaurantDetail.value = null
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
        return travelRoute.filter { it.day == day }.map { LatLng(it.latitude, it.longitude) }
    }

    fun toggleCondition(condition: String) {
        val current = selectedConditions.value
        selectedConditions.value = if (current.contains(condition)) {
            current - condition
        } else {
            current + condition
        }
    }

    fun addPlaceToRoute(place: PlaceInfo) {
        if (!travelRoute.any { it.name == place.name }) {
            travelRoute.add(place.copy(day = currentSelectedDay.value))
            travelRoute.sortBy { it.day }
        }
        selectedPlace.value = null
        searchQuery.value = ""
        isSearchActive.value = false
        nearbyRestaurants.clear()
        selectedRestaurantDetail.value = null
    }

    fun removePlace(place: PlaceInfo) {
        travelRoute.remove(place)
    }

    fun movePlace(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in travelRoute.indices || toIndex !in travelRoute.indices) return
        val item = travelRoute.removeAt(fromIndex)
        travelRoute.add(toIndex, item)
    }

    companion object {
        private const val DEFAULT_IMAGE_URL = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"
    }
}