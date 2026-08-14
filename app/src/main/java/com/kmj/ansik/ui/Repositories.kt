package com.kmj.ansik.ui

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
            Log.e(
                "PlaceRepository",
                "이미지 요청 실패: $title",
                e
            )
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
    ): List<TourRestaurant> = withContext(Dispatchers.IO) {
        val response = api.getNearbyRestaurants(
            longitude,
            latitude,
            radius
        )

        val items = response.response
            ?.body
            ?.items
            ?.item
            .orEmpty()

        coroutineScope {
            items.map { restaurant ->
                async {
                    val tourImages = normalizeImages(
                        listOf(
                            restaurant.firstimage,
                            restaurant.firstimage2
                        )
                    )

                    val exactImages =
                        if (tourImages.isEmpty()) {
                            placeRepository.fetchExactImages(
                                tourId = restaurant.contentid,
                                title = restaurant.title,
                                mapX = restaurant.mapx.toDoubleOrNull(),
                                mapY = restaurant.mapy.toDoubleOrNull()
                            )
                        } else {
                            emptyList()
                        }

                    val finalImages =
                        (tourImages + exactImages)
                            .filter { it.isNotBlank() }
                            .map {
                                it.replace(
                                    "http://",
                                    "https://"
                                )
                            }
                            .distinct()
                            .ifEmpty {
                                listOf(DEFAULT_IMAGE_URL)
                            }

                    restaurant.copy(
                        firstimage = finalImages.first(),
                        imageUrls = finalImages
                    )
                }
            }.awaitAll()
        }
    }

    suspend fun getRestaurantDetail(
        contentId: String
    ): TourRestaurantDetail? =
        withContext(Dispatchers.IO) {
            val response =
                api.getRestaurantDetails(contentId)

            response.response
                ?.body
                ?.items
                ?.item
                ?.firstOrNull()
        }

    private fun normalizeImages(
        urls: List<String>
    ): List<String> {
        return urls
            .filter { it.isNotBlank() }
            .map {
                it.replace(
                    "http://",
                    "https://"
                )
            }
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
    ): List<BlogReview> =
        withContext(Dispatchers.IO) {
            api.getPlaceReviews(
                placeName,
                address,
                start
            )
        }
}