package com.kmj.ansik.ui

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// 1. 카카오 장소 검색 응답 구조
data class KakaoSearchResponse(val documents: List<KakaoPlace>)
data class KakaoPlace(
    val place_name: String,
    val road_address_name: String,
    val category_group_name: String,
    val x: String, // 경도(longitude)
    val y: String  // 위도(latitude)
)

// 2. 카카오 이미지 검색 응답 구조
data class KakaoImageResponse(val documents: List<KakaoImageDocument>)
data class KakaoImageDocument(val image_url: String)

// 3. 서버 요청 명세서
interface KakaoSearchApi {
    @GET("v2/local/search/keyword.json")
    suspend fun searchPlace(
        @Header("Authorization") apiKey: String = "KakaoAK ea731a9e930f5151c7ccc30d50d0996e",
        @Query("query") query: String
    ): KakaoSearchResponse

    // 해당 장소의 사진을 가져오는 API
    @GET("v2/search/image")
    suspend fun searchImage(
        @Header("Authorization") apiKey: String = "KakaoAK ea731a9e930f5151c7ccc30d50d0996e",
        @Query("query") query: String,
        @Query("size") size: Int = 1
    ): KakaoImageResponse
}

// 통신 객체 생성
object RetrofitClient {
    val api: KakaoSearchApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoSearchApi::class.java)
    }
}