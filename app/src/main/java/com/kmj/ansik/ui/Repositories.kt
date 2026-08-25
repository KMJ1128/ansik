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
        mapY: Double? = null
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            api.getExactImages(
                tourId = tourId,
                title = title,
                mapX = mapX,
                mapY = mapY
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
        radius: Int
    ): List<RestaurantSummary> = withContext(Dispatchers.IO) {
        api.getTourApiNearbyRestaurants(
            lng = longitude,
            lat = latitude,
            radius = radius
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
                    mapY = restaurant.latitude
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
        restaurant: RestaurantSummary
    ): RestaurantDetailState = coroutineScope {
        val contentId = restaurant.tourContentId

        if (contentId.isNullOrBlank()) {
            return@coroutineScope RestaurantDetailState(
                restaurant = restaurant
            )
        }

        val detailDeferred = async(Dispatchers.IO) {
            runCatching {
                api.getRestaurantDetails(contentId)
                    .response
                    ?.body
                    ?.items
                    ?.item
                    ?.firstOrNull()
            }.getOrNull()
        }

        val menuImagesDeferred = async(Dispatchers.IO) {
            runCatching {
                api.getTourMenuImages(contentId)
            }.getOrDefault(emptyList())
        }

        RestaurantDetailState(
            restaurant = restaurant,
            tourDetail = detailDeferred.await(),
            menuImages = normalizeImages(menuImagesDeferred.await())
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
