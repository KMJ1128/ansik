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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kmj.ansik.R
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

private val MapScreenDayColorPalette = listOf(
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

private fun getMapDayColor(day: Int): Color {

    if (day < 1) return Color.Gray

    val index =
        (day - 1) % MapScreenDayColorPalette.size

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

    return remember(number, composeColor) {

        val size = 90

        val bitmap = Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            color = composeColor.toArgb()
        }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            size / 2f,
            paint
        )

        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            (size / 2f) - 3f,
            borderPaint
        )

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 42f
            typeface = Typeface.create(
                Typeface.DEFAULT,
                Typeface.BOLD
            )
            textAlign = Paint.Align.CENTER
        }

        val fontMetrics = textPaint.fontMetrics

        val textY =
            (size / 2f) -
                    (fontMetrics.ascent + fontMetrics.descent) / 2f

        canvas.drawText(
            number.toString(),
            size / 2f,
            textY,
            textPaint
        )

        OverlayImage.fromBitmap(bitmap)
    }
}


// ============================================================
// 식당 Marker
// ============================================================

@Composable
private fun rememberRestaurantMarker(): OverlayImage {

    return remember {

        val size = 80

        val bitmap = Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val outerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(
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

        val innerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
        }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            27f,
            innerPaint
        )

        val centerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(
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

        OverlayImage.fromBitmap(bitmap)
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

    val searchQuery by viewModel.searchQuery
    val isSearchActive by viewModel.isSearchActive
    val selectedPlace by viewModel.selectedPlace

    val travelRoute = viewModel.travelRoute
    val nearbyRestaurants = viewModel.nearbyRestaurants

    val selectedRestaurantDetail by
    viewModel.selectedRestaurantDetail

    val isFetchingRestaurants by
    viewModel.isFetchingRestaurants

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

    val restaurantMarker =
        rememberRestaurantMarker()

    var highlightedPlaceId by
    remember {
        mutableStateOf<String?>(null)
    }

    // ========================================================
    // 일정 사이드 패널
    // ========================================================

    var schedulePanelOffset by
    remember {
        mutableFloatStateOf(0f)
    }

    var isScheduleExpanded by
    remember {
        mutableStateOf(false)
    }

    // ========================================================
    // 식당 BottomSheet 표시
    // ========================================================

    var showRestaurantSheet by
    remember {
        mutableStateOf(false)
    }

    val restaurantSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )

    // ========================================================
    // 선택 장소가 변경되면 지도 이동
    // ========================================================

    LaunchedEffect(selectedPlace) {

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


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // ====================================================
        // 네이버 지도
        // ====================================================

        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,

            onSymbolClick = { symbol ->

                viewModel.selectLocationFromMap(
                    symbol.caption,
                    symbol.position.latitude,
                    symbol.position.longitude
                )

                keyboardController?.hide()

                true
            },

            onMapClick = { _, _ ->

                viewModel.selectedPlace.value = null

                keyboardController?.hide()
            }
        ) {

            // ------------------------------------------------
            // Day별 경로
            // ------------------------------------------------

            for (
            d in 1..viewModel.days.value
            ) {

                val coords =
                    viewModel.getRouteCoordsForDay(d)

                if (coords.size >= 2) {

                    PathOverlay(
                        coords = coords,
                        width = 5.dp,
                        color = getMapDayColor(d),
                        outlineWidth = 1.dp,
                        outlineColor = Color.White
                    )
                }
            }

            // ------------------------------------------------
            // 선택된 장소
            // ------------------------------------------------

            selectedPlace?.let { place ->

                key(
                    "selected_${place.id}"
                ) {

                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                place.latitude,
                                place.longitude
                            )
                        ),
                        icon = MarkerIcons.BLACK,
                        iconTintColor =
                            getMapDayColor(place.day)
                    )
                }
            }

            // ------------------------------------------------
            // 여행 일정 Marker
            // ------------------------------------------------

            travelRoute.forEach { place ->

                val dayColor =
                    getMapDayColor(place.day)

                val dayIndex =
                    travelRoute
                        .filter {
                            it.day == place.day
                        }
                        .indexOf(place) + 1

                val customMarkerIcon =
                    rememberNumberedMarker(
                        number = dayIndex,
                        composeColor = dayColor
                    )

                key(
                    "route_${place.id}"
                ) {

                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                place.latitude,
                                place.longitude
                            )
                        ),
                        icon = customMarkerIcon,

                        onClick = {

                            coroutineScope.launch {

                                val freshIndex =
                                    viewModel.travelRoute
                                        .indexOfFirst {
                                            it.id == place.id
                                        }

                                if (freshIndex != -1) {

                                    if (
                                        freshIndex <
                                        lazyListState.layoutInfo.totalItemsCount
                                    ) {

                                        lazyListState.animateScrollToItem(
                                            freshIndex
                                        )
                                    }
                                }

                                highlightedPlaceId =
                                    place.id

                                delay(800)

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

            // ------------------------------------------------
            // 주변 식당 Marker
            // ------------------------------------------------

            nearbyRestaurants.forEach { restaurant ->

                val lng =
                    restaurant.mapx.toDoubleOrNull()

                val lat =
                    restaurant.mapy.toDoubleOrNull()

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
                            state = MarkerState(
                                position = LatLng(
                                    lat,
                                    lng
                                )
                            ),

                            icon = restaurantMarker,

                            captionText =
                                restaurant.title,

                            onClick = {

                                viewModel.fetchRestaurantDetail(
                                    restaurant.contentid
                                )

                                true
                            }
                        )
                    }
                }
            }
        }


        // ====================================================
        // 검색창
        // ====================================================

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = 20.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .fillMaxWidth()
        ) {

            OutlinedTextField(

                value = searchQuery,

                onValueChange = {
                    viewModel.searchPlacesRealtime(it)
                },

                placeholder = {

                    Text(
                        text = stringResource(
                            id = R.string.search_placeholder
                        ),
                        color = Color(0xFF9E9E9E)
                    )
                },

                leadingIcon = {

                    Icon(
                        imageVector =
                            Icons.Rounded.Search,
                        contentDescription =
                            stringResource(
                                id = R.string.search
                            ),
                        tint = Color(0xFF2E7D32)
                    )
                },

                trailingIcon = {

                    if (searchQuery.isNotEmpty()) {

                        IconButton(
                            onClick = {

                                viewModel.searchQuery.value =
                                    ""

                                viewModel.isSearchActive.value =
                                    false
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Close,
                                contentDescription =
                                    stringResource(
                                        id = R.string.clear
                                    ),
                                tint = Color.Gray
                            )
                        }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        12.dp,
                        RoundedCornerShape(50),
                        spotColor =
                            Color(0x26000000)
                    ),

                shape = RoundedCornerShape(50),

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

                singleLine = true
            )


            // =================================================
            // 검색 결과
            // =================================================

            AnimatedVisibility(
                visible = isSearchActive
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .shadow(
                            16.dp,
                            RoundedCornerShape(20.dp)
                        ),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        )
                ) {

                    Column {

                        viewModel.recommendedPlaces
                            .forEach { place ->

                                Row(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {

                                            viewModel
                                                .selectedPlace
                                                .value =
                                                place

                                            viewModel
                                                .isSearchActive
                                                .value =
                                                false

                                            keyboardController
                                                ?.hide()
                                        }
                                        .padding(
                                            horizontal = 20.dp,
                                            vertical = 16.dp
                                        ),

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                Color(0xFFF5F5F5),
                                                CircleShape
                                            ),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Rounded.Search,
                                            contentDescription =
                                                null,
                                            tint =
                                                Color(0xFFBDBDBD),
                                            modifier =
                                                Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.width(16.dp)
                                    )

                                    Column(
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {

                                        Row(
                                            verticalAlignment =
                                                Alignment.CenterVertically
                                        ) {

                                            Text(
                                                text =
                                                    place.name,
                                                fontSize =
                                                    16.sp,
                                                fontWeight =
                                                    FontWeight.Bold
                                            )

                                            Spacer(
                                                modifier =
                                                    Modifier.width(8.dp)
                                            )

                                            Text(
                                                text =
                                                    place.tag,
                                                fontSize =
                                                    12.sp,
                                                color =
                                                    Color(0xFF2E7D32)
                                            )
                                        }

                                        Spacer(
                                            modifier =
                                                Modifier.height(2.dp)
                                        )

                                        Text(
                                            text =
                                                place.address,
                                            fontSize =
                                                13.sp,
                                            color =
                                                Color(0xFF9E9E9E)
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    color =
                                        Color(0xFFF5F5F5)
                                )
                            }
                    }
                }
            }
        }


        // ====================================================
        // 왼쪽 일정 사이드 패널
        // ====================================================

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 30.dp)
                .fillMaxHeight()
                .width(
                    if (isScheduleExpanded)
                        330.dp
                    else
                        82.dp
                )
                .pointerInput(isScheduleExpanded) {

                    detectHorizontalDragGestures(

                        onHorizontalDrag = { change, dragAmount ->

                            change.consume()

                            schedulePanelOffset +=
                                dragAmount

                            if (
                                schedulePanelOffset > 80f
                            ) {

                                isScheduleExpanded =
                                    true

                                schedulePanelOffset =
                                    0f
                            }

                            if (
                                schedulePanelOffset < -80f
                            ) {

                                isScheduleExpanded =
                                    false

                                schedulePanelOffset =
                                    0f
                            }
                        },

                        onDragEnd = {

                            schedulePanelOffset =
                                0f
                        }
                    )
                }
        ) {

            Card(
                modifier = Modifier
                    .fillMaxHeight(
                        fraction =
                            if (isScheduleExpanded)
                                0.82f
                            else
                                0.32f
                    )
                    .align(Alignment.CenterStart)
                    .shadow(
                        12.dp,
                        RoundedCornerShape(
                            topEnd = 24.dp,
                            bottomEnd = 24.dp
                        )
                    ),

                shape =
                    RoundedCornerShape(
                        topEnd = 24.dp,
                        bottomEnd = 24.dp
                    ),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White.copy(
                                alpha = 0.97f
                            )
                    )
            ) {

                if (!isScheduleExpanded) {

                    // =========================================
                    // 접힌 상태
                    // =========================================

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {

                                isScheduleExpanded =
                                    true
                            }
                            .padding(
                                horizontal = 12.dp,
                                vertical = 20.dp
                            ),

                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        verticalArrangement =
                            Arrangement.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Rounded.Menu,
                            contentDescription =
                                null,
                            tint =
                                Color(0xFF2E7D32),
                            modifier =
                                Modifier.size(28.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Text(
                            text = "내\n일정",
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                Color(0xFF333333)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Surface(
                            color =
                                Color(0xFFE8F5E9),
                            shape =
                                CircleShape
                        ) {

                            Text(
                                text =
                                    "${travelRoute.size}",
                                fontSize = 12.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color =
                                    Color(0xFF2E7D32),
                                modifier =
                                    Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 5.dp
                                    )
                            )
                        }
                    }

                } else {

                    // =========================================
                    // 펼친 상태
                    // =========================================

                    Column(
                        modifier =
                            Modifier.fillMaxSize()
                    ) {

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 18.dp,
                                        vertical = 14.dp
                                    ),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column {

                                Text(
                                    text =
                                        stringResource(
                                            id =
                                                R.string.my_travel_schedule
                                        ),
                                    fontSize =
                                        20.sp,
                                    fontWeight =
                                        FontWeight.ExtraBold
                                )

                                Text(
                                    text =
                                        stringResource(
                                            id =
                                                R.string.nights_days,
                                            viewModel.nights.value,
                                            viewModel.days.value
                                        ),
                                    fontSize =
                                        12.sp,
                                    color =
                                        Color(0xFF757575)
                                )
                            }

                            IconButton(
                                onClick = {
                                    isScheduleExpanded =
                                        false
                                }
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Close,
                                    contentDescription =
                                        "닫기"
                                )
                            }
                        }

                        HorizontalDivider(
                            color =
                                Color(0xFFEEEEEE)
                        )

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

                            contentPadding =
                                PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 12.dp
                                ),

                            modifier =
                                Modifier.weight(1f),

                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
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
                                    key = place.id
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
                                                    )
                                                        10.dp
                                                    else
                                                        2.dp,
                                                    RoundedCornerShape(
                                                        14.dp
                                                    )
                                                ),

                                        shape =
                                            RoundedCornerShape(
                                                14.dp
                                            ),

                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor =
                                                    if (
                                                        isHighlighted
                                                    )
                                                        Color(
                                                            0xFFFFF9C4
                                                        )
                                                    else
                                                        Color.White
                                            )
                                    ) {

                                        Column(
                                            modifier =
                                                Modifier.padding(
                                                    12.dp
                                                )
                                        ) {

                                            Row(
                                                verticalAlignment =
                                                    Alignment.CenterVertically
                                            ) {

                                                Icon(
                                                    imageVector =
                                                        Icons.Rounded.Menu,
                                                    contentDescription =
                                                        stringResource(
                                                            id =
                                                                R.string.change_order
                                                        ),
                                                    tint =
                                                        Color(
                                                            0xFFD0D0D0
                                                        ),
                                                    modifier =
                                                        Modifier
                                                            .draggableHandle()
                                                            .size(24.dp)
                                                )

                                                Spacer(
                                                    modifier =
                                                        Modifier.width(
                                                            8.dp
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
                                                            .size(48.dp)
                                                            .clip(
                                                                RoundedCornerShape(
                                                                    10.dp
                                                                )
                                                            )
                                                )

                                                Spacer(
                                                    modifier =
                                                        Modifier.width(
                                                            10.dp
                                                        )
                                                )

                                                Column(
                                                    modifier =
                                                        Modifier.weight(
                                                            1f
                                                        )
                                                ) {

                                                    Text(
                                                        text =
                                                            place.name,
                                                        fontSize =
                                                            14.sp,
                                                        fontWeight =
                                                            FontWeight.Bold,
                                                        maxLines =
                                                            1
                                                    )

                                                    Text(
                                                        text =
                                                            place.tag,
                                                        fontSize =
                                                            11.sp,
                                                        color =
                                                            Color(
                                                                0xFF757575
                                                            )
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        viewModel
                                                            .removePlace(
                                                                place
                                                            )
                                                    },
                                                    modifier =
                                                        Modifier.size(
                                                            32.dp
                                                        )
                                                ) {

                                                    Icon(
                                                        imageVector =
                                                            Icons.Default.Delete,
                                                        contentDescription =
                                                            stringResource(
                                                                id =
                                                                    R.string.delete
                                                            ),
                                                        tint =
                                                            Color(
                                                                0xFFFF8A80
                                                            )
                                                    )
                                                }
                                            }

                                            Spacer(
                                                modifier =
                                                    Modifier.height(
                                                        8.dp
                                                    )
                                            )

                                            LazyRow(
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(
                                                        6.dp
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

                                                    Surface(
                                                        modifier =
                                                            Modifier.clickable {

                                                                viewModel
                                                                    .changePlaceDay(
                                                                        place,
                                                                        d
                                                                    )
                                                            },

                                                        shape =
                                                            RoundedCornerShape(
                                                                50
                                                            ),

                                                        color =
                                                            if (
                                                                isSelected
                                                            )
                                                                getMapDayColor(
                                                                    d
                                                                )
                                                            else
                                                                Color(
                                                                    0xFFF5F5F5
                                                                )
                                                    ) {

                                                        Text(
                                                            text =
                                                                stringResource(
                                                                    id =
                                                                        R.string.day_format,
                                                                    d
                                                                ),
                                                            fontSize =
                                                                10.sp,
                                                            fontWeight =
                                                                if (
                                                                    isSelected
                                                                )
                                                                    FontWeight.Bold
                                                                else
                                                                    FontWeight.Medium,

                                                            color =
                                                                if (
                                                                    isSelected
                                                                )
                                                                    Color.White
                                                                else
                                                                    Color(
                                                                        0xFF9E9E9E
                                                                    ),

                                                            modifier =
                                                                Modifier.padding(
                                                                    horizontal = 10.dp,
                                                                    vertical = 5.dp
                                                                )
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
                }
            }
        }


        // ====================================================
        // 장소 상세 카드
        // ====================================================

        AnimatedVisibility(

            visible =
                selectedPlace != null,

            enter =
                slideInVertically(
                    initialOffsetY = {
                        it
                    }
                ) + fadeIn(),

            exit =
                slideOutVertically(
                    targetOffsetY = {
                        it
                    }
                ) + fadeOut(),

            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(
                        bottom = 20.dp,
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
                                RoundedCornerShape(24.dp)
                            ),

                    shape =
                        RoundedCornerShape(24.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(18.dp)
                    ) {

                        Row {

                            AsyncImage(
                                model =
                                    place.imageUrl,

                                contentDescription =
                                    stringResource(
                                        id =
                                            R.string.place_image
                                    ),

                                contentScale =
                                    ContentScale.Crop,

                                modifier =
                                    Modifier
                                        .size(82.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                14.dp
                                            )
                                        )
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(14.dp)
                            )

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text =
                                        place.name,
                                    fontSize =
                                        19.sp,
                                    fontWeight =
                                        FontWeight.ExtraBold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    text =
                                        place.address,
                                    fontSize =
                                        12.sp,
                                    color =
                                        Color(0xFF757575),
                                    maxLines =
                                        2
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
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
                                        text =
                                            place.tag,
                                        color =
                                            Color(0xFF2E7D32),
                                        fontSize =
                                            11.sp,
                                        fontWeight =
                                            FontWeight.Bold,
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
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
                                            Uri.parse(url)
                                        )

                                    context.startActivity(
                                        intent
                                    )
                                },

                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(46.dp),

                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
                            ) {

                                Text(
                                    text =
                                        stringResource(
                                            id =
                                                R.string.directions_roadview
                                        ),
                                    fontSize =
                                        12.sp,
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
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            Color(0xFF2E7D32)
                                    ),

                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(46.dp),

                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Add,
                                    contentDescription =
                                        null,
                                    modifier =
                                        Modifier.size(
                                            17.dp
                                        )
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(5.dp)
                                )

                                Text(
                                    text =
                                        stringResource(
                                            id =
                                                R.string.add_schedule
                                        ),
                                    fontSize =
                                        12.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {

                                viewModel
                                    .searchNearbyRestaurants(
                                        lat =
                                            place.latitude,
                                        lng =
                                            place.longitude
                                    )

                                showRestaurantSheet =
                                    true
                            },

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFF1976D2)
                                ),

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),

                            shape =
                                RoundedCornerShape(
                                    12.dp
                                )
                        ) {

                            if (
                                isFetchingRestaurants
                            ) {

                                CircularProgressIndicator(
                                    color =
                                        Color.White,
                                    modifier =
                                        Modifier.size(
                                            18.dp
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
                                    text =
                                        stringResource(
                                            id =
                                                R.string.loading_public_data
                                        )
                                )

                            } else {

                                Icon(
                                    imageVector =
                                        Icons.Rounded.Search,
                                    contentDescription =
                                        null,
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
                                    text =
                                        stringResource(
                                            id =
                                                R.string.find_nearby_safe_restaurants
                                        ),
                                    fontSize =
                                        12.sp,
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


    // ========================================================
    // 식당 리스트 BottomSheet
    // ========================================================

    if (showRestaurantSheet) {

        ModalBottomSheet(

            onDismissRequest = {
                showRestaurantSheet =
                    false
            },

            sheetState =
                restaurantSheetState,

            containerColor =
                Color.White,

            dragHandle = {

                Box(
                    modifier =
                        Modifier
                            .padding(
                                top = 8.dp,
                                bottom = 8.dp
                            )
                            .width(42.dp)
                            .height(5.dp)
                            .background(
                                Color(0xFFE0E0E0),
                                CircleShape
                            )
                )
            }
        ) {

            RestaurantListContent(
                restaurants =
                    nearbyRestaurants,

                selectedDetail =
                    selectedRestaurantDetail,

                onRestaurantClick = {
                    viewModel.fetchRestaurantDetail(
                        it.contentid
                    )
                }
            )
        }
    }
}


// ============================================================
// 식당 리스트
// ============================================================

@Composable
private fun RestaurantListContent(

    restaurants:
    List<TourRestaurant>,

    selectedDetail:
    TourRestaurantDetail?,

    onRestaurantClick:
        (TourRestaurant) -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 300.dp,
                    max = 700.dp
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    horizontal = 20.dp
                )
        ) {

            Text(
                text = "식당 리스트",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    "현재 위치 주변 5km 안심 식당",
                fontSize = 13.sp,
                color =
                    Color(0xFF757575)
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        if (restaurants.isEmpty()) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(40.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        "주변에 검색된 식당이 없습니다.",
                    fontSize =
                        15.sp,
                    color =
                        Color(0xFF757575)
                )
            }

        } else {

            LazyColumn(

                modifier =
                    Modifier.weight(1f),

                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 40.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    restaurants,
                    key = {
                        it.contentid
                    }
                ) { restaurant ->

                    RestaurantCard(
                        restaurant =
                            restaurant,

                        detail =
                            if (
                                selectedDetail
                                    ?.contentid ==
                                restaurant.contentid
                            )
                                selectedDetail
                            else
                                null,

                        onClick = {
                            onRestaurantClick(
                                restaurant
                            )
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// 식당 카드
// ============================================================

@Composable
private fun RestaurantCard(

    restaurant:
    TourRestaurant,

    detail:
    TourRestaurantDetail?,

    onClick:
        () -> Unit
) {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    3.dp
            )
    ) {

        Column {

            // ------------------------------------------------
            // 음식점 이미지
            // ------------------------------------------------

            AsyncImage(

                model =
                    restaurant.firstimage
                        .ifEmpty {
                            restaurant.firstimage2
                        },

                contentDescription =
                    restaurant.title,

                contentScale =
                    ContentScale.Crop,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp
                            )
                        )
            )

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    )
            ) {

                // ------------------------------------------------
                // 이름
                // ------------------------------------------------

                Text(
                    text =
                        restaurant.title,
                    fontSize =
                        18.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                // ------------------------------------------------
                // 주소
                // ------------------------------------------------

                Text(
                    text =
                        listOf(
                            restaurant.addr1,
                            restaurant.addr2
                        )
                            .filter {
                                it.isNotBlank()
                            }
                            .joinToString(" "),

                    fontSize =
                        13.sp,

                    color =
                        Color(0xFF757575),

                    maxLines =
                        2
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                // ------------------------------------------------
                // 상세정보
                // ------------------------------------------------

                if (detail != null) {

                    if (
                        !detail.firstmenu.isNullOrBlank()
                    ) {

                        RestaurantInfoRow(
                            title =
                                "대표 메뉴",
                            value =
                                detail.firstmenu!!
                        )
                    }

                    if (
                        !detail.treatmenu.isNullOrBlank()
                    ) {

                        RestaurantInfoRow(
                            title =
                                "취급 메뉴",
                            value =
                                detail.treatmenu!!
                        )
                    }

                    if (
                        !detail.opentimefood.isNullOrBlank()
                    ) {

                        RestaurantInfoRow(
                            title =
                                "영업시간",
                            value =
                                detail.opentimefood!!
                        )
                    }

                    if (
                        !detail.parkingfood.isNullOrBlank()
                    ) {

                        RestaurantInfoRow(
                            title =
                                "주차",
                            value =
                                detail.parkingfood!!
                        )
                    }

                    if (
                        !detail.packing.isNullOrBlank()
                    ) {

                        RestaurantInfoRow(
                            title =
                                "포장",
                            value =
                                detail.packing!!
                        )
                    }

                } else {

                    Surface(
                        color =
                            Color(0xFFF5F5F5),
                        shape =
                            RoundedCornerShape(
                                8.dp
                            )
                    ) {

                        Text(
                            text =
                                "탭하여 상세정보 확인",
                            fontSize =
                                12.sp,
                            color =
                                Color(0xFF757575),
                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 7.dp
                                )
                        )
                    }
                }
            }
        }
    }
}


// ============================================================
// 식당 정보 Row
// ============================================================

@Composable
private fun RestaurantInfoRow(

    title:
    String,

    value:
    String
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                )
    ) {

        Text(
            text =
                title,
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.Bold,
            color =
                Color(0xFF2E7D32)
        )

        Text(
            text =
                value,
            fontSize =
                13.sp,
            color =
                Color(0xFF424242),
            maxLines =
                3
        )
    }
}