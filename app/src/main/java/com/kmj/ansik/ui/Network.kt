package com.kmj.ansik.ui

import com.kmj.ansik.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/place")
    suspend fun searchPlace(
        @Query("query") query: String,
        @Query("mapX") mapX: Double? = null,
        @Query("mapY") mapY: Double? = null
    ): KakaoSearchResponse

    @GET("api/image/exact")
    suspend fun getExactImages(
        @Query("tourId") tourId: String? = null,
        @Query("title") title: String? = null,
        @Query("mapX") mapX: Double? = null,
        @Query("mapY") mapY: Double? = null
    ): List<String>

    @GET("api/restaurants/nearby")
    suspend fun getTourApiNearbyRestaurants(
        @Query("mapX") lng: Double,
        @Query("mapY") lat: Double,
        @Query("radius") radius: Int
    ): List<RestaurantSummary>

    @GET("api/tour/menu-images")
    suspend fun getTourMenuImages(
        @Query("contentId") contentId: String
    ): List<String>

    @GET("api/tour/location")
    suspend fun getNearbyRestaurants(
        @Query("mapX") lng: Double,
        @Query("mapY") lat: Double,
        @Query("radius") radius: Int
    ): TourLocationResponse

    @GET("api/tour/detail")
    suspend fun getRestaurantDetails(
        @Query("contentId") contentId: String
    ): TourDetailIntroResponse

    @GET("api/tour/reviews")
    suspend fun getPlaceReviews(
        @Query("placeName") placeName: String,
        @Query("address") address: String,
        @Query("start") start: Int
    ): List<BlogReview>
}

object RetrofitClient {
    private val BASE_URL = BuildConfig.SERVER_URL

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
