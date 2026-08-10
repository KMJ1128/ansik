package com.kmj.ansik.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PathOverlay
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


// ============================================================
// Day 색상
// ============================================================

private val MapScreenDayColorPalette =
    listOf(

        Color(0xFFE53935),
        Color(0xFFFF7043),
        Color(0xFFFFCA28),
        Color(0xFF43A047),
        Color(0xFF1E88E5),
        Color(0xFF3949AB),
        Color(0xFF8E24AA),
        Color(0xFFD81B60),
        Color(0xFF6D4C41),
        Color(0xFF546E7A)
    )


private fun getMapDayColor(
    day: Int
): Color {

    if (day < 1) {
        return Color.Gray
    }

    val index =
        (day - 1) %
                MapScreenDayColorPalette.size

    return MapScreenDayColorPalette[index]
}


// ============================================================
// 일정 번호 Marker
// ============================================================

@Composable
private fun rememberNumberedMarker(
    number: Int,
    composeColor: Color
): OverlayImage {

    return remember(
        number,
        composeColor
    ) {

        val size =
            90

        val bitmap =
            Bitmap.createBitmap(
                size,
                size,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        val paint =
            Paint().apply {

                isAntiAlias =
                    true

                color =
                    composeColor.toArgb()
            }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            size / 2f,
            paint
        )

        val borderPaint =
            Paint().apply {

                isAntiAlias =
                    true

                color =
                    android.graphics.Color.WHITE

                style =
                    Paint.Style.STROKE

                strokeWidth =
                    6f
            }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            (size / 2f) - 3f,
            borderPaint
        )

        val textPaint =
            Paint().apply {

                isAntiAlias =
                    true

                color =
                    android.graphics.Color.WHITE

                textSize =
                    42f

                typeface =
                    Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                    )

                textAlign =
                    Paint.Align.CENTER
            }

        val fontMetrics =
            textPaint.fontMetrics

        val textY =
            (size / 2f) -
                    (
                            fontMetrics.ascent +
                                    fontMetrics.descent
                            ) / 2f

        canvas.drawText(
            number.toString(),
            size / 2f,
            textY,
            textPaint
        )

        OverlayImage.fromBitmap(
            bitmap
        )
    }
}


// ============================================================
// 식당 Marker
// ============================================================

@Composable
private fun rememberRestaurantMarker(): OverlayImage {

    return remember {

        val size =
            80

        val bitmap =
            Bitmap.createBitmap(
                size,
                size,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        // 바깥 원
        val outerPaint =
            Paint().apply {

                isAntiAlias =
                    true

                color =
                    android.graphics.Color.rgb(
                        25,
                        118,
                        210
                    )
            }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            size / 2f,
            outerPaint
        )

        // 흰색 내부
        val innerPaint =
            Paint().apply {

                isAntiAlias =
                    true

                color =
                    android.graphics.Color.WHITE
            }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            27f,
            innerPaint
        )

        // 식당 아이콘 느낌의 포크/숫자 대신
        // 작은 점을 넣어 깔끔한 지도용 Marker로 사용
        val centerPaint =
            Paint().apply {

                isAntiAlias =
                    true

                color =
                    android.graphics.Color.rgb(
                        25,
                        118,
                        210
                    )
            }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            10f,
            centerPaint
        )

        OverlayImage.fromBitmap(
            bitmap
        )
    }
}


