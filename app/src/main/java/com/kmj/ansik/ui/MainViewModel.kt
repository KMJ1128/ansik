package com.kmj.ansik.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
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
import com.kmj.ansik.R

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    var selectedConditions = mutableStateOf<Set<String>>(emptySet())
        private set

    val travelRoute = mutableStateListOf<PlaceInfo>()
    val recommendedPlaces = mutableStateListOf<PlaceInfo>()

    val popularPlaces = mutableStateListOf<TourRestaurant>()
    val popularRestaurants = mutableStateListOf<TourRestaurant>()

    var showPopularPlaces = mutableStateOf(true)
    var maxPopularPlaces = mutableFloatStateOf(10f)

    var showPopularRestaurants = mutableStateOf(true)
    var maxPopularRestaurants = mutableFloatStateOf(10f)

    var nights = mutableStateOf(3)
    var days = mutableStateOf(4)
    var currentSelectedDay = mutableStateOf(1)

    private val sharedPreferences = context.getSharedPreferences("AnsikPrefs", Context.MODE_PRIVATE)

    var searchRadius = mutableIntStateOf(sharedPreferences.getInt("searchRadius", 2000))
        private set

    private var lastFetchedLat = 37.5665
    private var lastFetchedLng = 126.9780

    init {
        fetchPopularDataDynamic(lastFetchedLat, lastFetchedLng, force = true)
    }

    // 💡 안드로이드에서 네이버 고화질(link) 이미지를 직접 추출
    private suspend fun getFilteredImages(query: String): List<String> {
        return try {
            val response = RetrofitClient.api.searchImageNaver(query)
            response.items.map { it.link.replace("http://", "https://") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchPopularDataDynamic(lat: Double, lng: Double, force: Boolean = false) {
        if (!force) {
            val distance = FloatArray(1)
            android.location.Location.distanceBetween(lastFetchedLat, lastFetchedLng, lat, lng, distance)
            if (distance[0] < 3000f) return
        }

        lastFetchedLat = lat
        lastFetchedLng = lng

        viewModelScope.launch {
            try {
                coroutineScope {
                    val placesDef = async(Dispatchers.IO) { RetrofitClient.api.getPopularPlaces(lng, lat) }
                    val restsDef = async(Dispatchers.IO) { RetrofitClient.api.getPopularRestaurants(lng, lat) }

                    val placesRes = placesDef.await()
                    val restsRes = restsDef.await()

                    val places = placesRes.response?.body?.items?.item ?: emptyList()
                    val rests = restsRes.response?.body?.items?.item ?: emptyList()

                    val updatedPlaces = withContext(Dispatchers.IO) {
                        coroutineScope {
                            places.map { place ->
                                async {
                                    val finalImages = if (place.firstimage.isBlank() && place.firstimage2.isBlank()) {
                                        val shortAddr = place.addr1.split(" ").take(2).joinToString(" ")
                                        getFilteredImages("${place.title} $shortAddr")
                                    } else {
                                        listOfNotNull(place.firstimage, place.firstimage2)
                                    }
                                    place.copy(
                                        firstimage = finalImages.firstOrNull() ?: "",
                                        imageUrls = finalImages // 💡 모델 변수명 일치
                                    )
                                }
                            }.awaitAll()
                        }
                    }

                    val updatedRests = withContext(Dispatchers.IO) {
                        coroutineScope {
                            rests.map { rest ->
                                async {
                                    val finalImages = if (rest.firstimage.isBlank() && rest.firstimage2.isBlank()) {
                                        val shortAddr = rest.addr1.split(" ").take(2).joinToString(" ")
                                        getFilteredImages("${rest.title} $shortAddr 식당")
                                    } else {
                                        listOfNotNull(rest.firstimage, rest.firstimage2)
                                    }
                                    rest.copy(
                                        firstimage = finalImages.firstOrNull() ?: "",
                                        imageUrls = finalImages // 💡 모델 변수명 일치
                                    )
                                }
                            }.awaitAll()
                        }
                    }

                    popularPlaces.clear()
                    popularPlaces.addAll(updatedPlaces)

                    popularRestaurants.clear()
                    popularRestaurants.addAll(updatedRests)
                }
            } catch (e: Exception) {
                Log.e("PopularData", "동적 인기 데이터 불러오기 실패", e)
            }
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
                    val response = RetrofitClient.api.searchPlace(query = query)
                    coroutineScope {
                        response.documents.map { place ->
                            async {
                                val shortAddr = place.road_address_name.split(" ").take(2).joinToString(" ")
                                val exactQuery = "${place.place_name} $shortAddr"

                                val images = getFilteredImages(exactQuery)
                                val finalImages = images.ifEmpty { listOf(DEFAULT_IMAGE_URL) }
                                val shortTag = place.category_group_name.split(">").lastOrNull()?.trim().orEmpty()

                                PlaceInfo(
                                    id = place.id,
                                    name = place.place_name,
                                    address = place.road_address_name.ifEmpty { context.getString(R.string.no_address) },
                                    tag = shortTag.ifEmpty { context.getString(R.string.place) },
                                    imageUrl = finalImages.first(),
                                    imageUrls = finalImages,
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
                Log.e("Search", "검색 통신 실패", e)
            }
        }
    }

    fun selectLocationFromMap(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val newPlace = withContext(Dispatchers.IO) {
                    val searchRes = RetrofitClient.api.searchPlace(query = name)
                    val matchedPlace = searchRes.documents.firstOrNull()

                    val shortAddr = matchedPlace?.road_address_name?.split(" ")?.take(2)?.joinToString(" ") ?: ""
                    val exactQuery = "$name $shortAddr".trim()

                    val images = getFilteredImages(exactQuery)
                    val finalImages = images.ifEmpty { listOf(DEFAULT_IMAGE_URL) }

                    val shortTag = matchedPlace?.category_group_name?.split(">")?.lastOrNull()?.trim() ?: context.getString(R.string.poi)
                    val address = matchedPlace?.road_address_name?.ifEmpty { context.getString(R.string.no_address) } ?: context.getString(R.string.selected_from_map)

                    PlaceInfo(
                        id = matchedPlace?.id ?: "",
                        name = name,
                        address = address,
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
                    RetrofitClient.api.getNearbyRestaurants(
                        lng = lng,
                        lat = lat,
                        radius = searchRadius.intValue
                    )
                }
                val items = response.response?.body?.items?.item ?: emptyList()

                val updatedItems = withContext(Dispatchers.IO) {
                    coroutineScope {
                        items.map { restaurant ->
                            async {
                                val finalImages = if (restaurant.firstimage.isBlank() && restaurant.firstimage2.isBlank()) {
                                    val shortAddr = restaurant.addr1.split(" ").take(2).joinToString(" ")
                                    getFilteredImages("${restaurant.title} $shortAddr 식당")
                                } else {
                                    listOfNotNull(restaurant.firstimage, restaurant.firstimage2)
                                }
                                restaurant.copy(
                                    firstimage = finalImages.firstOrNull() ?: "",
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