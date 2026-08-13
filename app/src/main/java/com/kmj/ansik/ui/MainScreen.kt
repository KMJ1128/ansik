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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.kmj.ansik.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
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
// 전체화면 이미지 스와이퍼 (다이얼로그)
// ============================================================
@Composable
fun FullScreenImageViewer(imageUrls: List<String>, onDismiss: () -> Unit) {
    if (imageUrls.isEmpty()) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            val pagerState = rememberPagerState(pageCount = { imageUrls.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = imageUrls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
            }

            Text(
                text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

// ============================================================
// Day 색상 및 마커 함수들
// ============================================================
private val MapScreenDayColorPalette = listOf(
    Color(0xFFE53935), Color(0xFFFF7043), Color(0xFFFFCA28), Color(0xFF43A047),
    Color(0xFF1E88E5), Color(0xFF3949AB), Color(0xFF8E24AA), Color(0xFFD81B60),
    Color(0xFF6D4C41), Color(0xFF546E7A)
)

private fun getMapDayColor(day: Int): Color {
    if (day < 1) return Color.Gray
    return MapScreenDayColorPalette[(day - 1) % MapScreenDayColorPalette.size]
}

@Composable
private fun rememberNumberedMarker(number: Int, composeColor: Color): OverlayImage {
    return remember(number, composeColor) {
        val size = 90
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            color = composeColor.toArgb()
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 3f, borderPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val fontMetrics = textPaint.fontMetrics
        val textY = (size / 2f) - (fontMetrics.ascent + fontMetrics.descent) / 2f

        canvas.drawText(number.toString(), size / 2f, textY, textPaint)
        OverlayImage.fromBitmap(bitmap)
    }
}

@Composable
private fun rememberTouristMarker(): OverlayImage {
    return remember {
        val size = 50
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val outerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(46, 125, 50)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, outerPaint)

        val innerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 5f, innerPaint)

        val centerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(46, 125, 50)
        }
        canvas.drawCircle(size / 2f, size / 2f, 10f, centerPaint)

        OverlayImage.fromBitmap(bitmap)
    }
}

@Composable
private fun rememberRestaurantMarker(): OverlayImage {
    return remember {
        val size = 50
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val outerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(25, 118, 210)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, outerPaint)

        val innerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 5f, innerPaint)

        val centerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(25, 118, 210)
        }
        canvas.drawCircle(size / 2f, size / 2f, 10f, centerPaint)

        OverlayImage.fromBitmap(bitmap)
    }
}

