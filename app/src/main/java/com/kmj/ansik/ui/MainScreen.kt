package com.kmj.ansik.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.kmj.ansik.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.*
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val cameraPositionState = rememberCameraPositionState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val scheduleListState = rememberLazyListState()

    var isScheduleExpanded by remember { mutableStateOf(false) }
    var highlightedPlaceId by remember { mutableStateOf<String?>(null) }
    var showRadiusDialog by remember { mutableStateOf(false) }
    var showDetailPopup by remember { mutableStateOf(false) }
    var viewerImages by remember { mutableStateOf<List<String>?>(null) }

    // 🔥 에러 수정: AppDialogs에서 제거된 핫플 필터 파라미터 삭제 적용 완료
    AppDialogs(
        viewModel = viewModel,
        viewerImages = viewerImages,
        onDismissViewer = { viewerImages = null },
        showRadiusDialog = showRadiusDialog,
        onDismissRadiusDialog = { showRadiusDialog = false },
        showDetailPopup = showDetailPopup,
        onDismissDetailPopup = { showDetailPopup = false }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMapContent(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState,
            scheduleListState = scheduleListState,
            highlightedPlaceId = highlightedPlaceId,
            coroutineScope = coroutineScope,
            onHighlightPlace = { highlightedPlaceId = it },
            onShowDetail = { showDetailPopup = true }
        )

        TopSearchLayout(
            viewModel = viewModel,
            focusManager = focusManager,
            keyboardController = keyboardController
        )

        ScheduleDrawer(
            viewModel = viewModel,
            isExpanded = isScheduleExpanded,
            onToggleExpand = { isScheduleExpanded = it },
            listState = scheduleListState,
            highlightedPlaceId = highlightedPlaceId
        )

        BottomCards(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState,
            coroutineScope = coroutineScope,
            onShowRadiusDialog = { showRadiusDialog = true },
            onShowDetailPopup = { showDetailPopup = true },
            onShowViewer = { viewerImages = it }
        )
    }

    ReviewBottomSheet(viewModel = viewModel)
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
private fun NaverMapContent(
    viewModel: MainViewModel,
    cameraPositionState: CameraPositionState,
    scheduleListState: LazyListState,
    highlightedPlaceId: String?,
    coroutineScope: CoroutineScope,
    onHighlightPlace: (String?) -> Unit,
    onShowDetail: () -> Unit
) {
    val restaurantMarker = rememberRestaurantMarker()

    LaunchedEffect(viewModel.selectedPlace.value) {
        viewModel.selectedPlace.value?.let { place ->
            val targetLocation = LatLng(place.latitude, place.longitude)
            cameraPositionState.animate(CameraUpdate.scrollTo(targetLocation).animate(CameraAnimation.Easing))
        }
    }

    NaverMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onSymbolClick = { symbol ->
            viewModel.selectLocationFromMap(symbol.caption, symbol.position.latitude, symbol.position.longitude)
            true
        },
        onMapClick = { _, _ -> viewModel.selectedPlace.value = null }
    ) {
        for (d in 1..viewModel.days.value) {
            val coords = viewModel.getRouteCoordsForDay(d)
            if (coords.size >= 2) {
                PathOverlay(coords = coords, width = 5.dp, color = getMapDayColor(d), outlineWidth = 1.dp, outlineColor = Color.White)
            }
        }

        viewModel.selectedPlace.value?.let { place ->
            key("selected_${place.id}") {
                Marker(state = MarkerState(position = LatLng(place.latitude, place.longitude)), icon = MarkerIcons.BLACK, iconTintColor = getMapDayColor(place.day))
            }
        }

        viewModel.travelRoute.forEach { place ->
            val dayIndex = viewModel.travelRoute.filter { it.day == place.day }.indexOf(place) + 1
            val customMarkerIcon = rememberNumberedMarker(number = dayIndex, composeColor = getMapDayColor(place.day))
            key("route_${place.id}") {
                Marker(
                    state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                    icon = customMarkerIcon,
                    onClick = {
                        coroutineScope.launch {
                            val freshIndex = viewModel.travelRoute.indexOfFirst { it.id == place.id }
                            if (freshIndex != -1 && freshIndex < scheduleListState.layoutInfo.totalItemsCount) {
                                scheduleListState.animateScrollToItem(freshIndex)
                            }
                            onHighlightPlace(place.id)
                            delay(800)
                            if (highlightedPlaceId == place.id) onHighlightPlace(null)
                        }
                        true
                    }
                )
            }
        }

        viewModel.nearbyRestaurants.forEach { restaurant ->
            val lat = restaurant.mapy.toDoubleOrNull()
            val lng = restaurant.mapx.toDoubleOrNull()
            if (lat != null && lng != null) {
                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    icon = restaurantMarker,
                    captionText = restaurant.title,
                    onClick = {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdate.scrollTo(LatLng(lat, lng)).animate(CameraAnimation.Easing))
                        }
                        viewModel.fetchRestaurantDetail(restaurant.contentid)
                        onShowDetail()
                        true
                    }
                )
            }
        }
    }
}

