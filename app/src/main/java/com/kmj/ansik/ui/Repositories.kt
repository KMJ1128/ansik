package com.kmj.ansik.ui

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class PlaceRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun searchPlace(
        query: String,
        mapX: Double? = null,
        mapY: Double? = null
    ): KakaoSearchResponse = withContext(Dispatchers.IO) {
        api.searchPlace(
            query = query,
            mapX = mapX,
            mapY = mapY
        )
    }

    suspend fun fetchExactImages(
        tourId: String? = null,
        title: String? = null,
        mapX: Double? = null,
        mapY: Double? = null,
        language: String = "ko"
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            api.getExactImages(
                tourId = tourId,
                title = title,
                mapX = mapX,
                mapY = mapY,
                language = language
            )
        } catch (e: Exception) {
            Log.e("PlaceRepository", "이미지 요청 실패: $title", e)
            emptyList()
        }
    }
}

class RestaurantRepository(
    private val api: ApiService = RetrofitClient.api,
    private val placeRepository: PlaceRepository = PlaceRepository(api)
) {
    suspend fun getNearbyRestaurants(
        longitude: Double,
        latitude: Double,
        radius: Int,
        language: String
    ): List<RestaurantSummary> = withContext(Dispatchers.IO) {
        api.getTourApiNearbyRestaurants(
            lng = longitude,
            lat = latitude,
            radius = radius,
            language = language
        ).map { restaurant ->
            val sourceImages = normalizeImages(
                restaurant.imageUrls + restaurant.imageUrl
            )

            val fallbackImages = if (
                sourceImages.isEmpty() &&
                restaurant.hasTourData
            ) {
                placeRepository.fetchExactImages(
                    tourId = restaurant.tourContentId,
                    title = restaurant.title,
                    mapX = restaurant.longitude,
                    mapY = restaurant.latitude,
                    language = restaurant.tourLanguage
                )
            } else {
                emptyList()
            }

            val finalImages = normalizeImages(sourceImages + fallbackImages)

            restaurant.copy(
                imageUrl = finalImages.firstOrNull().orEmpty(),
                imageUrls = finalImages
            )
        }
    }

    suspend fun getRestaurantDetail(
        restaurant: RestaurantSummary,
        language: String
    ): RestaurantDetailState = coroutineScope {
        val contentId = restaurant.tourContentId

        val detail = withContext(Dispatchers.IO) {
            if (contentId.isNullOrBlank()) {
                return@withContext null
            }
            runCatching {
                api.getRestaurantDetails(
                    contentId = contentId,
                    language = restaurant.tourLanguage
                )
                    .response
                    ?.body
                    ?.items
                    ?.item
                    ?.firstOrNull()
            }.getOrNull()
        }

        val menuHints = listOfNotNull(detail?.firstmenu, detail?.treatmenu)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val menuGuide = withContext(Dispatchers.IO) {
            runCatching {
                api.getRestaurantMenuGuide(
                    restaurantName = restaurant.title,
                    address = restaurant.address,
                    language = language,
                    menuHints = menuHints
                )
            }.getOrNull()
        }

        RestaurantDetailState(
            restaurant = restaurant,
            tourDetail = detail,
            menuGuide = menuGuide
        )
    }

    suspend fun getMenuDetail(
        menuName: String,
        language: String
    ): MenuDetailState = coroutineScope {
        val profile = async(Dispatchers.IO) {
            runCatching {
                api.getMenuProfile(menuName = menuName, language = language)
            }.getOrNull()
        }.await()
        val imageSearchName = profile?.canonicalKoreanName
            ?.takeIf { it.isNotBlank() }
            ?: menuName
        val images = async(Dispatchers.IO) {
            runCatching {
                api.getMenuImages(menuName = imageSearchName)
            }.getOrDefault(emptyList())
        }.await()

        MenuDetailState(
            menuName = menuName,
            profile = profile,
            imageUrls = images
                .filter { it.isNotBlank() }
                .distinct()
        )
    }

    private fun normalizeImages(
        urls: List<String>
    ): List<String> {
        return urls
            .filter { it.isNotBlank() }
            .map { it.replace("http://", "https://") }
            .distinct()
    }

    companion object {
        const val DEFAULT_IMAGE_URL =
            "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"
    }
}

class ReviewRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun getPlaceReviews(
        placeName: String,
        address: String,
        start: Int
    ): List<BlogReview> = withContext(Dispatchers.IO) {
        api.getPlaceReviews(
            placeName = placeName,
            address = address,
            start = start
        )
    }
}
