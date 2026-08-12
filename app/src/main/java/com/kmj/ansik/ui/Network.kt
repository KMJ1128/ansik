package com.kmj.ansik.ui

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {

    // ========================================================
    // Kakao Local API (장소 검색용으로 유지)
    // ========================================================
    @GET("v2/local/search/keyword.json")
    suspend fun searchPlace(
        @Header("Authorization")
        apiKey: String = "KakaoAK ea731a9e930f5151c7ccc30d50d0996e",
        @Query("query")
        query: String
    ): KakaoSearchResponse

    // ========================================================
    // Kakao Image API (기존 유지)
    // ========================================================
    @GET("v2/search/image")
    suspend fun searchImage(
        @Header("Authorization")
        apiKey: String = "KakaoAK ea731a9e930f5151c7ccc30d50d0996e",
        @Query("query")
        query: String,
        @Query("size")
        size: Int = 1
    ): KakaoImageResponse

    // ========================================================
    // 💡 NCP NAVER API HUB Image API (수정됨)
    // ========================================================
    @GET("https://naverapihub.apigw.ntruss.com/search/v1/image")
    suspend fun searchImageNaver(
        // NAVER API HUB 전용 헤더와 제공해주신 키 적용
        @Header("X-NCP-APIGW-API-KEY-ID")
        clientId: String = "fibxb4zdoc",
        @Header("X-NCP-APIGW-API-KEY")
        clientSecret: String = "0cNuSzrIWvE1z4xhFGQUF171JEDDN7IT1fLdjTnz",
        @Query("query")
        query: String,
        @Query("display")
        display: Int = 1,
        @Query("sort")
        sort: String = "sim"
    ): NaverImageResponse

    // ========================================================
    // TourAPI
    // ========================================================
    @GET("https://apis.data.go.kr/B551011/KorService2/locationBasedList2")
    suspend fun getNearbyRestaurants(
        @Query("serviceKey", encoded = true)
        serviceKey: String = "ecb776bfdd4b1760b36175140757e31841453b0c1aa8c5ac261dcf182a6ab4bf",
        @Query("MobileOS")
        mobileOS: String = "AND",
        @Query("MobileApp")
        mobileApp: String = "Ansik",
        @Query("_type")
        type: String = "json",
        @Query("mapX")
        lng: Double,
        @Query("mapY")
        lat: Double,
        @Query("radius")
        radius: Int = 5000,
        @Query("contentTypeId")
        contentTypeId: Int = 39,
        @Query("numOfRows")
        numOfRows: Int = 20,
        @Query("pageNo")
        pageNo: Int = 1,
        @Query("arrange")
        arrange: String = "E"
    ): TourLocationResponse

    @GET("https://apis.data.go.kr/B551011/KorService2/detailIntro2")
    suspend fun getRestaurantDetails(
        @Query("serviceKey", encoded = true)
        serviceKey: String = "ecb776bfdd4b1760b36175140757e31841453b0c1aa8c5ac261dcf182a6ab4bf",
        @Query("MobileOS")
        mobileOS: String = "AND",
        @Query("MobileApp")
        mobileApp: String = "Ansik",
        @Query("_type")
        type: String = "json",
        @Query("contentId")
        contentId: String,
        @Query("contentTypeId")
        contentTypeId: Int = 39,
        @Query("numOfRows")
        numOfRows: Int = 1,
        @Query("pageNo")
        pageNo: Int = 1
    ): TourDetailIntroResponse
}

object RetrofitClient {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/") // 공통 BaseUrl (절대경로 사용 시 무시됨)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}