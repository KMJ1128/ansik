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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val placeRepository = PlaceRepository()
    private val restaurantRepository = RestaurantRepository()
    private val reviewRepository = ReviewRepository()
    private val aiCourseRepository = AiCourseRepository()

    private val sharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    var selectedConditions = mutableStateOf(
        sharedPreferences.getStringSet(KEY_SELECTED_CONDITIONS, emptySet())
            ?.toSet()
            .orEmpty()
    )
        private set

    val travelRoute = mutableStateListOf<PlaceInfo>()
    val recommendedPlaces = mutableStateListOf<PlaceInfo>()
    val savedAiCourses = mutableStateListOf<AiCourse>().apply {
        addAll(loadSavedAiCourses())
    }
    val savedMyCourses = mutableStateListOf<SavedMyCourse>().apply {
        addAll(loadSavedMyCourses())
    }

    var isBuildingMyCourse = mutableStateOf(false)
        private set

    var isCreatingAiCourse = mutableStateOf(false)
        private set

    var aiCourseError = mutableStateOf<String?>(null)
        private set

    var appliedCourseVersion = mutableIntStateOf(0)
        private set

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

    var isSearchingPlaces = mutableStateOf(false)
        private set

    var isResolvingMapSelection = mutableStateOf(false)
        private set

    var selectedPlace = mutableStateOf<PlaceInfo?>(null)
        private set

    val nearbyRestaurants = mutableStateListOf<RestaurantSummary>()

    var currentUserLocation = mutableStateOf<LatLng?>(null)
        private set

    var restaurantSearchCenter = mutableStateOf<LatLng?>(null)
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
    private var mapSelectionJob: Job? = null
    private var mapSelectionRequestId = 0L
    private var menuDetailJob: Job? = null
    private var restaurantDetailJob: Job? = null
    private var loadingRestaurantId: String? = null

    fun searchPlacesRealtime(query: String) {
        searchQuery.value = query
        isSearchActive.value = query.isNotEmpty()

        if (query.isBlank()) {
            searchJob?.cancel()
            isSearchingPlaces.value = false
            recommendedPlaces.clear()
            return
        }

        searchJob?.cancel()
        isSearchingPlaces.value = true
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
            } finally {
                if (searchQuery.value == query) {
                    isSearchingPlaces.value = false
                }
            }
        }
    }

    fun clearSearch() {
        searchQuery.value = ""
        isSearchActive.value = false
        searchJob?.cancel()
        isSearchingPlaces.value = false
        recommendedPlaces.clear()
    }

    fun selectLocationFromMap(name: String, lat: Double, lng: Double) {
        mapSelectionJob?.cancel()
        val requestId = ++mapSelectionRequestId
        mapSelectionJob = viewModelScope.launch {
            isResolvingMapSelection.value = true
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
            } finally {
                if (mapSelectionRequestId == requestId) {
                    isResolvingMapSelection.value = false
                }
            }
        }
    }

    fun clearSelectedPlace() {
        selectedPlace.value = null
    }

    fun searchNearbyRestaurants(lat: Double, lng: Double) {
        viewModelScope.launch {
            restaurantSearchCenter.value = LatLng(lat, lng)
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

    fun clearNearbyRestaurants() {
        nearbyRestaurants.clear()
        hasSearchedRestaurants.value = false
        restaurantSearchCenter.value = null
    }

    fun fetchRestaurantDetail(restaurant: RestaurantSummary) {
        if (selectedRestaurantDetail.value?.restaurant?.id == restaurant.id) return
        if (isFetchingRestaurantDetail.value && loadingRestaurantId == restaurant.id) return

        restaurantDetailJob?.cancel()
        val requestedRestaurantId = restaurant.id
        loadingRestaurantId = requestedRestaurantId
        selectedRestaurantDetail.value = null
        clearMenuDetail()
        restaurantDetailJob = viewModelScope.launch {
            isFetchingRestaurantDetail.value = true
            try {
                val loadedDetail = restaurantRepository.getRestaurantDetail(
                    restaurant = restaurant,
                    language = currentLanguageTag(),
                    healthConditions = selectedConditions.value.sorted()
                )
                if (loadingRestaurantId == requestedRestaurantId) {
                    selectedRestaurantDetail.value = loadedDetail
                }
            } catch (e: retrofit2.HttpException) {
                Log.e("TourAPI_DETAIL", "HTTP 오류 코드 = ${e.code()}", e)
                if (loadingRestaurantId == requestedRestaurantId) {
                    selectedRestaurantDetail.value = null
                }
            } catch (e: Exception) {
                Log.e("TourAPI_DETAIL", "식당 상세 검색 실패", e)
                if (loadingRestaurantId == requestedRestaurantId) {
                    selectedRestaurantDetail.value = null
                }
            } finally {
                if (loadingRestaurantId == requestedRestaurantId) {
                    isFetchingRestaurantDetail.value = false
                    loadingRestaurantId = null
                }
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
                disclaimer = selectedRestaurantDetail.value?.menuGuide?.disclaimer.orEmpty(),
                healthRiskLevel = menu.healthRiskLevel,
                healthRiskSummary = menu.healthRiskSummary,
                healthRiskReasons = menu.healthRiskReasons,
                questionsForRestaurant = menu.questionsForRestaurant
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
        val updated = if (current.contains(condition)) {
            current - condition
        } else {
            current + condition
        }
        selectedConditions.value = updated
        sharedPreferences.edit()
            .putStringSet(KEY_SELECTED_CONDITIONS, updated)
            .apply()
        clearRestaurantDetail()
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

    fun createAiCourse(
        cityCode: String,
        cityName: String,
        nights: Int,
        days: Int,
        stopsPerDay: Int,
        existingSchedule: String,
        preferences: List<String>,
        onSuccess: (AiCourse) -> Unit
    ) {
        if (isCreatingAiCourse.value) return
        viewModelScope.launch {
            isCreatingAiCourse.value = true
            aiCourseError.value = null
            try {
                val course = aiCourseRepository.createCourse(
                    AiCourseRequest(
                        cityCode = cityCode,
                        cityName = cityName,
                        nights = nights,
                        days = days,
                        stopsPerDay = stopsPerDay,
                        existingSchedule = existingSchedule,
                        preferences = preferences,
                        healthConditions = selectedConditions.value.sorted(),
                        language = currentLanguageTag()
                    )
                )
                if (course.status == "OPENAI_READY" && course.itinerary.isNotEmpty()) {
                    savedAiCourses.removeAll { it.id == course.id }
                    savedAiCourses.add(0, course)
                    while (savedAiCourses.size > MAX_SAVED_AI_COURSES) {
                        savedAiCourses.removeAt(savedAiCourses.lastIndex)
                    }
                    saveAiCourses()
                    applyAiCourse(course)
                    onSuccess(course)
                } else {
                    aiCourseError.value = course.status.ifBlank { "OPENAI_NO_RESULT" }
                }
            } catch (e: Exception) {
                Log.e("AI_COURSE", "AI 코스 생성 실패", e)
                aiCourseError.value = "NETWORK_ERROR"
            } finally {
                isCreatingAiCourse.value = false
            }
        }
    }

    fun applyAiCourse(course: AiCourse) {
        val route = course.itinerary
            .sortedBy { it.day }
            .flatMap { day ->
                day.stops.map { stop ->
                    PlaceInfo(
                        id = "ai:${course.id}:${day.day}:${stop.id}",
                        name = stop.name,
                        address = stop.address,
                        tag = courseCategoryLabel(stop.category),
                        imageUrl = stop.imageUrl.ifBlank { RestaurantRepository.DEFAULT_IMAGE_URL },
                        imageUrls = listOfNotNull(stop.imageUrl.takeIf { it.isNotBlank() }),
                        latitude = stop.latitude,
                        longitude = stop.longitude,
                        day = day.day
                    )
                }
            }
            .filter { it.latitude != 0.0 && it.longitude != 0.0 }
        if (route.isEmpty()) return

        isBuildingMyCourse.value = false
        travelRoute.clear()
        travelRoute.addAll(route)
        days.value = course.days.coerceIn(1, MAX_TRAVEL_DAYS)
        nights.value = course.nights.coerceIn(0, days.value - 1)
        currentSelectedDay.value = 1
        clearSelectedPlace()
        clearSearch()
        clearNearbyRestaurants()
        clearRestaurantDetail()
        appliedCourseVersion.intValue += 1
    }

    fun startNewMyCourse() {
        travelRoute.clear()
        nights.value = 0
        days.value = 1
        currentSelectedDay.value = 1
        isBuildingMyCourse.value = true
        clearSelectedPlace()
        clearSearch()
        clearNearbyRestaurants()
        clearRestaurantDetail()
        appliedCourseVersion.intValue += 1
    }

    fun saveCurrentMyCourse(title: String): Boolean {
        if (travelRoute.isEmpty()) return false
        val cleanTitle = title.trim().take(60).ifBlank {
            context.getString(R.string.my_course_default_name, savedMyCourses.size + 1)
        }
        val course = SavedMyCourse(
            id = UUID.randomUUID().toString(),
            title = cleanTitle,
            nights = nights.value,
            days = days.value,
            places = travelRoute.map { it.copy() }
        )
        savedMyCourses.add(0, course)
        while (savedMyCourses.size > MAX_SAVED_MY_COURSES) {
            savedMyCourses.removeAt(savedMyCourses.lastIndex)
        }
        saveMyCourses()
        isBuildingMyCourse.value = false
        return true
    }

    fun applyMyCourse(course: SavedMyCourse) {
        if (course.places.isEmpty()) return
        travelRoute.clear()
        travelRoute.addAll(course.places.map { it.copy() })
        days.value = course.days.coerceIn(1, MAX_TRAVEL_DAYS)
        nights.value = course.nights.coerceIn(0, days.value - 1)
        currentSelectedDay.value = 1
        isBuildingMyCourse.value = false
        clearSelectedPlace()
        clearSearch()
        clearNearbyRestaurants()
        clearRestaurantDetail()
        appliedCourseVersion.intValue += 1
    }

    fun clearAiCourseError() {
        aiCourseError.value = null
    }

    private fun loadSavedAiCourses(): List<AiCourse> {
        val json = sharedPreferences.getString(KEY_SAVED_AI_COURSES, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<AiCourse>>() {}.type
            gson.fromJson<List<AiCourse>>(json, type).orEmpty()
        }.getOrElse {
            Log.w("AI_COURSE", "저장된 AI 코스를 읽지 못했습니다", it)
            emptyList()
        }
    }

    private fun saveAiCourses() {
        sharedPreferences.edit()
            .putString(KEY_SAVED_AI_COURSES, gson.toJson(savedAiCourses.toList()))
            .apply()
    }

    private fun loadSavedMyCourses(): List<SavedMyCourse> {
        val json = sharedPreferences.getString(KEY_SAVED_MY_COURSES, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<SavedMyCourse>>() {}.type
            gson.fromJson<List<SavedMyCourse>>(json, type).orEmpty()
        }.getOrElse {
            Log.w("MY_COURSE", "저장된 직접 만든 코스를 읽지 못했습니다", it)
            emptyList()
        }
    }

    private fun saveMyCourses() {
        sharedPreferences.edit()
            .putString(KEY_SAVED_MY_COURSES, gson.toJson(savedMyCourses.toList()))
            .apply()
    }

    private fun courseCategoryLabel(category: String): String = when (category) {
        "ATTRACTION" -> context.getString(R.string.course_category_attraction)
        "CULTURE" -> context.getString(R.string.course_category_culture)
        "FESTIVAL" -> context.getString(R.string.course_category_festival)
        "LEISURE" -> context.getString(R.string.course_category_leisure)
        "SHOPPING" -> context.getString(R.string.course_category_shopping)
        "RESTAURANT" -> context.getString(R.string.course_category_restaurant)
        else -> context.getString(R.string.place)
    }

    private fun currentLanguageTag(): String {
        val appLocale = AppCompatDelegate.getApplicationLocales()[0]
        return appLocale?.toLanguageTag()
            ?: context.resources.configuration.locales[0].toLanguageTag()
    }

    companion object {
        private const val PREFS_NAME = "AnsikPrefs"
        private const val KEY_SEARCH_RADIUS = "searchRadius"
        private const val KEY_SELECTED_CONDITIONS = "selectedHealthConditions"
        private const val KEY_SAVED_AI_COURSES = "savedAiCourses"
        private const val KEY_SAVED_MY_COURSES = "savedMyCourses"
        private const val MAX_SAVED_AI_COURSES = 10
        private const val MAX_SAVED_MY_COURSES = 20
        private const val DEFAULT_SEARCH_RADIUS = 2000
        private const val MAX_TRAVEL_DAYS = 14
        private const val SEARCH_DEBOUNCE_MS = 400L
        private const val REVIEW_PAGE_SIZE = 5
    }
}
