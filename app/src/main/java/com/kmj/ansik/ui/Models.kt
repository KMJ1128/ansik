package com.kmj.ansik.ui

import java.util.UUID

// ============================================================
// 여행 장소
// ============================================================

data class TravelSpot(

    val id: String =
        UUID.randomUUID().toString(),

    val name: String
)

// ============================================================
// 일정 장소
// ============================================================

data class PlaceInfo(

    val id: String =
        UUID.randomUUID().toString(),

    val name: String,

    val address: String,

    val tag: String,

    val imageUrl: String,

    val latitude: Double,

    val longitude: Double,

    var day: Int = 1
)

// ============================================================
// Kakao Local API
// ============================================================

data class KakaoSearchResponse(

    val documents:
    List<KakaoPlace> =
        emptyList()
)

data class KakaoPlace(

    val place_name: String = "",

    val road_address_name: String = "",

    val category_group_name: String = "",

    val x: String = "",

    val y: String = ""
)

// ============================================================
// Kakao Image API
// ============================================================

data class KakaoImageResponse(

    val documents:
    List<KakaoImageDocument> =
        emptyList()
)

data class KakaoImageDocument(

    val image_url: String = ""
)

// ============================================================
// TourAPI 공통 응답
// ============================================================

data class TourApiResponse<T>(

    val response:
    TourApiResponseBody<T>? =
        null
)

data class TourApiResponseBody<T>(

    val header:
    TourApiHeader? =
        null,

    val body:
    TourApiBody<T>? =
        null
)

data class TourApiHeader(

    val resultCode: String? =
        null,

    val resultMsg: String? =
        null
)

data class TourApiBody<T>(

    val items:
    TourApiItems<T>? =
        null,

    val numOfRows: Int? =
        null,

    val pageNo: Int? =
        null,

    val totalCount: Int? =
        null
)

data class TourApiItems<T>(

    val item:
    List<T>? =
        null
)

// ============================================================
// TourAPI
// 위치 기반 음식점 검색
// ============================================================

typealias TourLocationResponse =
        TourApiResponse<TourRestaurant>

data class TourRestaurant(

    val contentid: String = "",

    val contenttypeid: String = "",

    val title: String = "",

    val mapx: String = "",

    val mapy: String = "",

    val addr1: String = "",

    val addr2: String = "",

    val firstimage: String = "",

    val firstimage2: String = ""
)

// ============================================================
// TourAPI
// 음식점 상세정보
// ============================================================

typealias TourDetailIntroResponse =
        TourApiResponse<TourRestaurantDetail>

data class TourRestaurantDetail(

    val contentid: String? =
        null,

    val contenttypeid: String? =
        null,

    val firstmenu: String? =
        null,

    val treatmenu: String? =
        null,

    val kidsfacility: String? =
        null,

    val parkingfood: String? =
        null,

    val packing: String? =
        null,

    val seat: String? =
        null,

    val smoking: String? =
        null,

    val creditcardfood: String? =
        null,

    val reservationfood: String? =
        null,

    val opentimefood: String? =
        null,

    val restdatefood: String? =
        null
)