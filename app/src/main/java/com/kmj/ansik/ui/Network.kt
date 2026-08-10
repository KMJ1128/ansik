package com.kmj.ansik.ui

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {

    // ========================================================
    // Kakao Local API
    // ========================================================

    @GET("v2/local/search/keyword.json")
    suspend fun searchPlace(

        @Header("Authorization")
        apiKey: String =
            "KakaoAK ea731a9e930f5151c7ccc30d50d0996e",

        @Query("query")
        query: String

    ): KakaoSearchResponse


    // ========================================================
    // Kakao Image API
    // ========================================================

    @GET("v2/search/image")
    suspend fun searchImage(

        @Header("Authorization")
        apiKey: String =
            "KakaoAK ea731a9e930f5151c7ccc30d50d0996e",

        @Query("query")
        query: String,

        @Query("size")
        size: Int = 1

    ): KakaoImageResponse


    // ========================================================
    // TourAPI
    // 위치 기반 관광정보
    //
    // 음식점:
    // contentTypeId = 39
    // ========================================================

    @GET(
        "https://apis.data.go.kr/B551011/KorService2/locationBasedList2"
    )
    suspend fun getNearbyRestaurants(

        @Query(
            "serviceKey",
            encoded = true
        )
        serviceKey: String =
            "ecb776bfdd4b1760b36175140757e31841453b0c1aa8c5ac261dcf182a6ab4bf",

        @Query("MobileOS")
        mobileOS: String =
            "AND",

        @Query("MobileApp")
        mobileApp: String =
            "Ansik",

        @Query("_type")
        type: String =
            "json",

        @Query("mapX")
        lng: Double,

        @Query("mapY")
        lat: Double,

        @Query("radius")
        radius: Int =
            5000,

        @Query("contentTypeId")
        contentTypeId: Int =
            39,

        @Query("numOfRows")
        numOfRows: Int =
            20,

        @Query("pageNo")
        pageNo: Int =
            1,

        @Query("arrange")
        arrange: String =
            "E"

    ): TourLocationResponse


    // ========================================================
    // TourAPI
    // 음식점 상세정보
    //
    // 중요:
    // detailIntro1 ❌
    // detailIntro2 ✅
    // ========================================================

    @GET(
        "https://apis.data.go.kr/B551011/KorService2/detailIntro2"
    )
    suspend fun getRestaurantDetails(

        @Query(
            "serviceKey",
            encoded = true
        )
        serviceKey: String =
            "ecb776bfdd4b1760b36175140757e31841453b0c1aa8c5ac261dcf182a6ab4bf",

        @Query("MobileOS")
        mobileOS: String =
            "AND",

        @Query("MobileApp")
        mobileApp: String =
            "Ansik",

        @Query("_type")
        type: String =
            "json",

        @Query("contentId")
        contentId: String,

        @Query("contentTypeId")
        contentTypeId: Int =
            39,

        @Query("numOfRows")
        numOfRows: Int =
            1,

        @Query("pageNo")
        pageNo: Int =
            1

    ): TourDetailIntroResponse
}


// ============================================================
// Retrofit
// ============================================================

object RetrofitClient {

    val api: ApiService by lazy {

        Retrofit.Builder()

            .baseUrl(
                "https://dapi.kakao.com/"
            )

            .addConverterFactory(
                GsonConverterFactory.create()
            )

            .build()

            .create(
                ApiService::class.java
            )
    }
}