package com.kmj.ansik.ui

import com.kmj.ansik.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/place")
    suspend fun searchPlace(
        @Query("query") query: String
    ): KakaoSearchResponse

    // 🔥 구글 API 정밀 검색을 위해 위도/경도 파라미터 적용
    @GET("api/image/exact")
    suspend fun getExactImages(
        @Query("tourId") tourId: String? = null,
        @Query("title") title: String? = null,
        @Query("mapX") mapX: Double? = null,
        @Query("mapY") mapY: Double? = null
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

    @GET("api/tour/popular-places")
    suspend fun getPopularPlaces(
        @Query("mapX") lng: Double,
        @Query("mapY") lat: Double
    ): TourLocationResponse

    @GET("api/tour/popular-restaurants")
    suspend fun getPopularRestaurants(
        @Query("mapX") lng: Double,
        @Query("mapY") lat: Double
    ): TourLocationResponse
}

object RetrofitClient {

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}