@Composable
private fun BoxScope.TopSearchLayout(
    viewModel: MainViewModel,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?
) {
    Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp, start = 16.dp, end = 16.dp).fillMaxWidth()) {
        OutlinedTextField(
            value = viewModel.searchQuery.value,
            onValueChange = { viewModel.searchPlacesRealtime(it) },
            placeholder = { Text("장소, 식당, 관광지 검색...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF2E7D32)) },
            trailingIcon = {
                if (viewModel.searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = ""; viewModel.isSearchActive.value = false }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(50), spotColor = Color(0x26000000)),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White.copy(alpha = 0.98f), unfocusedContainerColor = Color.White.copy(alpha = 0.95f), focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
            singleLine = true
        )

        AnimatedVisibility(visible = viewModel.isSearchActive.value && viewModel.recommendedPlaces.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).padding(top = 12.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
                LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    items(viewModel.recommendedPlaces) { place ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectLocationFromMap(place.name, place.latitude, place.longitude)
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(36.dp).background(Color(0xFFF5F5F5), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = place.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = place.tag, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = place.address, fontSize = 13.sp, color = Color(0xFF9E9E9E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ScheduleDrawer(
    viewModel: MainViewModel,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit,
    listState: LazyListState,
    highlightedPlaceId: String?
) {
    Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 90.dp, end = 16.dp)) {
        if (!isExpanded) {
            Card(modifier = Modifier.clickable { onToggleExpand(true) }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Menu, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(id = R.string.my_schedule_short), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                        Text("${viewModel.travelRoute.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        } else {
            Card(modifier = Modifier.width(330.dp).fillMaxHeight(0.7f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)), elevation = CardDefaults.cardElevation(12.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(stringResource(id = R.string.my_travel_schedule), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.decreaseDays() }, modifier = Modifier.size(24.dp).background(Color(0xFFF5F5F5), CircleShape)) { Icon(Icons.Default.Remove, null, Modifier.size(16.dp)) }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("${viewModel.nights.value}박 ${viewModel.days.value}일", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(10.dp))
                                IconButton(onClick = { viewModel.increaseDays() }, modifier = Modifier.size(24.dp).background(Color(0xFFF5F5F5), CircleShape)) { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) }
                            }
                        }
                        IconButton(onClick = { onToggleExpand(false) }) { Icon(Icons.Default.Close, null) }
                    }
                    HorizontalDivider()

                    val reorderableState = rememberReorderableLazyListState(listState) { from, to -> viewModel.movePlace(from.index, to.index) }
                    LazyColumn(state = listState, contentPadding = PaddingValues(12.dp), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(items = viewModel.travelRoute.toList(), key = { it.id }) { place ->
                            ReorderableItem(reorderableState, key = place.id) { isDragging ->
                                val isHigh = place.id == highlightedPlaceId
                                Card(modifier = Modifier.fillMaxWidth().shadow(if (isDragging || isHigh) 10.dp else 2.dp, RoundedCornerShape(14.dp)), colors = CardDefaults.cardColors(containerColor = if (isHigh) Color(0xFFFFF9C4) else Color.White)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Menu, null, tint = Color.LightGray, modifier = Modifier.draggableHandle().size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AsyncImage(model = place.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(place.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                Text(place.tag, fontSize = 11.sp, color = Color.Gray)
                                            }
                                            IconButton(onClick = { viewModel.removePlace(place) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF8A80)) }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(count = viewModel.days.value) { index ->
                                                val d = index + 1
                                                val isSelected = place.day == d
                                                Surface(modifier = Modifier.clickable { viewModel.changePlaceDay(place, d) }, shape = RoundedCornerShape(50), color = if (isSelected) getMapDayColor(d) else Color(0xFFF5F5F5)) {
                                                    Text("${d}일차", fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else Color.Gray, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
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
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
private fun BoxScope.BottomCards(
    viewModel: MainViewModel,
    cameraPositionState: CameraPositionState,
    coroutineScope: CoroutineScope,
    onShowRadiusDialog: () -> Unit,
    onShowDetailPopup: () -> Unit,
    onShowViewer: (List<String>) -> Unit
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = viewModel.selectedPlace.value != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        viewModel.selectedPlace.value?.let { place ->
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(20.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row {
                        AsyncImage(model = place.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(82.dp).clip(RoundedCornerShape(14.dp)).clickable { onShowViewer(place.imageUrls.ifEmpty { listOf(place.imageUrl) }) })
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(place.name, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(place.address, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://m.map.naver.com/search.naver?query=${Uri.encode(place.name)}"))) }, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp)) { Text("길찾기/로드뷰", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        Button(onClick = { viewModel.addPlaceToRoute(place) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp)) { Text("일정에 추가", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.searchNearbyRestaurants(place.latitude, place.longitude) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)), modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp)) { Text("주변 안심 식당 찾기", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                        OutlinedButton(
                            onClick = { viewModel.fetchPlaceReviews(place.name, place.address) },
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "리뷰", tint = Color(0xFFFBC02D))
                        }

                        OutlinedButton(onClick = onShowRadiusDialog, modifier = Modifier.size(46.dp), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.Tune, null, tint = Color(0xFF1976D2)) }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = viewModel.selectedPlace.value == null && (viewModel.isFetchingRestaurants.value || viewModel.nearbyRestaurants.isNotEmpty()),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("주변 안식 식당 목록", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.nearbyRestaurants.clear() }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (viewModel.isFetchingRestaurants.value) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2E7D32)) }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(viewModel.nearbyRestaurants) { restaurant ->
                            RestaurantHorizontalCard(
                                restaurant = restaurant,
                                onCardClick = {
                                    val lat = restaurant.mapy.toDoubleOrNull() ?: 0.0
                                    val lng = restaurant.mapx.toDoubleOrNull() ?: 0.0
                                    if (lat != 0.0 && lng != 0.0) {
                                        coroutineScope.launch {
                                            cameraPositionState.animate(CameraUpdate.scrollTo(LatLng(lat, lng)).animate(CameraAnimation.Easing))
                                        }
                                    }
                                },
                                onImageClick = { onShowViewer(restaurant.imageUrls.ifEmpty { listOf(restaurant.firstimage.ifEmpty { restaurant.firstimage2 }) }) },
                                onReviewClick = { viewModel.fetchPlaceReviews(restaurant.title, restaurant.addr1) },
                                onDetailClick = { viewModel.fetchRestaurantDetail(restaurant.contentid); onShowDetailPopup() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantHorizontalCard(
    restaurant: TourRestaurant,
    onCardClick: () -> Unit,
    onImageClick: () -> Unit,
    onReviewClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    val rawImageUrl = restaurant.firstimage.ifEmpty { restaurant.firstimage2 }
    val secureImageUrl = rawImageUrl.replace("http://", "https://")

    Card(
        modifier = Modifier.width(230.dp).clickable { onCardClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(model = secureImageUrl.ifEmpty { "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400" }, contentDescription = null, modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(8.dp)).clickable { onImageClick() }, contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.height(8.dp))
            Text(restaurant.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(restaurant.addr1, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(modifier = Modifier.weight(1f).clickable { onReviewClick() }, color = Color(0xFFFFF9C4), shape = RoundedCornerShape(6.dp)) {
                    Text("후기 보기", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                }
                Surface(modifier = Modifier.weight(1f).clickable { onDetailClick() }, color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                    Text("상세 보기", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

// ============================================================
// 6. 팝업 / 다이얼로그 모음
// ============================================================
@Composable
private fun AppDialogs(
    viewModel: MainViewModel,
    viewerImages: List<String>?,
    onDismissViewer: () -> Unit,
    showRadiusDialog: Boolean,
    onDismissRadiusDialog: () -> Unit,
    showDetailPopup: Boolean,
    onDismissDetailPopup: () -> Unit
) {
    viewerImages?.let { FullScreenImageViewer(imageUrls = it, onDismiss = onDismissViewer) }

    if (showRadiusDialog) {
        AlertDialog(
            onDismissRequest = onDismissRadiusDialog,
            title = { Text(text = stringResource(id = R.string.radius_settings_title), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column {
                    var tempRadius by remember { mutableFloatStateOf(viewModel.searchRadius.intValue.toFloat()) }
                    val snappedRadius = (tempRadius / 100).toInt() * 100
                    val radiusText = if (snappedRadius >= 1000) { if (snappedRadius % 1000 == 0) "${snappedRadius / 1000}km" else String.format("%.1fkm", snappedRadius / 1000f) } else { "${snappedRadius}m" }
                    Text(text = radiusText, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32), modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(value = tempRadius, onValueChange = { tempRadius = it }, onValueChangeFinished = { viewModel.updateSearchRadius(snappedRadius) }, valueRange = 100f..3000f, colors = SliderDefaults.colors(thumbColor = Color(0xFF2E7D32), activeTrackColor = Color(0xFF2E7D32), inactiveTrackColor = Color(0xFFE8F5E9)))
                }
            },
            confirmButton = { TextButton(onClick = onDismissRadiusDialog) { Text(text = stringResource(id = R.string.confirm), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) } },
            containerColor = Color.White
        )
    }

    if (showDetailPopup && viewModel.selectedRestaurantDetail.value != null) {
        AlertDialog(
            onDismissRequest = onDismissDetailPopup,
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("식당 상세 정보", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32)) },
            text = {
                viewModel.selectedRestaurantDetail.value?.let { detail ->
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        if (!detail.firstmenu.isNullOrBlank()) RestaurantInfoRow("대표 메뉴", detail.firstmenu!!)
                        if (!detail.treatmenu.isNullOrBlank()) RestaurantInfoRow("취급 메뉴", detail.treatmenu!!)
                        if (!detail.opentimefood.isNullOrBlank()) RestaurantInfoRow("영업 시간", detail.opentimefood!!)
                        if (!detail.packing.isNullOrBlank()) RestaurantInfoRow("포장 여부", detail.packing!!)
                        if (!detail.parkingfood.isNullOrBlank()) RestaurantInfoRow("주차 정보", detail.parkingfood!!)
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismissDetailPopup) { Text("닫기", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(viewModel: MainViewModel) {
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 1
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && viewModel.hasMoreReviews.value && !viewModel.isFetchingReviews.value) {
            viewModel.fetchPlaceReviews(viewModel.currentReviewPlaceName, viewModel.currentReviewAddress, isLoadMore = true)
        }
    }

    if (viewModel.showReviewSheet.value) {
        ModalBottomSheet(onDismissRequest = { viewModel.showReviewSheet.value = false }, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("네이버 블로그 방문 후기", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                if (viewModel.isFetchingReviews.value && viewModel.selectedPlaceReviews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2E7D32)) }
                } else if (viewModel.selectedPlaceReviews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { Text("등록된 후기가 없습니다.", color = Color.Gray) }
                } else {
                    val context = LocalContext.current
                    LazyColumn(state = listState, modifier = Modifier.padding(bottom = 24.dp)) {
                        items(viewModel.selectedPlaceReviews) { review ->
                            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(review.link))) }, colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(review.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(review.description, fontSize = 13.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("원문 보기 >", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (viewModel.isFetchingReviews.value) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
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
// 유틸리티
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
fun FullScreenImageViewer(imageUrls: List<String>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            val pagerState = rememberPagerState(pageCount = { imageUrls.size })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(model = imageUrls[page], contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
            }
            Text(text = "${pagerState.currentPage + 1} / ${imageUrls.size}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun rememberNumberedMarker(number: Int, composeColor: Color): OverlayImage {
    return remember(number, composeColor) {
        val size = 90
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true; color = composeColor.toArgb() }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        val borderPaint = Paint().apply { isAntiAlias = true; color = android.graphics.Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 6f }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 3f, borderPaint)
        val textPaint = Paint().apply { isAntiAlias = true; color = android.graphics.Color.WHITE; textSize = 42f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
        val fontMetrics = textPaint.fontMetrics
        canvas.drawText(number.toString(), size / 2f, (size / 2f) - (fontMetrics.ascent + fontMetrics.descent) / 2f, textPaint)
        OverlayImage.fromBitmap(bitmap)
    }
}

@Composable
private fun rememberTouristMarker(): OverlayImage {
    return remember {
        val size = 50
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, Paint().apply { isAntiAlias = true; color = android.graphics.Color.rgb(46, 125, 50) })
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 5f, Paint().apply { isAntiAlias = true; color = android.graphics.Color.WHITE })
        canvas.drawCircle(size / 2f, size / 2f, 10f, Paint().apply { isAntiAlias = true; color = android.graphics.Color.rgb(46, 125, 50) })
        OverlayImage.fromBitmap(bitmap)
    }
}

@Composable
private fun rememberRestaurantMarker(): OverlayImage {
    return remember {
        val size = 50
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, Paint().apply { isAntiAlias = true; color = android.graphics.Color.rgb(25, 118, 210) })
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 5f, Paint().apply { isAntiAlias = true; color = android.graphics.Color.WHITE })
        canvas.drawCircle(size / 2f, size / 2f, 10f, Paint().apply { isAntiAlias = true; color = android.graphics.Color.rgb(25, 118, 210) })
        OverlayImage.fromBitmap(bitmap)
    }
}

@Composable
private fun RestaurantInfoRow(title: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 14.sp, color = Color(0xFF424242), maxLines = 4)
        HorizontalDivider(modifier = Modifier.padding(top = 6.dp), color = Color(0xFFEEEEEE))
    }
}