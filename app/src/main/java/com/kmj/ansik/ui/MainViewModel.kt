package com.kmj.ansik.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    // ========================================================
    // 사용자 건강 / 식단 조건
    // ========================================================

    var selectedConditions = mutableStateOf<Set<String>>(emptySet())
        private set

    // ========================================================
    // 여행 일정
    // ========================================================

    val travelRoute = mutableStateListOf<PlaceInfo>()

    val recommendedPlaces = mutableStateListOf<PlaceInfo>()

    var nights = mutableStateOf(3)

    var days = mutableStateOf(4)

    var currentSelectedDay = mutableStateOf(1)

    // ========================================================
    // 장소 검색
    // ========================================================

    var searchQuery = mutableStateOf("")

    var isSearchActive = mutableStateOf(false)

    var selectedPlace = mutableStateOf<PlaceInfo?>(null)

    private var searchJob: Job? = null

    // ========================================================
    // TourAPI
    // ========================================================

    /**
     * 지도에 표시할 주변 식당 목록
     */
    val nearbyRestaurants =
        mutableStateListOf<TourRestaurant>()

    var isFetchingRestaurants =
        mutableStateOf(false)

    /**
     * 현재 선택된 식당 상세정보
     *
     * 현재 UI에서는 별도 팝업을 띄우지 않고
     * 로그 확인 / 추후 상세 UI 확장용으로 유지합니다.
     */
    var selectedRestaurantDetail =
        mutableStateOf<TourRestaurantDetail?>(null)

    // ========================================================
    // Kakao 장소 검색
    // ========================================================

    fun searchPlacesRealtime(query: String) {

        searchQuery.value = query

        isSearchActive.value =
            query.isNotEmpty()

        if (query.isBlank()) {

            recommendedPlaces.clear()

            return
        }

        searchJob?.cancel()

        searchJob =
            viewModelScope.launch {

                delay(400)

                try {

                    val placesWithImages =
                        withContext(Dispatchers.IO) {

                            val response =
                                RetrofitClient.api.searchPlace(
                                    query = query
                                )

                            coroutineScope {

                                response.documents
                                    .map { place ->

                                        async {

                                            val imageUrl =
                                                try {

                                                    val imageResponse =
                                                        RetrofitClient.api.searchImage(
                                                            query = place.place_name
                                                        )

                                                    imageResponse
                                                        .documents
                                                        .firstOrNull()
                                                        ?.image_url
                                                        ?: DEFAULT_IMAGE_URL

                                                } catch (
                                                    e: Exception
                                                ) {

                                                    DEFAULT_IMAGE_URL
                                                }

                                            val shortTag =
                                                place.category_group_name
                                                    .split(">")
                                                    .lastOrNull()
                                                    ?.trim()
                                                    .orEmpty()

                                            PlaceInfo(

                                                name =
                                                    place.place_name,

                                                address =
                                                    place.road_address_name
                                                        .ifEmpty {
                                                            "주소 없음"
                                                        },

                                                tag =
                                                    shortTag.ifEmpty {
                                                        "장소"
                                                    },

                                                imageUrl =
                                                    imageUrl,

                                                latitude =
                                                    place.y
                                                        .toDoubleOrNull()
                                                        ?: 0.0,

                                                longitude =
                                                    place.x
                                                        .toDoubleOrNull()
                                                        ?: 0.0,

                                                day =
                                                    currentSelectedDay.value
                                            )
                                        }
                                    }
                                    .awaitAll()
                            }
                        }

                    recommendedPlaces.clear()

                    recommendedPlaces.addAll(
                        placesWithImages
                    )

                } catch (e: Exception) {

                    android.util.Log.e(
                        "KakaoSearch",
                        "검색 통신 실패",
                        e
                    )
                }
            }
    }

    // ========================================================
    // 지도에서 장소 선택
    // ========================================================

    fun selectLocationFromMap(
        name: String,
        lat: Double,
        lng: Double
    ) {

        viewModelScope.launch {

            try {

                val newPlace =
                    withContext(Dispatchers.IO) {

                        val searchRes =
                            RetrofitClient.api.searchPlace(
                                query = name
                            )

                        val matchedPlace =
                            searchRes.documents
                                .firstOrNull()

                        val imageRes =
                            RetrofitClient.api.searchImage(
                                query = name
                            )

                        val imageUrl =
                            imageRes
                                .documents
                                .firstOrNull()
                                ?.image_url
                                ?: DEFAULT_IMAGE_URL

                        val shortTag =
                            matchedPlace
                                ?.category_group_name
                                ?.split(">")
                                ?.lastOrNull()
                                ?.trim()
                                ?: "관심 위치"

                        val address =
                            matchedPlace
                                ?.road_address_name
                                ?.ifEmpty {
                                    "주소 없음"
                                }
                                ?: "지도에서 선택한 장소"

                        PlaceInfo(

                            name = name,

                            address = address,

                            tag = shortTag,

                            imageUrl = imageUrl,

                            latitude = lat,

                            longitude = lng,

                            day =
                                currentSelectedDay.value
                        )
                    }

                selectedPlace.value =
                    newPlace

                isSearchActive.value =
                    false

                searchQuery.value =
                    ""

                nearbyRestaurants.clear()

                selectedRestaurantDetail.value =
                    null

            } catch (e: Exception) {

                android.util.Log.e(
                    "MapClick",
                    "지도 심볼 통신 실패",
                    e
                )
            }
        }
    }

    // ========================================================
    // TourAPI
    // 주변 음식점 검색
    // ========================================================

    fun searchNearbyRestaurants(
        lat: Double,
        lng: Double
    ) {

        viewModelScope.launch {

            isFetchingRestaurants.value =
                true

            selectedRestaurantDetail.value =
                null

            try {

                android.util.Log.d(
                    "TourAPI",
                    "==============================="
                )

                android.util.Log.d(
                    "TourAPI",
                    "주변 식당 검색 시작"
                )

                android.util.Log.d(
                    "TourAPI",
                    "lat = $lat, lng = $lng"
                )

                val response =
                    withContext(Dispatchers.IO) {

                        RetrofitClient.api
                            .getNearbyRestaurants(
                                lng = lng,
                                lat = lat
                            )
                    }

                val items =
                    response
                        .response
                        ?.body
                        ?.items
                        ?.item
                        ?: emptyList()

                android.util.Log.d(
                    "TourAPI",
                    "검색 결과 = ${items.size}건"
                )

                // 기존 식당 핀 제거
                nearbyRestaurants.clear()

                // 새 식당 목록 저장
                nearbyRestaurants.addAll(
                    items
                )

                // ====================================================
                // 중요
                // ====================================================
                //
                // 기존에 선택되어 있던 장소 팝업을 닫습니다.
                //
                // 따라서 "주변 식당 찾기"를 누르면
                // 하단 장소 카드가 사라지고
                // 지도만 보이게 됩니다.
                //
                selectedPlace.value = null

                isSearchActive.value =
                    false

                searchQuery.value =
                    ""

                android.util.Log.d(
                    "TourAPI",
                    "식당 지도 핀 표시 준비 완료"
                )

                android.util.Log.d(
                    "TourAPI",
                    "==============================="

                )

            } catch (e: retrofit2.HttpException) {

                android.util.Log.e(
                    "TourAPI",
                    "HTTP 오류 코드 = ${e.code()}"
                )

                android.util.Log.e(
                    "TourAPI",
                    "HTTP 오류 응답 = ${
                        e.response()
                            ?.errorBody()
                            ?.string()
                    }"
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "TourAPI",
                    "주변 식당 검색 실패",
                    e
                )

            } finally {

                isFetchingRestaurants.value =
                    false
            }
        }
    }

    // ========================================================
    // TourAPI
    // 식당 상세정보
    // ========================================================

    fun fetchRestaurantDetail(
        contentId: String
    ) {

        viewModelScope.launch {

            try {

                android.util.Log.d(
                    "TourAPI_DETAIL",
                    "================================"
                )

                android.util.Log.d(
                    "TourAPI_DETAIL",
                    "식당 상세정보 요청"
                )

                android.util.Log.d(
                    "TourAPI_DETAIL",
                    "contentId = $contentId"
                )

                val response =
                    withContext(Dispatchers.IO) {

                        RetrofitClient.api
                            .getRestaurantDetails(
                                contentId = contentId
                            )
                    }

                android.util.Log.d(
                    "TourAPI_DETAIL",
                    "TourAPI 응답 수신"
                )

                val items =
                    response
                        .response
                        ?.body
                        ?.items
                        ?.item

                android.util.Log.d(
                    "TourAPI_DETAIL",
                    "응답 item 개수 = ${
                        items?.size ?: 0
                    }"
                )

                val detail =
                    items?.firstOrNull()

                if (detail != null) {

                    android.util.Log.d(
                        "TourAPI_DETAIL",
                        "firstmenu = ${detail.firstmenu}"
                    )

                    android.util.Log.d(
                        "TourAPI_DETAIL",
                        "treatmenu = ${detail.treatmenu}"
                    )

                    android.util.Log.d(
                        "TourAPI_DETAIL",
                        "kidsfacility = ${detail.kidsfacility}"
                    )

                    android.util.Log.d(
                        "TourAPI_DETAIL",
                        "parkingfood = ${detail.parkingfood}"
                    )

                    selectedRestaurantDetail.value =
                        detail

                } else {

                    android.util.Log.w(
                        "TourAPI_DETAIL",
                        "상세정보 item이 없습니다."
                    )

                    selectedRestaurantDetail.value =
                        null
                }

                android.util.Log.d(
                    "TourAPI_DETAIL",
                    "================================"
                )

            } catch (e: retrofit2.HttpException) {

                android.util.Log.e(
                    "TourAPI_DETAIL",
                    "HTTP 오류 코드 = ${e.code()}"
                )

                android.util.Log.e(
                    "TourAPI_DETAIL",
                    "HTTP 오류 응답 = ${
                        e.response()
                            ?.errorBody()
                            ?.string()
                    }")

                selectedRestaurantDetail.value =
                    null

            } catch (e: Exception) {

                android.util.Log.e(
                    "TourAPI_DETAIL",
                    "식당 상세 검색 실패",
                    e
                )

                selectedRestaurantDetail.value =
                    null
            }
        }
    }

    // ========================================================
    // 일정 Day 변경
    // ========================================================

    fun changePlaceDay(
        place: PlaceInfo,
        newDay: Int
    ) {

        val index =
            travelRoute.indexOfFirst {
                it.id == place.id
            }

        if (index != -1) {

            travelRoute[index] =
                travelRoute[index].copy(
                    day = newDay
                )

            currentSelectedDay.value =
                newDay

            travelRoute.sortBy {
                it.day
            }
        }
    }

    // ========================================================
    // 특정 Day 좌표
    // ========================================================

    fun getRouteCoordsForDay(
        day: Int
    ): List<LatLng> {

        return travelRoute
            .filter {
                it.day == day
            }
            .map {

                LatLng(
                    it.latitude,
                    it.longitude
                )
            }
    }

    // ========================================================
    // 건강 조건 선택
    // ========================================================

    fun toggleCondition(
        condition: String
    ) {

        val current =
            selectedConditions.value

        selectedConditions.value =
            if (
                current.contains(condition)
            ) {

                current - condition

            } else {

                current + condition
            }
    }

    // ========================================================
    // 여행 일정에 장소 추가
    // ========================================================

    fun addPlaceToRoute(
        place: PlaceInfo
    ) {

        if (
            !travelRoute.any {
                it.name == place.name
            }
        ) {

            travelRoute.add(
                place.copy(
                    day =
                        currentSelectedDay.value
                )
            )

            travelRoute.sortBy {
                it.day
            }
        }

        selectedPlace.value =
            null

        searchQuery.value =
            ""

        isSearchActive.value =
            false

        nearbyRestaurants.clear()

        selectedRestaurantDetail.value =
            null
    }

    // ========================================================
    // 장소 삭제
    // ========================================================

    fun removePlace(
        place: PlaceInfo
    ) {

        travelRoute.remove(
            place
        )
    }

    // ========================================================
    // 장소 순서 변경
    // ========================================================

    fun movePlace(
        fromIndex: Int,
        toIndex: Int
    ) {

        if (
            fromIndex !in travelRoute.indices ||
            toIndex !in travelRoute.indices
        ) {

            return
        }

        val item =
            travelRoute.removeAt(
                fromIndex
            )

        travelRoute.add(
            toIndex,
            item
        )
    }

    companion object {

        private const val DEFAULT_IMAGE_URL =
            "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"
    }
}