// ============================================================
// MapScreen
// ============================================================

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalNaverMapApi::class
)
@Composable
fun MapScreen(
    viewModel: MainViewModel
) {

    // ========================================================
    // Bottom Sheet
    // ========================================================

    val sheetState =
        rememberStandardBottomSheetState(
            initialValue =
                SheetValue.PartiallyExpanded,

            skipHiddenState =
                false
        )

    val scaffoldState =
        rememberBottomSheetScaffoldState(
            bottomSheetState =
                sheetState
        )

    // ========================================================
    // 상태
    // ========================================================

    val searchQuery by
    viewModel.searchQuery

    val isSearchActive by
    viewModel.isSearchActive

    val selectedPlace by
    viewModel.selectedPlace

    val travelRoute =
        viewModel.travelRoute

    val nearbyRestaurants =
        viewModel.nearbyRestaurants

    val cameraPositionState =
        rememberCameraPositionState()

    val keyboardController =
        LocalSoftwareKeyboardController.current

    val context =
        LocalContext.current

    val coroutineScope =
        rememberCoroutineScope()

    val lazyListState =
        rememberLazyListState()

    // ========================================================
    // 식당 Marker
    // ========================================================

    val restaurantMarker =
        rememberRestaurantMarker()

    // ========================================================
    // 일정 Marker Highlight
    // ========================================================

    var highlightedPlaceId
            by remember {

                mutableStateOf<String?>(null)
            }


    // ========================================================
    // 선택 장소 변경 → 카메라 이동
    // ========================================================

    LaunchedEffect(
        selectedPlace
    ) {

        selectedPlace?.let { place ->

            val targetLocation =
                LatLng(
                    place.latitude,
                    place.longitude
                )

            cameraPositionState.animate(
                CameraUpdate.scrollTo(
                    targetLocation
                )
            )
        }
    }


    // ========================================================
    // BottomSheetScaffold
    // ========================================================

    BottomSheetScaffold(

        scaffoldState =
            scaffoldState,

        sheetPeekHeight =
            260.dp,

        sheetContainerColor =
            Color.White,

        sheetShadowElevation =
            16.dp,

        sheetDragHandle =
            null,

        sheetContent = {

            Column(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = 260.dp,
                            max = 720.dp
                        )
            ) {

                // ====================================================
                // Drag Handle
                // ====================================================

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 12.dp,
                                bottom = 8.dp
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Box(

                        modifier =
                            Modifier
                                .width(40.dp)
                                .height(5.dp)
                                .background(
                                    Color(0xFFE0E0E0),
                                    CircleShape
                                )
                    )
                }


                // ====================================================
                // 일정 제목
                // ====================================================

                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(

                                interactionSource =
                                    remember {
                                        MutableInteractionSource()
                                    },

                                indication =
                                    null
                            ) {

                                coroutineScope.launch {

                                    if (
                                        scaffoldState
                                            .bottomSheetState
                                            .currentValue ==
                                        SheetValue.Expanded
                                    ) {

                                        scaffoldState
                                            .bottomSheetState
                                            .partialExpand()

                                    } else {

                                        scaffoldState
                                            .bottomSheetState
                                            .expand()
                                    }
                                }
                            }
                            .padding(
                                horizontal = 24.dp,
                                vertical = 8.dp
                            ),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(

                        "내 여행 일정",

                        fontSize =
                            22.sp,

                        fontWeight =
                            FontWeight.ExtraBold
                    )


                    Surface(

                        color =
                            Color(0xFFF1F8E9),

                        shape =
                            RoundedCornerShape(
                                percent = 50
                            )
                    ) {

                        Row(

                            verticalAlignment =
                                Alignment.CenterVertically,

                            modifier =
                                Modifier.padding(
                                    horizontal = 4.dp
                                )
                        ) {

                            IconButton(

                                onClick = {

                                    if (
                                        viewModel.nights.value > 0
                                    ) {

                                        viewModel.nights.value--

                                        viewModel.days.value--
                                    }
                                },

                                modifier =
                                    Modifier.size(
                                        32.dp
                                    )
                            ) {

                                Text(
                                    "−",
                                    fontSize = 20.sp,
                                    color =
                                        Color(0xFF2E7D32)
                                )
                            }


                            Text(

                                "${viewModel.nights.value}박 ${viewModel.days.value}일",

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    Color(0xFF2E7D32),

                                fontSize =
                                    14.sp
                            )


                            IconButton(

                                onClick = {

                                    viewModel.nights.value++

                                    viewModel.days.value++
                                },

                                modifier =
                                    Modifier.size(
                                        32.dp
                                    )
                            ) {

                                Text(
                                    "+",
                                    fontSize = 20.sp,
                                    color =
                                        Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                // ====================================================
                // 일정 Drag & Drop
                // ====================================================

                val reorderableState =
                    rememberReorderableLazyListState(
                        lazyListState
                    ) { from, to ->

                        viewModel.movePlace(
                            from.index,
                            to.index
                        )
                    }


                LazyColumn(

                    state =
                        lazyListState,

                    userScrollEnabled =
                        false,

                    contentPadding =
                        PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 40.dp
                        ),

                    modifier =
                        Modifier
                            .weight(1f)
                            .pointerInput(Unit) {

                                detectVerticalDragGestures {

                                        change,
                                        dragAmount ->

                                    change.consume()

                                    lazyListState
                                        .dispatchRawDelta(
                                            -dragAmount
                                        )
                                }
                            },

                    verticalArrangement =
                        Arrangement.spacedBy(
                            16.dp
                        )
                ) {

                    items(

                        items =
                            travelRoute,

                        key = {
                            it.id
                        }

                    ) { place ->

                        ReorderableItem(

                            reorderableState,

                            key =
                                place.id

                        ) { isDragging ->

                            val isHighlighted =
                                place.id ==
                                        highlightedPlaceId

                            val dayColor =
                                getMapDayColor(
                                    place.day
                                )


                            Card(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .shadow(

                                            if (
                                                isDragging ||
                                                isHighlighted
                                            ) {
                                                12.dp
                                            } else {
                                                2.dp
                                            },

                                            RoundedCornerShape(
                                                16.dp
                                            ),

                                            spotColor =
                                                Color(
                                                    0x33000000
                                                )
                                        ),

                                shape =
                                    RoundedCornerShape(
                                        16.dp
                                    ),

                                colors =
                                    CardDefaults.cardColors(

                                        containerColor =
                                            if (
                                                isHighlighted
                                            ) {

                                                Color(
                                                    0xFFFFF9C4
                                                )

                                            } else {

                                                Color.White
                                            }
                                    ),

                                elevation =
                                    CardDefaults.cardElevation(
                                        defaultElevation =
                                            0.dp
                                    )
                            ) {

                                Column(

                                    modifier =
                                        Modifier.padding(
                                            16.dp
                                        )
                                ) {

                                    Row(

                                        modifier =
                                            Modifier.fillMaxWidth(),

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Icon(

                                            imageVector =
                                                Icons.Rounded.Menu,

                                            contentDescription =
                                                "순서 변경",

                                            tint =
                                                Color(0xFFD0D0D0),

                                            modifier =
                                                Modifier
                                                    .draggableHandle()
                                                    .size(
                                                        28.dp
                                                    )
                                                    .padding(
                                                        end = 8.dp
                                                    )
                                        )


                                        AsyncImage(

                                            model =
                                                place.imageUrl,

                                            contentDescription =
                                                null,

                                            contentScale =
                                                ContentScale.Crop,

                                            modifier =
                                                Modifier
                                                    .size(
                                                        56.dp
                                                    )
                                                    .clip(
                                                        RoundedCornerShape(
                                                            12.dp
                                                        )
                                                    )
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.width(
                                                    16.dp
                                                )
                                        )


                                        Column(

                                            modifier =
                                                Modifier.weight(
                                                    1f
                                                )
                                        ) {

                                            Text(

                                                place.name,

                                                fontWeight =
                                                    FontWeight.Bold,

                                                fontSize =
                                                    16.sp
                                            )

                                            Spacer(
                                                modifier =
                                                    Modifier.height(
                                                        2.dp
                                                    )
                                            )

                                            Text(

                                                place.tag,

                                                color =
                                                    Color(0xFF757575),

                                                fontSize =
                                                    13.sp
                                            )
                                        }


                                        IconButton(

                                            onClick = {

                                                viewModel.removePlace(
                                                    place
                                                )
                                            },

                                            modifier =
                                                Modifier.size(
                                                    32.dp
                                                )
                                        ) {

                                            Icon(

                                                Icons.Default.Delete,

                                                contentDescription =
                                                    "삭제",

                                                tint =
                                                    Color(0xFFFF8A80)
                                            )
                                        }
                                    }


                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                12.dp
                                            )
                                    )


                                    LazyRow(

                                        modifier =
                                            Modifier.fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement.spacedBy(
                                                8.dp
                                            )
                                    ) {

                                        items(

                                            count =
                                                viewModel.days.value

                                        ) { index ->

                                            val d =
                                                index + 1

                                            val isSelected =
                                                place.day == d

                                            val chipColor =
                                                getMapDayColor(
                                                    d
                                                )


                                            Surface(

                                                modifier =
                                                    Modifier.clickable(

                                                        interactionSource =
                                                            remember {
                                                                MutableInteractionSource()
                                                            },

                                                        indication =
                                                            null
                                                    ) {

                                                        viewModel
                                                            .changePlaceDay(
                                                                place,
                                                                d
                                                            )
                                                    },

                                                shape =
                                                    RoundedCornerShape(
                                                        percent = 50
                                                    ),

                                                color =
                                                    if (
                                                        isSelected
                                                    ) {
                                                        chipColor
                                                    } else {
                                                        Color(
                                                            0xFFF5F5F5
                                                        )
                                                    }
                                            ) {

                                                Text(

                                                    text =
                                                        "Day $d",

                                                    fontSize =
                                                        12.sp,

                                                    fontWeight =
                                                        if (
                                                            isSelected
                                                        ) {
                                                            FontWeight.Bold
                                                        } else {
                                                            FontWeight.Medium
                                                        },

                                                    color =
                                                        if (
                                                            isSelected
                                                        ) {
                                                            Color.White
                                                        } else {
                                                            Color(
                                                                0xFF9E9E9E
                                                            )
                                                        },

                                                    modifier =
                                                        Modifier.padding(
                                                            horizontal =
                                                                12.dp,
                                                            vertical =
                                                                6.dp
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                    item {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    1000.dp
                                )
                        )
                    }
                }
            }
        }

    ) { innerPadding ->


        // ========================================================
        // 지도
        // ========================================================

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
        ) {

            NaverMap(

                modifier =
                    Modifier.fillMaxSize(),

                cameraPositionState =
                    cameraPositionState,

                // ====================================================
                // 네이버 지도 기본 POI 클릭
                // ====================================================

                onSymbolClick = { symbol ->

                    viewModel.selectLocationFromMap(

                        symbol.caption,

                        symbol.position.latitude,

                        symbol.position.longitude
                    )

                    keyboardController?.hide()

                    true
                },

                // ====================================================
                // 지도 빈 곳 클릭
                // ====================================================

                onMapClick = { _, _ ->

                    viewModel.selectedPlace.value =
                        null

                    keyboardController?.hide()
                }

            ) {


                // ====================================================
                // 기존 여행 일정 경로
                // ====================================================

                for (
                d in 1..viewModel.days.value
                ) {

                    val coords =
                        viewModel.getRouteCoordsForDay(
                            d
                        )

                    if (
                        coords.size >= 2
                    ) {

                        PathOverlay(

                            coords =
                                coords,

                            width =
                                5.dp,

                            color =
                                getMapDayColor(
                                    d
                                ),

                            outlineWidth =
                                1.dp,

                            outlineColor =
                                Color.White
                        )
                    }
                }


                // ====================================================
                // 현재 선택된 장소 Marker
                // ====================================================

                selectedPlace?.let { place ->

                    key(
                        "selected_${place.id}"
                    ) {

                        Marker(

                            state =
                                MarkerState(

                                    position =
                                        LatLng(

                                            place.latitude,

                                            place.longitude
                                        )
                                ),

                            icon =
                                MarkerIcons.BLACK,

                            iconTintColor =
                                getMapDayColor(
                                    place.day
                                )
                        )
                    }
                }


                // ====================================================
                // 기존 여행 일정 Marker
                // ====================================================

                travelRoute.forEach { place ->

                    val dayColor =
                        getMapDayColor(
                            place.day
                        )

                    val dayIndex =
                        travelRoute
                            .filter {
                                it.day ==
                                        place.day
                            }
                            .indexOf(place) + 1

                    val customMarkerIcon =
                        rememberNumberedMarker(

                            number =
                                dayIndex,

                            composeColor =
                                dayColor
                        )


                    key(
                        "route_${place.id}"
                    ) {

                        Marker(

                            state =
                                MarkerState(

                                    position =
                                        LatLng(

                                            place.latitude,

                                            place.longitude
                                        )
                                ),

                            icon =
                                customMarkerIcon,

                            onClick = {

                                coroutineScope.launch {

                                    val freshIndex =
                                        viewModel.travelRoute
                                            .indexOfFirst {

                                                it.id ==
                                                        place.id
                                            }

                                    if (
                                        freshIndex != -1
                                    ) {

                                        lazyListState
                                            .animateScrollToItem(
                                                freshIndex
                                            )
                                    }

                                    highlightedPlaceId =
                                        place.id

                                    delay(
                                        800
                                    )

                                    if (
                                        highlightedPlaceId ==
                                        place.id
                                    ) {

                                        highlightedPlaceId =
                                            null
                                    }
                                }

                                true
                            }
                        )
                    }
                }


                // ====================================================
                // ⭐ 주변 식당 Marker
                // ====================================================
                //
                // 여기서 TourAPI의 mapx / mapy를 사용합니다.
                //
                // mapx = 경도
                // mapy = 위도
                //
                // ====================================================

                nearbyRestaurants.forEach { restaurant ->

                    val lng =
                        restaurant.mapx
                            .toDoubleOrNull()

                    val lat =
                        restaurant.mapy
                            .toDoubleOrNull()

                    if (
                        lat != null &&
                        lng != null &&
                        lat != 0.0 &&
                        lng != 0.0
                    ) {

                        key(
                            "restaurant_${restaurant.contentid}"
                        ) {

                            Marker(

                                state =
                                    MarkerState(

                                        position =
                                            LatLng(
                                                lat,
                                                lng
                                            )
                                    ),

                                icon =
                                    restaurantMarker,

                                captionText =
                                    restaurant.title,

                                onClick = {

                                    // 식당 상세정보 조회
                                    viewModel
                                        .fetchRestaurantDetail(
                                            restaurant.contentid
                                        )

                                    true
                                }
                            )
                        }
                    }
                }
            }


            // ========================================================
            // 검색창
            // ========================================================

            Column(

                modifier =
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
                        .padding(
                            top = 24.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                        .fillMaxWidth()
            ) {

                OutlinedTextField(

                    value =
                        searchQuery,

                    onValueChange = {

                        viewModel.searchPlacesRealtime(
                            it
                        )
                    },

                    placeholder = {

                        Text(
                            "어디로 떠나시나요?",
                            color =
                                Color(0xFF9E9E9E)
                        )
                    },

                    leadingIcon = {

                        Icon(

                            Icons.Rounded.Search,

                            contentDescription =
                                "검색",

                            tint =
                                Color(0xFF2E7D32)
                        )
                    },

                    trailingIcon = {

                        if (
                            searchQuery.isNotEmpty()
                        ) {

                            IconButton(

                                onClick = {

                                    viewModel.searchQuery.value =
                                        ""

                                    viewModel.isSearchActive.value =
                                        false
                                }
                            ) {

                                Icon(

                                    Icons.Default.Close,

                                    contentDescription =
                                        "지우기",

                                    tint =
                                        Color.Gray
                                )
                            }
                        }
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .shadow(

                                12.dp,

                                RoundedCornerShape(
                                    percent = 50
                                ),

                                spotColor =
                                    Color(0x26000000)
                            ),

                    shape =
                        RoundedCornerShape(
                            percent = 50
                        ),

                    colors =
                        OutlinedTextFieldDefaults.colors(

                            focusedContainerColor =
                                Color.White.copy(
                                    alpha = 0.98f
                                ),

                            unfocusedContainerColor =
                                Color.White.copy(
                                    alpha = 0.95f
                                ),

                            focusedBorderColor =
                                Color.Transparent,

                            unfocusedBorderColor =
                                Color.Transparent
                        ),

                    singleLine =
                        true
                )


                // ====================================================
                // 검색 결과
                // ====================================================

                AnimatedVisibility(
                    visible =
                        isSearchActive
                ) {

                    Card(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 12.dp
                                )
                                .shadow(

                                    16.dp,

                                    RoundedCornerShape(
                                        20.dp
                                    ),

                                    spotColor =
                                        Color(0x33000000)
                                ),

                        shape =
                            RoundedCornerShape(
                                20.dp
                            ),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White
                            )
                    ) {

                        Column {

                            viewModel
                                .recommendedPlaces
                                .forEach { place ->

                                    Row(

                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable(

                                                    interactionSource =
                                                        remember {
                                                            MutableInteractionSource()
                                                        },

                                                    indication =
                                                        null
                                                ) {

                                                    viewModel.selectedPlace.value =
                                                        place

                                                    viewModel.isSearchActive.value =
                                                        false

                                                    keyboardController?.hide()
                                                }
                                                .padding(

                                                    horizontal =
                                                        20.dp,

                                                    vertical =
                                                        16.dp
                                                ),

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Box(

                                            modifier =
                                                Modifier
                                                    .size(
                                                        36.dp
                                                    )
                                                    .background(

                                                        Color(
                                                            0xFFF5F5F5
                                                        ),

                                                        CircleShape
                                                    ),

                                            contentAlignment =
                                                Alignment.Center
                                        ) {

                                            Icon(

                                                Icons.Rounded.Search,

                                                tint =
                                                    Color(
                                                        0xFFBDBDBD
                                                    ),

                                                contentDescription =
                                                    null,

                                                modifier =
                                                    Modifier.size(
                                                        18.dp
                                                    )
                                            )
                                        }


                                        Spacer(
                                            modifier =
                                                Modifier.width(
                                                    16.dp
                                                )
                                        )


                                        Column(

                                            modifier =
                                                Modifier.weight(
                                                    1f
                                                )
                                        ) {

                                            Row(

                                                verticalAlignment =
                                                    Alignment.CenterVertically
                                            ) {

                                                Text(

                                                    place.name,

                                                    fontSize =
                                                        16.sp,

                                                    fontWeight =
                                                        FontWeight.Bold,

                                                    color =
                                                        Color(
                                                            0xFF212121
                                                        )
                                                )

                                                Spacer(
                                                    modifier =
                                                        Modifier.width(
                                                            8.dp
                                                        )
                                                )

                                                Text(

                                                    place.tag,

                                                    fontSize =
                                                        12.sp,

                                                    color =
                                                        Color(
                                                            0xFF2E7D32
                                                        )
                                                )
                                            }


                                            Spacer(
                                                modifier =
                                                    Modifier.height(
                                                        2.dp
                                                    )
                                            )


                                            Text(

                                                place.address,

                                                fontSize =
                                                    13.sp,

                                                color =
                                                    Color(
                                                        0xFF9E9E9E
                                                    )
                                            )
                                        }
                                    }


                                    HorizontalDivider(

                                        color =
                                            Color(
                                                0xFFF5F5F5
                                            )
                                    )
                                }
                        }
                    }
                }
            }


            // ========================================================
            // 선택 장소 하단 팝업
            // ========================================================
            //
            // 주변 식당 검색 성공 시 selectedPlace = null이 되므로
            // 이 카드 자체가 사라집니다.
            //
            // ========================================================

            AnimatedVisibility(

                visible =
                    selectedPlace != null,

                enter =
                    slideInVertically(
                        initialOffsetY = {
                            it
                        }
                    ) +
                            fadeIn(),

                exit =
                    slideOutVertically(
                        targetOffsetY = {
                            it
                        }
                    ) +
                            fadeOut(),

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .padding(

                            bottom = 24.dp,

                            start = 20.dp,

                            end = 20.dp
                        )
            ) {

                selectedPlace?.let { place ->

                    Card(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .shadow(

                                    20.dp,

                                    RoundedCornerShape(
                                        24.dp
                                    ),

                                    spotColor =
                                        Color(
                                            0x40000000
                                        )
                                ),

                        shape =
                            RoundedCornerShape(
                                24.dp
                            ),

                        colors =
                            CardDefaults.cardColors(

                                containerColor =
                                    Color.White
                            )
                    ) {

                        Column(

                            modifier =
                                Modifier.padding(
                                    20.dp
                                )
                        ) {

                            Row(

                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                AsyncImage(

                                    model =
                                        place.imageUrl,

                                    contentDescription =
                                        "장소 이미지",

                                    contentScale =
                                        ContentScale.Crop,

                                    modifier =
                                        Modifier
                                            .size(
                                                88.dp
                                            )
                                            .clip(
                                                RoundedCornerShape(
                                                    16.dp
                                                )
                                            )
                                )


                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            16.dp
                                        )
                                )


                                Column(

                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                ) {

                                    Text(

                                        place.name,

                                        fontSize =
                                            20.sp,

                                        fontWeight =
                                            FontWeight.ExtraBold
                                    )


                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                4.dp
                                            )
                                    )


                                    Text(

                                        place.address,

                                        fontSize =
                                            13.sp,

                                        color =
                                            Color(0xFF757575),

                                        maxLines =
                                            1
                                    )


                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                10.dp
                                            )
                                    )


                                    Surface(

                                        color =
                                            Color(0xFFE8F5E9),

                                        shape =
                                            RoundedCornerShape(
                                                8.dp
                                            )
                                    ) {

                                        Text(

                                            place.tag,

                                            color =
                                                Color(
                                                    0xFF2E7D32
                                                ),

                                            fontSize =
                                                12.sp,

                                            fontWeight =
                                                FontWeight.Bold,

                                            modifier =
                                                Modifier.padding(

                                                    horizontal =
                                                        8.dp,

                                                    vertical =
                                                        4.dp
                                                )
                                        )
                                    }
                                }
                            }


                            Spacer(
                                modifier =
                                    Modifier.height(
                                        16.dp
                                    )
                            )


                            // ====================================================
                            // 길찾기 / 일정 추가
                            // ====================================================

                            Row(

                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        12.dp
                                    )
                            ) {

                                OutlinedButton(

                                    onClick = {

                                        val encodedName =
                                            Uri.encode(
                                                place.name
                                            )

                                        val url =
                                            "https://m.map.naver.com/search.naver?query=$encodedName"

                                        val intent =
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(
                                                    url
                                                )
                                            )

                                        context.startActivity(
                                            intent
                                        )
                                    },

                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(
                                                48.dp
                                            ),

                                    shape =
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                ) {

                                    Text(
                                        "길찾기 / 로드뷰",

                                        fontSize =
                                            14.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }


                                Button(

                                    onClick = {

                                        viewModel
                                            .addPlaceToRoute(
                                                place
                                            )
                                    },

                                    colors =
                                        ButtonDefaults
                                            .buttonColors(

                                                containerColor =
                                                    Color(
                                                        0xFF2E7D32
                                                    )
                                            ),

                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(
                                                48.dp
                                            ),

                                    shape =
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                ) {

                                    Icon(

                                        Icons.Default.Add,

                                        contentDescription =
                                            "일정에 추가",

                                        modifier =
                                            Modifier.size(
                                                18.dp
                                            )
                                    )


                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                6.dp
                                            )
                                    )


                                    Text(

                                        "일정 추가",

                                        fontSize =
                                            14.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }
                            }


                            Spacer(
                                modifier =
                                    Modifier.height(
                                        12.dp
                                    )
                            )


                            // ====================================================
                            // TourAPI 식당 검색
                            // ====================================================

                            Button(

                                onClick = {

                                    viewModel
                                        .searchNearbyRestaurants(

                                            lat =
                                                place.latitude,

                                            lng =
                                                place.longitude
                                        )
                                },

                                colors =
                                    ButtonDefaults
                                        .buttonColors(

                                            containerColor =
                                                Color(
                                                    0xFF1976D2
                                                )
                                        ),

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(
                                            48.dp
                                        ),

                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
                            ) {

                                if (
                                    viewModel
                                        .isFetchingRestaurants
                                        .value
                                ) {

                                    CircularProgressIndicator(

                                        color =
                                            Color.White,

                                        modifier =
                                            Modifier.size(
                                                20.dp
                                            ),

                                        strokeWidth =
                                            2.dp
                                    )


                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                8.dp
                                            )
                                    )


                                    Text(

                                        "공공데이터 불러오는 중...",

                                        fontSize =
                                            14.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                } else {

                                    Icon(

                                        Icons.Rounded.Search,

                                        contentDescription =
                                            "식당 찾기",

                                        modifier =
                                            Modifier.size(
                                                18.dp
                                            )
                                    )


                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                6.dp
                                            )
                                    )


                                    Text(

                                        "주변 5km 안심 식당 찾기 (TourAPI)",

                                        fontSize =
                                            14.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}