// ============================================================
// 🔥 MainScreen (지도 + 핀 + 사이드바 + 가로 스와이프 UI)
// ============================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val searchQuery by viewModel.searchQuery
    val isSearchActive by viewModel.isSearchActive
    val selectedPlace by viewModel.selectedPlace

    val travelRoute = viewModel.travelRoute
    val nearbyRestaurants = viewModel.nearbyRestaurants
    val selectedRestaurantDetail by viewModel.selectedRestaurantDetail
    val isFetchingRestaurants by viewModel.isFetchingRestaurants

    val cameraPositionState = rememberCameraPositionState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val restaurantMarker = rememberRestaurantMarker()
    val touristMarker = rememberTouristMarker()

    var highlightedPlaceId by remember { mutableStateOf<String?>(null) }
    var isScheduleExpanded by remember { mutableStateOf(false) }
    var showRadiusDialog by remember { mutableStateOf(false) }
    var showHotPlaceFilterDialog by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }

    var viewerImages by remember { mutableStateOf<List<String>?>(null) }

    viewerImages?.let { urls ->
        FullScreenImageViewer(
            imageUrls = urls,
            onDismiss = { viewerImages = null }
        )
    }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            val targetLocation = LatLng(place.latitude, place.longitude)
            cameraPositionState.animate(CameraUpdate.scrollTo(targetLocation))
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val zoom = cameraPositionState.position.zoom
            if (zoom >= 11.0) {
                val target = cameraPositionState.position.target
                viewModel.fetchPopularDataDynamic(target.latitude, target.longitude)
            }
        }
    }

    // (다이얼로그 부분 생략 없이 그대로 유지)
    if (showHotPlaceFilterDialog) {
        AlertDialog(
            onDismissRequest = { showHotPlaceFilterDialog = false },
            title = {
                Text("인기 핫플 표시 설정", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("인기 관광지 표시", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Switch(
                            checked = viewModel.showPopularPlaces.value,
                            onCheckedChange = { viewModel.showPopularPlaces.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2E7D32))
                        )
                    }
                    if (viewModel.showPopularPlaces.value) {
                        Text("최대 개수: ${viewModel.maxPopularPlaces.floatValue.toInt()}개", fontSize = 13.sp, color = Color.Gray)
                        Slider(
                            value = viewModel.maxPopularPlaces.floatValue,
                            onValueChange = { viewModel.maxPopularPlaces.floatValue = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF2E7D32), activeTrackColor = Color(0xFF2E7D32))
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("인기 맛집 표시", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Switch(
                            checked = viewModel.showPopularRestaurants.value,
                            onCheckedChange = { viewModel.showPopularRestaurants.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF1976D2))
                        )
                    }
                    if (viewModel.showPopularRestaurants.value) {
                        Text("최대 개수: ${viewModel.maxPopularRestaurants.floatValue.toInt()}개", fontSize = 13.sp, color = Color.Gray)
                        Slider(
                            value = viewModel.maxPopularRestaurants.floatValue,
                            onValueChange = { viewModel.maxPopularRestaurants.floatValue = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2), activeTrackColor = Color(0xFF1976D2))
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHotPlaceFilterDialog = false }) {
                    Text("확인", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    if (showRadiusDialog) {
        AlertDialog(
            onDismissRequest = { showRadiusDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.radius_settings_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    var tempRadius by remember { mutableFloatStateOf(viewModel.searchRadius.intValue.toFloat()) }
                    val snappedRadius = (tempRadius / 100).toInt() * 100

                    val radiusText = if (snappedRadius >= 1000) {
                        if (snappedRadius % 1000 == 0) "${snappedRadius / 1000}km"
                        else String.format("%.1fkm", snappedRadius / 1000f)
                    } else {
                        "${snappedRadius}m"
                    }

                    Text(
                        text = radiusText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = tempRadius,
                        onValueChange = { tempRadius = it },
                        onValueChangeFinished = {
                            viewModel.updateSearchRadius(snappedRadius)
                        },
                        valueRange = 100f..3000f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF2E7D32),
                            activeTrackColor = Color(0xFF2E7D32),
                            inactiveTrackColor = Color(0xFFE8F5E9)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRadiusDialog = false }) {
                    Text("확인", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ============================================================
        // 🗺️ 네이버 지도 (핀 그리기 및 경로 연결 유지)
        // ============================================================
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onSymbolClick = { symbol ->
                viewModel.selectLocationFromMap(symbol.caption, symbol.position.latitude, symbol.position.longitude)
                focusManager.clearFocus()
                keyboardController?.hide()
                true
            },
            onMapClick = { _, _ ->
                viewModel.selectedPlace.value = null
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ) {
            // 💡 경로 그리기(선)
            for (d in 1..viewModel.days.value) {
                val coords = viewModel.getRouteCoordsForDay(d)
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

            // 💡 선택한 장소 까만 핀
            selectedPlace?.let { place ->
                key("selected_${place.id}") {
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        icon = MarkerIcons.BLACK,
                        iconTintColor = getMapDayColor(place.day)
                    )
                }
            }

            // 💡 일정에 담은 장소 숫자 핀
            val currentRouteList: List<PlaceInfo> = viewModel.travelRoute.toList()
            currentRouteList.forEach { place ->
                val dayColor = getMapDayColor(place.day)
                val dayIndex = currentRouteList.filter { it.day == place.day }.indexOf(place) + 1
                val customMarkerIcon = rememberNumberedMarker(number = dayIndex, composeColor = dayColor)

                key("route_${place.id}") {
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        icon = customMarkerIcon,
                        onClick = {
                            coroutineScope.launch {
                                val freshIndex = viewModel.travelRoute.indexOfFirst { it.id == place.id }
                                if (freshIndex != -1 && freshIndex < lazyListState.layoutInfo.totalItemsCount) {
                                    lazyListState.animateScrollToItem(freshIndex)
                                }
                                highlightedPlaceId = place.id
                                delay(800)
                                if (highlightedPlaceId == place.id) highlightedPlaceId = null
                            }
                            true
                        }
                    )
                }
            }

            // 💡 인기 관광지 핀
            if (viewModel.showPopularPlaces.value) {
                val placesList: List<TourRestaurant> = viewModel.popularPlaces.toList().take(viewModel.maxPopularPlaces.floatValue.toInt())
                placesList.forEach { place ->
                    val lng = place.mapx.toDoubleOrNull()
                    val lat = place.mapy.toDoubleOrNull()
                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                        key("popular_place_${place.contentid}") {
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                icon = touristMarker,
                                captionText = place.title,
                                onClick = {
                                    viewModel.selectLocationFromMap(place.title, lat, lng)
                                    true
                                }
                            )
                        }
                    }
                }
            }

            // 💡 인기 식당 핀
            if (viewModel.showPopularRestaurants.value) {
                val restsList: List<TourRestaurant> = viewModel.popularRestaurants.toList().take(viewModel.maxPopularRestaurants.floatValue.toInt())
                restsList.forEachIndexed { index, restaurant ->
                    val rank = index + 1
                    val lng = restaurant.mapx.toDoubleOrNull()
                    val lat = restaurant.mapy.toDoubleOrNull()
                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                        key("popular_restaurant_${restaurant.contentid}") {
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                icon = restaurantMarker,
                                captionText = "${rank}위 ${restaurant.title}",
                                onClick = {
                                    viewModel.nearbyRestaurants.clear()
                                    viewModel.nearbyRestaurants.add(restaurant)
                                    viewModel.fetchRestaurantDetail(restaurant.contentid)
                                    true
                                }
                            )
                        }
                    }
                }
            }

            // 💡 주변 식당 핀
            val nearbyList: List<TourRestaurant> = nearbyRestaurants.toList()
            nearbyList.forEach { restaurant ->
                val lng = restaurant.mapx.toDoubleOrNull()
                val lat = restaurant.mapy.toDoubleOrNull()
                if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                    key("restaurant_${restaurant.contentid}") {
                        Marker(
                            state = MarkerState(position = LatLng(lat, lng)),
                            icon = restaurantMarker,
                            captionText = restaurant.title,
                            onClick = {
                                viewModel.fetchRestaurantDetail(restaurant.contentid)
                                true
                            }
                        )
                    }
                }
            }
        }

        // ============================================================
        // 🍔 상단/하단 UI 레이아웃
        // ============================================================
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            // 검색바
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchPlacesRealtime(it) },
                placeholder = {
                    Text(text = stringResource(id = R.string.search_placeholder), color = Color(0xFF9E9E9E))
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = stringResource(id = R.string.search), tint = Color(0xFF2E7D32))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.searchQuery.value = ""
                            viewModel.isSearchActive.value = false
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.clear), tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isSearchFocused = focusState.isFocused
                    }
                    .shadow(12.dp, RoundedCornerShape(50), spotColor = Color(0x26000000)),
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.98f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable { showHotPlaceFilterDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("핫플 표시 설정", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }

            // 검색 자동완성
            AnimatedVisibility(visible = isSearchActive && viewModel.recommendedPlaces.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    LazyColumn(contentPadding = PaddingValues(8.dp)) {
                        items(viewModel.recommendedPlaces) { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectLocationFromMap(place.name, place.latitude, place.longitude)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = place.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1C1F1E))
                                    Text(text = place.address, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // 💡 일정 관리 사이드바 (사용자님 원본 완벽 복구)
        // ============================================================
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 16.dp)
        ) {
            if (!isScheduleExpanded) {
                Card(
                    modifier = Modifier.clickable { isScheduleExpanded = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.my_schedule_short),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                            Text(
                                text = "${travelRoute.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .width(330.dp)
                        .fillMaxHeight(0.7f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.my_travel_schedule),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.decreaseDays() },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFFF5F5F5), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF757575)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = stringResource(
                                            id = R.string.nights_days,
                                            viewModel.nights.value,
                                            viewModel.days.value
                                        ),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    IconButton(
                                        onClick = { viewModel.increaseDays() },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFFF5F5F5), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF757575)
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { isScheduleExpanded = false }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.clear))
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                            viewModel.movePlace(from.index, to.index)
                        }

                        val currentRouteList: List<PlaceInfo> = travelRoute.toList()

                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = currentRouteList, key = { it.id }) { place ->
                                ReorderableItem(reorderableState, key = place.id) { isDragging ->
                                    val isHighlighted = place.id == highlightedPlaceId
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(
                                                if (isDragging || isHighlighted) 10.dp else 2.dp,
                                                RoundedCornerShape(14.dp)
                                            ),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isHighlighted) Color(0xFFFFF9C4) else Color.White
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Menu,
                                                    contentDescription = stringResource(id = R.string.change_order),
                                                    tint = Color(0xFFD0D0D0),
                                                    modifier = Modifier.draggableHandle().size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                AsyncImage(
                                                    model = place.imageUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = place.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                    Text(text = place.tag, fontSize = 11.sp, color = Color(0xFF757575))
                                                }
                                                IconButton(
                                                    onClick = { viewModel.removePlace(place) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = stringResource(id = R.string.delete),
                                                        tint = Color(0xFFFF8A80)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(count = viewModel.days.value) { index ->
                                                    val d = index + 1
                                                    val isSelected = place.day == d
                                                    Surface(
                                                        modifier = Modifier.clickable { viewModel.changePlaceDay(place, d) },
                                                        shape = RoundedCornerShape(50),
                                                        color = if (isSelected) getMapDayColor(d) else Color(0xFFF5F5F5)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.day_format, d),
                                                            fontSize = 10.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) Color.White else Color(0xFF9E9E9E),
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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

        // ============================================================
        // 💡 하단에 표시되는 가로 스와이프 UI 영역 (선택된 장소 OR 주변 식당 리스트)
        // ============================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            // (1) 지도에서 마커/검색 클릭했을 때 나오는 단일 상세 카드
            if (viewModel.selectedPlace.value != null) {
                viewModel.selectedPlace.value?.let { place ->
                    PlaceDetailCard(
                        place = place,
                        onClose = { viewModel.selectedPlace.value = null },
                        onAddToRoute = { viewModel.addPlaceToRoute(place) },
                        onFindNearby = {
                            viewModel.searchNearbyRestaurants(place.latitude, place.longitude)
                        },
                        onFetchReviews = {
                            viewModel.fetchPlaceReviews(place.name)
                        },
                        onImageClick = {
                            val urls: List<String> = place.imageUrls.toList()
                            viewerImages = if (urls.isEmpty()) listOf(place.imageUrl) else urls
                        }
                    )
                }
            }
            // (2) 주변 식당 검색 버튼을 눌렀을 때 나오는 [가로 스와이프 LazyRow] UI (사용자님 최애 디자인!)
            else if (viewModel.isFetchingRestaurants.value || viewModel.nearbyRestaurants.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "주변 안식 식당 목록",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1F1E)
                            )
                            IconButton(
                                onClick = { viewModel.nearbyRestaurants.clear() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (viewModel.isFetchingRestaurants.value) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF2E7D32))
                            }
                        } else {
                            // 💡 여기서 가로로 슉슉 넘기는 LazyRow 사용!
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(viewModel.nearbyRestaurants) { restaurant ->
                                    RestaurantHorizontalCard(
                                        restaurant = restaurant,
                                        viewModel = viewModel,
                                        onImageClick = {
                                            val urls: List<String> = restaurant.imageUrls.toList()
                                            viewerImages = if (urls.isEmpty()) listOf(restaurant.firstimage) else urls
                                        },
                                        onReviewClick = {
                                            viewModel.fetchPlaceReviews(restaurant.title)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔥 네이버 리뷰 확인용 바텀시트
        ReviewBottomSheet(viewModel = viewModel)
    }
}

// ============================================================
// 선택된 장소 단일 카드
// ============================================================
@Composable
fun PlaceDetailCard(
    place: PlaceInfo,
    onClose: () -> Unit,
    onAddToRoute: () -> Unit,
    onFindNearby: () -> Unit,
    onFetchReviews: () -> Unit,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = place.tag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = place.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1F1E)
                    )
                    Text(
                        text = place.address,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (place.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddToRoute,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "일정에 추가", fontSize = 13.sp)
                }

                Button(
                    onClick = onFindNearby,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "주변 식당", fontSize = 13.sp)
                }

                Button(
                    onClick = onFetchReviews,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "후기 보기", fontSize = 13.sp, color = Color.Black)
                }
            }
        }
    }
}

