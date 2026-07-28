package com.kmj.ansik.ui

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// 카카오 API가 주는 응답 데이터 구조
data class KakaoSearchResponse(val documents: List<KakaoPlace>)
data class KakaoPlace(
    val place_name: String,
    val road_address_name: String,
    val category_group_name: String,
    val x: String, // 경도(longitude)
    val y: String  // 위도(latitude)
)

// 서버에 요청하는 명세서
interface KakaoSearchApi {
    @GET("v2/local/search/keyword.json")
    suspend fun searchPlace(
        // 🚀 TODO: 나중에 "여기에_키_입력" 부분을 본인의 카카오 REST API 키로 바꿔야 합니다!
        @Header("Authorization") apiKey: String = "7a4ce9cf18bcb59b25bda5495b554471",
        @Query("query") query: String
    ): KakaoSearchResponse
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