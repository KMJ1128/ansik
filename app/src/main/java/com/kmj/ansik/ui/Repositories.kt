package com.kmj.ansik.ui

import android.util.Log
import com.kmj.ansik.privacy.PrivacyConsentStore
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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
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
        language: String,
        healthConditions: List<String>
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

        val consentedHealthConditions = if (PrivacyConsentStore.hasSensitiveInfoConsent()) {
            healthConditions
        } else {
            emptyList()
        }

        val menuGuide = withContext(Dispatchers.IO) {
            runCatching {
                api.getRestaurantMenuGuide(
                    restaurantName = restaurant.title,
                    address = restaurant.address,
                    language = language,
                    menuHints = menuHints,
                    healthConditions = consentedHealthConditions
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
        MenuDetailState(
            menuName = menuName,
            profile = profile
        )
    }

    private fun normalizeImages(
        urls: List<String>
    ): List<String> {
        return urls
            .map(String::trim)
            .filter { (it.startsWith("http://") || it.startsWith("https://")) && it != DEFAULT_IMAGE_URL }
            .distinct()
    }

    companion object {
        const val DEFAULT_IMAGE_URL =
            "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"
    }
}

class AiCourseRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun createCourse(request: AiCourseRequest): AiCourse = withContext(Dispatchers.IO) {
        val consentedRequest = if (PrivacyConsentStore.hasSensitiveInfoConsent()) {
            request
        } else {
            request.copy(healthConditions = emptyList())
        }
        api.createAiCourse(consentedRequest)
    }
}
