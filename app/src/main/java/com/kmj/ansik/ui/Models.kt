package com.kmj.ansik.ui

import java.util.UUID

data class TravelSpot(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class PlaceInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val tag: String,
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val latitude: Double,
    val longitude: Double,
    var day: Int = 1
)

data class KakaoSearchResponse(
    val documents: List<KakaoPlace> = emptyList()
)

data class KakaoPlace(
    val id: String = "",
    val place_name: String = "",
    val road_address_name: String = "",
    val category_group_name: String = "",
    val x: String = "",
    val y: String = ""
)

data class NaverImageResponse(
    val items: List<NaverImageItem> = emptyList()
)

data class NaverImageItem(
    val title: String = "",
    val link: String = "",
    val thumbnail: String = ""
)

data class TourApiResponse<T>(
    val response: TourApiBodyOuter<T>? = null
)

data class TourApiBodyOuter<T>(
    val body: TourApiBodyInner<T>? = null
)

data class TourApiBodyInner<T>(
    val items: TourApiItems<T>? = null,
    val numOfRows: Int? = null,
    val pageNo: Int? = null,
    val totalCount: Int? = null
)

data class TourApiItems<T>(
    val item: List<T>? = null
)

typealias TourLocationResponse = TourApiResponse<TourRestaurant>

data class TourRestaurant(
    val contentid: String = "",
    val contenttypeid: String = "",
    val title: String = "",
    val mapx: String = "",
    val mapy: String = "",
    val addr1: String = "",
    val addr2: String = "",
    val firstimage: String = "",
    val firstimage2: String = "",
    val imageUrls: List<String> = emptyList()
)

data class RestaurantSummary(
    val id: String = "",
    val title: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val tourContentId: String? = null,
    val kakaoPlaceId: String? = null,
    val sources: List<String> = emptyList(),
    val distanceMeters: Int = 0
) {
    val hasTourData: Boolean
        get() = !tourContentId.isNullOrBlank()
}

typealias TourDetailIntroResponse = TourApiResponse<TourRestaurantDetail>

data class TourRestaurantDetail(
    val contentid: String? = null,
    val contenttypeid: String? = null,
    val firstmenu: String? = null,
    val treatmenu: String? = null,
    val kidsfacility: String? = null,
    val parkingfood: String? = null,
    val packing: String? = null,
    val seat: String? = null,
    val smoking: String? = null,
    val chkcreditcardfood: String? = null,
    val reservationfood: String? = null,
    val opentimefood: String? = null
)

enum class RiskStatus {
    GREEN,
    YELLOW,
    RED,
    UNKNOWN
}

enum class MenuEvidenceType {
    TOUR_MAIN_MENU,
    TOUR_TREAT_MENU,
    TOUR_MENU_IMAGE,
    OFFICIAL_MENU,
    USER_MENU_IMAGE,
    UNKNOWN
}

data class RestaurantDetailState(
    val restaurant: RestaurantSummary,
    val tourDetail: TourRestaurantDetail? = null,
    val menuImages: List<String> = emptyList(),
    val riskStatus: RiskStatus = RiskStatus.UNKNOWN
) {
    val hasMenuEvidence: Boolean
        get() = !tourDetail?.firstmenu.isNullOrBlank() ||
            !tourDetail?.treatmenu.isNullOrBlank() ||
            menuImages.isNotEmpty()
}

data class BlogReview(
    val title: String = "",
    val description: String = "",
    val link: String = ""
)