// ============================================================
// 💡 가로 스와이프 용 식당 카드 (가로 폭이 고정되어있음)
// ============================================================
@Composable
fun RestaurantHorizontalCard(
    restaurant: TourRestaurant,
    viewModel: MainViewModel,
    onImageClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val rawImageUrl = restaurant.firstimage.ifEmpty { restaurant.firstimage2 }
    val secureImageUrl = rawImageUrl.replace("http://", "https://")

    Card(
        modifier = Modifier
            .width(220.dp) // 💡 카드의 가로 폭을 고정해서 스와이프가 되게 만듦!
            .clickable {
                viewModel.fetchRestaurantDetail(restaurant.contentid)
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = secureImageUrl.ifEmpty { "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400" },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageClick() },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = restaurant.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1C1F1E)
            )

            Text(
                text = restaurant.addr1,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔥 가로 스크롤 카드에서도 리뷰 버튼이 작동하도록 추가
            Surface(
                modifier = Modifier.clickable { onReviewClick() },
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "후기 보기 >",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            // 상세보기 정보 확장 레이아웃 (식당 카드 클릭 시 펼쳐짐)
            if (viewModel.selectedRestaurantDetail.value?.contentid == restaurant.contentid) {
                viewModel.selectedRestaurantDetail.value?.let { detail ->
                    Spacer(modifier = Modifier.height(8.dp))
                    RestaurantInfoRow(title = "대표 메뉴", value = detail.firstmenu ?: "정보 없음")
                    RestaurantInfoRow(title = "영업 시간", value = detail.opentimefood ?: "정보 없음")
                }
            }
        }
    }
}

@Composable
private fun RestaurantInfoRow(title: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Text(text = value, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ============================================================
// 🔥 네이버 블로그 방문 후기 바텀시트
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(viewModel: MainViewModel) {
    if (viewModel.showReviewSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showReviewSheet.value = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "네이버 블로그 방문 후기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1F1E),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (viewModel.isFetchingReviews.value) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2E7D32))
                    }
                } else if (viewModel.selectedPlaceReviews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text(text = "등록된 후기가 없습니다.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    val context = LocalContext.current
                    LazyColumn(modifier = Modifier.padding(bottom = 24.dp)) {
                        items(viewModel.selectedPlaceReviews) { review ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(review.link))
                                        context.startActivity(intent)
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = review.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = review.description,
                                        fontSize = 13.sp,
                                        color = Color.DarkGray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "원문 보기 >",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold
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