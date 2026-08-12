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
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
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
    Color(0xFFE53935), Color(0xFFFF7043), Color(0xFFFFCA28), Color(0xFF43A047),
    Color(0xFF1E88E5), Color(0xFF3949AB), Color(0xFF8E24AA), Color(0xFFD81B60),
    Color(0xFF6D4C41), Color(0xFF546E7A)
)

private fun getMapDayColor(day: Int): Color {
    if (day < 1) return Color.Gray
    return MapScreenDayColorPalette[(day - 1) % MapScreenDayColorPalette.size]
}

// ============================================================
// 일정 번호 Marker
// ============================================================
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

// ============================================================
// 식당 Marker
// ============================================================
@Composable
private fun rememberRestaurantMarker(): OverlayImage {
    return remember {
        val size = 80
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
        canvas.drawCircle(size / 2f, size / 2f, 27f, innerPaint)

        val centerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(25, 118, 210)
        }
        canvas.drawCircle(size / 2f, size / 2f, 10f, centerPaint)

        OverlayImage.fromBitmap(bitmap)
    }
}

// ============================================================
// MapScreen
// ============================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(viewModel: MainViewModel) {
    val searchQuery by viewModel.searchQuery
    val isSearchActive by viewModel.isSearchActive
    val selectedPlace by viewModel.selectedPlace

    val travelRoute = viewModel.travelRoute
    val nearbyRestaurants = viewModel.nearbyRestaurants
    val selectedRestaurantDetail by viewModel.selectedRestaurantDetail
    val isFetchingRestaurants by viewModel.isFetchingRestaurants

    val cameraPositionState = rememberCameraPositionState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val restaurantMarker = rememberRestaurantMarker()

    var highlightedPlaceId by remember { mutableStateOf<String?>(null) }
    var isScheduleExpanded by remember { mutableStateOf(false) }
    var showRestaurantSheet by remember { mutableStateOf(false) }
    var showRadiusDialog by remember { mutableStateOf(false) }
    val restaurantSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            val targetLocation = LatLng(place.latitude, place.longitude)
            cameraPositionState.animate(CameraUpdate.scrollTo(targetLocation))
        }
    }

    // =========================================
    // 반경 설정 다이얼로그 (팝업)
    // =========================================
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
                    // 슬라이더 조작 시 임시로 보여줄 상태
                    var tempRadius by remember { mutableFloatStateOf(viewModel.searchRadius.intValue.toFloat()) }

                    // 100m 단위로 보정된 값
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

                    // 점(steps) 제거된 부드러운 슬라이더
                    Slider(
                        value = tempRadius,
                        onValueChange = { tempRadius = it },
                        onValueChangeFinished = {
                            // 드래그 종료 시 ViewModel(및 SharedPreferences) 업데이트
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
                    Text(
                        text = stringResource(id = R.string.confirm),
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. 지도
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onSymbolClick = { symbol ->
                viewModel.selectLocationFromMap(symbol.caption, symbol.position.latitude, symbol.position.longitude)
                keyboardController?.hide()
                true
            },
            onMapClick = { _, _ ->
                viewModel.selectedPlace.value = null
                keyboardController?.hide()
            }
        ) {
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

            selectedPlace?.let { place ->
                key("selected_${place.id}") {
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        icon = MarkerIcons.BLACK,
                        iconTintColor = getMapDayColor(place.day)
                    )
                }
            }

            travelRoute.forEach { place ->
                val dayColor = getMapDayColor(place.day)
                val dayIndex = travelRoute.filter { it.day == place.day }.indexOf(place) + 1
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

            nearbyRestaurants.forEach { restaurant ->
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

        // 2. 우측 상단 '내 일정' 패널
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

                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = travelRoute, key = { it.id }) { place ->
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

        // 3. 하단 장소 상세 카드
        AnimatedVisibility(
            visible = selectedPlace != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            selectedPlace?.let { place ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(20.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row {
                            AsyncImage(
                                model = place.imageUrl,
                                contentDescription = stringResource(id = R.string.place_image),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(82.dp).clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = place.name, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = place.address, fontSize = 12.sp, color = Color(0xFF757575), maxLines = 2)
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = place.tag,
                                        color = Color(0xFF2E7D32),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val encodedName = Uri.encode(place.name)
                                    val url = "https://m.map.naver.com/search.naver?query=$encodedName"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.directions_roadview),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { viewModel.addPlaceToRoute(place) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = stringResource(id = R.string.add_schedule),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // =========================================
                        // 식당 찾기 버튼 + 설정(필터) 아이콘 배치
                        // =========================================
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.searchNearbyRestaurants(lat = place.latitude, lng = place.longitude)
                                    showRestaurantSheet = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isFetchingRestaurants) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = stringResource(id = R.string.loading_public_data))
                                } else {
                                    Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(id = R.string.find_nearby_safe_restaurants),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 팝업을 여는 설정(필터) 아이콘 버튼
                            OutlinedButton(
                                onClick = { showRadiusDialog = true },
                                modifier = Modifier.size(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = stringResource(id = R.string.radius_settings_title),
                                    tint = Color(0xFF1976D2)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. 검색창 (가장 마지막에 그려서 자동완성 목록이 일정 패널 위로 오게 함)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchPlacesRealtime(it) },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_placeholder),
                        color = Color(0xFF9E9E9E)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.search),
                        tint = Color(0xFF2E7D32)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.searchQuery.value = ""
                            viewModel.isSearchActive.value = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.clear),
                                tint = Color.Gray
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
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

            AnimatedVisibility(visible = isSearchActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .shadow(16.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        viewModel.recommendedPlaces.forEach { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedPlace.value = place
                                        viewModel.isSearchActive.value = false
                                        keyboardController?.hide()
                                    }
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFF5F5F5), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = Color(0xFFBDBDBD),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = place.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = place.tag, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = place.address, fontSize = 13.sp, color = Color(0xFF9E9E9E))
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF5F5F5))
                        }
                    }
                }
            }
        }
    }

    if (showRestaurantSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRestaurantSheet = false },
            sheetState = restaurantSheetState,
            containerColor = Color.White,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .width(42.dp)
                        .height(5.dp)
                        .background(Color(0xFFE0E0E0), CircleShape)
                )
            }
        ) {
            RestaurantListContent(
                restaurants = nearbyRestaurants,
                selectedDetail = selectedRestaurantDetail,
                radius = viewModel.searchRadius.intValue, // 바텀 시트에 반경 전달
                onRestaurantClick = { viewModel.fetchRestaurantDetail(it.contentid) }
            )
        }
    }
}

