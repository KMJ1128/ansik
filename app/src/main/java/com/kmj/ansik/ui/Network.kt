package com.kmj.ansik.ui

import com.kmj.ansik.BuildConfig // 본인 앱 패키지명에 맞게 BuildConfig를 import 해야 합니다.
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ============================================================
// 1. ApiService 인터페이스 (서버의 API 주소들)
// ============================================================
interface ApiService {

    @GET("api/place")
    suspend fun searchPlace(
        @Query("query") query: String
    ): KakaoSearchResponse

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

    // 🔥 리뷰 API 호출 함수
    @GET("api/tour/reviews")
    suspend fun getPlaceReviews(
        @Query("placeName") placeName: String
    ): List<BlogReview>
}

// ============================================================
// 2. RetrofitClient 객체
// ============================================================
object RetrofitClient {
    // 💡 BuildConfig를 통해 local.properties에 설정된 URL을 가져옵니다.
    // 주의: BuildConfig가 제대로 생성되려면 build.gradle 설정이 필요합니다.
    private val BASE_URL = BuildConfig.SERVER_URL

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}