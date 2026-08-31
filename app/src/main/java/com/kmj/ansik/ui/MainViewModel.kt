package com.kmj.ansik.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.appcompat.app.AppCompatDelegate
import com.kmj.ansik.R
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val placeRepository = PlaceRepository()
    private val restaurantRepository = RestaurantRepository()
    private val reviewRepository = ReviewRepository()

    private val sharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var selectedConditions = mutableStateOf<Set<String>>(emptySet())
        private set

    val travelRoute = mutableStateListOf<PlaceInfo>()
    val recommendedPlaces = mutableStateListOf<PlaceInfo>()

    var nights = mutableStateOf(3)
        private set

    var days = mutableStateOf(4)
        private set

    var currentSelectedDay = mutableStateOf(1)
        private set

    var searchRadius = mutableIntStateOf(
        sharedPreferences.getInt(KEY_SEARCH_RADIUS, DEFAULT_SEARCH_RADIUS)
    )
        private set

    var searchQuery = mutableStateOf("")
        private set

    var isSearchActive = mutableStateOf(false)
        private set

    var selectedPlace = mutableStateOf<PlaceInfo?>(null)
        private set

    val nearbyRestaurants = mutableStateListOf<RestaurantSummary>()

    var currentUserLocation = mutableStateOf<LatLng?>(null)
        private set

    var isFetchingRestaurants = mutableStateOf(false)
        private set

    var hasSearchedRestaurants = mutableStateOf(false)
        private set

    var selectedRestaurantDetail = mutableStateOf<RestaurantDetailState?>(null)
        private set

    var isFetchingRestaurantDetail = mutableStateOf(false)
        private set

    var selectedMenuDetail = mutableStateOf<MenuDetailState?>(null)
        private set

    var isFetchingMenuDetail = mutableStateOf(false)
        private set

    var showReviewSheet = mutableStateOf(false)
        private set

    val selectedPlaceReviews = mutableStateListOf<BlogReview>()

    var isFetchingReviews = mutableStateOf(false)
        private set

    var hasMoreReviews = mutableStateOf(true)
        private set

    var currentReviewPlaceName = ""
        private set

    var currentReviewAddress = ""
        private set

    private var reviewStartPage = 1
    private var searchJob: Job? = null
    private var menuDetailJob: Job? = null
    private var restaurantDetailJob: Job? = null
    private var loadingRestaurantId: String? = null

    fun searchPlacesRealtime(query: String) {
        searchQuery.value = query
        isSearchActive.value = query.isNotEmpty()

        if (query.isBlank()) {
            recommendedPlaces.clear()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)

            try {
                val response = placeRepository.searchPlace(query = query)
                val places = response.documents.map { place ->
                    val shortTag = place.category_group_name
                        .split(">")
                        .lastOrNull()
                        ?.trim()
                        .orEmpty()

                    PlaceInfo(
                        id = place.id,
                        name = place.place_name,
                        address = place.road_address_name.ifEmpty {
                            context.getString(R.string.no_address)
                        },
                        tag = shortTag.ifEmpty {
                            context.getString(R.string.place)
                        },
                        imageUrl = RestaurantRepository.DEFAULT_IMAGE_URL,
                        imageUrls = listOf(RestaurantRepository.DEFAULT_IMAGE_URL),
                        latitude = place.y.toDoubleOrNull() ?: 0.0,
                        longitude = place.x.toDoubleOrNull() ?: 0.0,
                        day = currentSelectedDay.value
                    )
                }

                recommendedPlaces.clear()
                recommendedPlaces.addAll(places)
            } catch (e: Exception) {
                Log.e("Search", "검색 통신 실패", e)
            }
        }
    }

    fun clearSearch() {
        searchQuery.value = ""
        isSearchActive.value = false
        recommendedPlaces.clear()
    }

    fun selectLocationFromMap(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val searchResponse = placeRepository.searchPlace(
                    query = name,
                    mapX = lng,
                    mapY = lat
                )
                val matchedPlace = searchResponse.documents.firstOrNull()

                val images = placeRepository.fetchExactImages(
                    title = name,
                    mapX = lng,
                    mapY = lat
                )
                val finalImages = images.ifEmpty {
                    listOf(RestaurantRepository.DEFAULT_IMAGE_URL)
                }

                val shortTag = matchedPlace?.category_group_name
                    ?.split(">")
                    ?.lastOrNull()
                    ?.trim()
                    ?: context.getString(R.string.poi)

                val finalAddress = matchedPlace?.road_address_name
                    ?.ifEmpty { context.getString(R.string.no_address) }
                    ?: context.getString(R.string.selected_from_map)

                selectedPlace.value = PlaceInfo(
                    id = matchedPlace?.id.orEmpty(),
                    name = name,
                    address = finalAddress,
                    tag = shortTag,
                    imageUrl = finalImages.first(),
                    imageUrls = finalImages,
                    latitude = lat,
                    longitude = lng,
                    day = currentSelectedDay.value
                )

                clearSearch()
                clearNearbyRestaurants()
                clearRestaurantDetail()
            } catch (e: Exception) {
                Log.e("MapClick", "지도 심볼 통신 실패", e)
            }
        }
    }

    fun clearSelectedPlace() {
        selectedPlace.value = null
    }

    fun searchNearbyRestaurants(lat: Double, lng: Double) {
        viewModelScope.launch {
            isFetchingRestaurants.value = true
            hasSearchedRestaurants.value = false
            clearRestaurantDetail()

            try {
                val restaurants = restaurantRepository.getNearbyRestaurants(
                    longitude = lng,
                    latitude = lat,
                    radius = searchRadius.intValue,
                    language = currentLanguageTag()
                )

                nearbyRestaurants.clear()
                nearbyRestaurants.addAll(restaurants)
                hasSearchedRestaurants.value = true

                clearSelectedPlace()
                clearSearch()
            } catch (e: retrofit2.HttpException) {
                Log.e("TourAPI", "HTTP 오류 코드 = ${e.code()}", e)
            } catch (e: Exception) {
                Log.e("TourAPI", "주변 식당 검색 실패", e)
            } finally {
                isFetchingRestaurants.value = false
            }
        }
    }

    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        currentUserLocation.value = LatLng(latitude, longitude)
    }

    fun searchRestaurantsFromCurrentLocation() {
        currentUserLocation.value?.let { location ->
            searchNearbyRestaurants(location.latitude, location.longitude)
        }
    }

    fun clearNearbyRestaurants() {
        nearbyRestaurants.clear()
        hasSearchedRestaurants.value = false
    }

    fun fetchRestaurantDetail(restaurant: RestaurantSummary) {
        if (selectedRestaurantDetail.value?.restaurant?.id == restaurant.id) return
        if (isFetchingRestaurantDetail.value && loadingRestaurantId == restaurant.id) return

        restaurantDetailJob?.cancel()
        loadingRestaurantId = restaurant.id
        clearMenuDetail()
        restaurantDetailJob = viewModelScope.launch {
            isFetchingRestaurantDetail.value = true
            try {
                selectedRestaurantDetail.value =
                    restaurantRepository.getRestaurantDetail(
                        restaurant = restaurant,
                        language = currentLanguageTag()
                    )
            } catch (e: retrofit2.HttpException) {
                Log.e("TourAPI_DETAIL", "HTTP 오류 코드 = ${e.code()}", e)
                clearRestaurantDetail()
            } catch (e: Exception) {
                Log.e("TourAPI_DETAIL", "식당 상세 검색 실패", e)
                clearRestaurantDetail()
            } finally {
                isFetchingRestaurantDetail.value = false
                loadingRestaurantId = null
            }
        }
    }

    fun clearRestaurantDetail() {
        restaurantDetailJob?.cancel()
        restaurantDetailJob = null
        loadingRestaurantId = null
        selectedRestaurantDetail.value = null
        isFetchingRestaurantDetail.value = false
        clearMenuDetail()
    }

    fun fetchMenuDetail(menuName: String) {
        val cleanMenuName = menuName.trim()
        if (cleanMenuName.isBlank()) return

        menuDetailJob?.cancel()
        selectedMenuDetail.value = MenuDetailState(menuName = cleanMenuName)
        menuDetailJob = viewModelScope.launch {
            isFetchingMenuDetail.value = true
            try {
                selectedMenuDetail.value = restaurantRepository.getMenuDetail(
                    menuName = cleanMenuName,
                    language = currentLanguageTag()
                )
            } catch (e: Exception) {
                Log.e("MENU_DETAIL", "메뉴 상세 검색 실패: $cleanMenuName", e)
            } finally {
                isFetchingMenuDetail.value = false
            }
        }
    }

    fun showResearchedMenuDetail(menu: RestaurantMenuItem) {
        menuDetailJob?.cancel()
        menuDetailJob = null
        isFetchingMenuDetail.value = false
        selectedMenuDetail.value = MenuDetailState(
            menuName = menu.name,
            profile = MenuProfile(
                menuName = menu.name,
                canonicalKoreanName = menu.name,
                description = menu.description,
                tasteTags = menu.tasteTags,
                typicalIngredients = menu.typicalIngredients,
                possibleAllergens = menu.possibleAllergens,
                matchStatus = "OPENAI_RESEARCHED",
                descriptionSource = "OPENAI_WEB_RESEARCH",
                descriptionSourceUrl = menu.sourceUrls.firstOrNull().orEmpty(),
                disclaimer = selectedRestaurantDetail.value?.menuGuide?.disclaimer.orEmpty()
            ),
            imageUrls = menu.imageUrls
        )
    }

    private fun clearMenuDetail() {
        menuDetailJob?.cancel()
        menuDetailJob = null
        selectedMenuDetail.value = null
        isFetchingMenuDetail.value = false
    }

    fun fetchPlaceReviews(
        placeName: String,
        address: String,
        isLoadMore: Boolean = false
    ) {
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
                val reviews = reviewRepository.getPlaceReviews(
                    placeName = currentReviewPlaceName,
                    address = currentReviewAddress,
                    start = reviewStartPage
                )

                if (reviews.isEmpty()) {
                    hasMoreReviews.value = false
                } else {
                    selectedPlaceReviews.addAll(reviews)
                    reviewStartPage += REVIEW_PAGE_SIZE
                }
            } catch (e: Exception) {
                Log.e("Reviews", "리뷰 불러오기 실패", e)
            } finally {
                isFetchingReviews.value = false
            }
        }
    }

    fun dismissReviewSheet() {
        showReviewSheet.value = false
    }

    fun updateSearchRadius(radius: Int) {
        searchRadius.intValue = radius
        sharedPreferences.edit()
            .putInt(KEY_SEARCH_RADIUS, radius)
            .apply()
    }

    fun increaseDays() {
        if (days.value >= MAX_TRAVEL_DAYS) return

        days.value += 1
        nights.value = days.value - 1
    }

    fun decreaseDays() {
        if (days.value <= 1) return

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

    fun changePlaceDay(place: PlaceInfo, newDay: Int) {
        val index = travelRoute.indexOfFirst { it.id == place.id }
        if (index == -1) return

        travelRoute[index] = travelRoute[index].copy(day = newDay)
        currentSelectedDay.value = newDay
        travelRoute.sortBy { it.day }
    }

    fun getRouteCoordsForDay(day: Int): List<LatLng> {
        return travelRoute
            .filter { it.day == day }
            .map { LatLng(it.latitude, it.longitude) }
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
        if (travelRoute.none { it.name == place.name }) {
            travelRoute.add(place.copy(day = currentSelectedDay.value))
            travelRoute.sortBy { it.day }
        }

        clearSelectedPlace()
        clearSearch()
        clearNearbyRestaurants()
        clearRestaurantDetail()
    }

    fun removePlace(place: PlaceInfo) {
        travelRoute.remove(place)
    }

    fun movePlace(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in travelRoute.indices || toIndex !in travelRoute.indices) return

        val item = travelRoute.removeAt(fromIndex)
        travelRoute.add(toIndex, item)
    }

    private fun currentLanguageTag(): String {
        val appLocale = AppCompatDelegate.getApplicationLocales()[0]
        return appLocale?.toLanguageTag()
            ?: context.resources.configuration.locales[0].toLanguageTag()
    }

    companion object {
        private const val PREFS_NAME = "AnsikPrefs"
        private const val KEY_SEARCH_RADIUS = "searchRadius"
        private const val DEFAULT_SEARCH_RADIUS = 2000
        private const val MAX_TRAVEL_DAYS = 14
        private const val SEARCH_DEBOUNCE_MS = 400L
        private const val REVIEW_PAGE_SIZE = 5
    }
}
