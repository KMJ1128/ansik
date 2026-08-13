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

    // 💡 네이버 이미지 검색으로 롤백
    @GET("api/image")
    suspend fun searchImageNaver(
        @Query("query") query: String
    ): NaverImageResponse

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