// ============================================================
// 식당 리스트 바텀 시트
// ============================================================
@Composable
private fun RestaurantListContent(
    restaurants: List<TourRestaurant>,
    selectedDetail: TourRestaurantDetail?,
    radius: Int,
    onRestaurantClick: (TourRestaurant) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 700.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = stringResource(id = R.string.restaurant_list_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // 포맷팅 적용 (500m 또는 1.5km 형태)
            val radiusStr = if (radius >= 1000) {
                if (radius % 1000 == 0) "${radius / 1000}km"
                else String.format("%.1fkm", radius / 1000f)
            } else {
                "${radius}m"
            }

            Text(
                text = stringResource(id = R.string.restaurant_list_desc, radiusStr),
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (restaurants.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.no_restaurants_found),
                    fontSize = 15.sp,
                    color = Color(0xFF757575)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(restaurants, key = { it.contentid }) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        detail = if (selectedDetail?.contentid == restaurant.contentid) selectedDetail else null,
                        onClick = { onRestaurantClick(restaurant) }
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
    restaurant: TourRestaurant,
    detail: TourRestaurantDetail?,
    onClick: () -> Unit
) {
    // 💡 핵심 수정: http 주소를 https로 강제 변환하여 이미지 로드 차단 해결
    val rawImageUrl = restaurant.firstimage.ifEmpty { restaurant.firstimage2 }
    val secureImageUrl = rawImageUrl.replace("http://", "https://")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            AsyncImage(
                model = secureImageUrl, // 💡 변환된 안전한 URL 적용
                contentDescription = restaurant.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = restaurant.title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = listOf(restaurant.addr1, restaurant.addr2).filter { it.isNotBlank() }.joinToString(" "),
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (detail != null) {
                    if (!detail.firstmenu.isNullOrBlank()) {
                        RestaurantInfoRow(title = stringResource(id = R.string.restaurant_main_menu), value = detail.firstmenu!!)
                    }
                    if (!detail.treatmenu.isNullOrBlank()) {
                        RestaurantInfoRow(title = stringResource(id = R.string.restaurant_treat_menu), value = detail.treatmenu!!)
                    }
                    if (!detail.opentimefood.isNullOrBlank()) {
                        RestaurantInfoRow(title = stringResource(id = R.string.restaurant_open_time), value = detail.opentimefood!!)
                    }
                    if (!detail.parkingfood.isNullOrBlank()) {
                        RestaurantInfoRow(title = stringResource(id = R.string.restaurant_parking), value = detail.parkingfood!!)
                    }
                    if (!detail.packing.isNullOrBlank()) {
                        RestaurantInfoRow(title = stringResource(id = R.string.restaurant_packing), value = detail.packing!!)
                    }
                } else {
                    Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = stringResource(id = R.string.tap_to_view_details),
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
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
private fun RestaurantInfoRow(title: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Text(text = value, fontSize = 13.sp, color = Color(0xFF424242), maxLines = 3)
    }
}