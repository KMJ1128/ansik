package com.kmj.ansik.ui

import com.kmj.ansik.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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
        @Query("mapY") mapY: Double? = null,
        @Query("lang") language: String = "ko"
    ): List<String>

    @GET("api/restaurants/nearby")
    suspend fun getTourApiNearbyRestaurants(
        @Query("mapX") lng: Double,
        @Query("mapY") lat: Double,
        @Query("radius") radius: Int,
        @Query("lang") language: String
    ): List<RestaurantSummary>

    @GET("api/restaurants/menu-guide")
    suspend fun getRestaurantMenuGuide(
        @Query("restaurantName") restaurantName: String,
        @Query("address") address: String,
        @Query("lang") language: String,
        @Query("menuHints") menuHints: List<String> = emptyList()
    ): RestaurantMenuGuide

    @GET("api/tour/menu-images")
    suspend fun getTourMenuImages(
        @Query("contentId") contentId: String,
        @Query("lang") language: String
    ): List<String>

    @GET("api/tour/location")
    suspend fun getNearbyRestaurants(
        @Query("mapX") lng: Double,
        @Query("mapY") lat: Double,
        @Query("radius") radius: Int,
        @Query("lang") language: String
    ): TourLocationResponse

    @GET("api/tour/detail")
    suspend fun getRestaurantDetails(
        @Query("contentId") contentId: String,
        @Query("lang") language: String
    ): TourDetailIntroResponse

    @GET("api/menu/profile")
    suspend fun getMenuProfile(
        @Query("menuName") menuName: String,
        @Query("lang") language: String
    ): MenuProfile

    @GET("api/menu/images")
    suspend fun getMenuImages(
        @Query("menuName") menuName: String
    ): List<String>

    @GET("api/tour/reviews")
    suspend fun getPlaceReviews(
        @Query("placeName") placeName: String,
        @Query("address") address: String,
        @Query("start") start: Int
    ): List<BlogReview>
}

object RetrofitClient {
    private val BASE_URL = BuildConfig.SERVER_URL
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
