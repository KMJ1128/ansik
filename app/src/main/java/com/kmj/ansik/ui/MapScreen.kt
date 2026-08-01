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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import com.naver.maps.map.compose.*
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

// 무지개 순서 Day 색상 팔레트
val DayColorPalette = listOf(
    Color(0xFFE53935), // 1일차: 빨강
    Color(0xFFFF7043), // 2일차: 주황
    Color(0xFFFFCA28), // 3일차: 노랑
    Color(0xFF43A047), // 4일차: 초록
    Color(0xFF1E88E5), // 5일차: 파랑
    Color(0xFF3949AB), // 6일차: 남색
    Color(0xFF8E24AA), // 7일차: 보라
    Color(0xFFD81B60), // 8일차: 핑크
    Color(0xFF6D4C41), // 9일차: 갈색
    Color(0xFF546E7A)  // 10일차: 블루그레이
)

fun getDayColor(day: Int): Color {
    if (day < 1) return Color.Gray
    val index = (day - 1) % DayColorPalette.size
    return DayColorPalette[index]
}

// 번호가 박힌 커스텀 원형 핀 이미지를 생성하는 함수
@Composable
fun rememberNumberedMarker(number: Int, composeColor: Color): OverlayImage {
    return remember(number, composeColor) {
        val size = 90 // 마커 크기
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 핀 배경색 (Day 컬러)
        val paint = Paint().apply {
            isAntiAlias = true
            color = composeColor.toArgb()
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // 하얀색 테두리
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 3f, borderPaint)

        // 가운데 번호 텍스트
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 45f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val fontMetrics = textPaint.fontMetrics
        val textY = (size / 2f) - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(number.toString(), size / 2f, textY, textPaint)

        OverlayImage.fromBitmap(bitmap)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(viewModel: MainViewModel) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val searchQuery by viewModel.searchQuery
    val isSearchActive by viewModel.isSearchActive
    val selectedPlace by viewModel.selectedPlace
    val travelRoute = viewModel.travelRoute

    val cameraPositionState = rememberCameraPositionState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var highlightedPlaceId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            val targetLocation = LatLng(place.latitude, place.longitude)
            cameraPositionState.animate(CameraUpdate.scrollTo(targetLocation))
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 240.dp,
        sheetContainerColor = Color.White,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .heightIn(min = 240.dp, max = 720.dp)
            ) {
                val headerInteractionSource = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = headerInteractionSource,
                            indication = null
                        ) {
                            coroutineScope.launch {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    scaffoldState.bottomSheetState.partialExpand()
                                } else {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("내 여행 일정", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded)
                                Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "접기/펼치기",
                            tint = Color.Gray
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (viewModel.nights.value > 0) { viewModel.nights.value--; viewModel.days.value-- } }) { Text("−", fontSize = 24.sp) }
                        Text("${viewModel.nights.value}박 ${viewModel.days.value}일", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        IconButton(onClick = { viewModel.nights.value++; viewModel.days.value++ }) { Text("+", fontSize = 24.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    viewModel.movePlace(from.index, to.index)
                }

                LazyColumn(
                    state = lazyListState,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                lazyListState.dispatchRawDelta(-dragAmount)
                            }
                        },
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = travelRoute, key = { it.id }) { place ->
                        ReorderableItem(reorderableState, key = place.id) { isDragging ->
                            val elevation = if (isDragging) 8.dp else 0.dp
                            val dayColor = getDayColor(place.day)

                            val isHighlighted = (place.id == highlightedPlaceId)
                            val cardBgColor = when {
                                isDragging -> Color(0xFFE8F5E9)
                                isHighlighted -> Color(0xFFFFF9C4)
                                else -> Color(0xFFF9F9F9)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                border = BorderStroke(2.dp, dayColor)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "순서 변경",
                                            tint = Color.LightGray,
                                            modifier = Modifier
                                                .draggableHandle()
                                                .padding(end = 12.dp)
                                        )
                                        AsyncImage(
                                            model = place.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(place.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(place.tag, color = Color(0xFF2E7D32), fontSize = 12.sp)
                                        }
                                        IconButton(onClick = { viewModel.removePlace(place) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFFFF5252))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(count = viewModel.days.value) { index ->
                                            val d = index + 1
                                            val isSelected = (place.day == d)
                                            val chipColor = getDayColor(d)

                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.changePlaceDay(place, d) },
                                                label = {
                                                    Text(
                                                        text = "Day $d",
                                                        fontSize = 11.sp,
                                                        maxLines = 1
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = chipColor,
                                                    selectedLabelColor = Color.White
                                                ),
                                                modifier = Modifier.height(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 🚀 [해결 핵심]: 1000.dp로 늘려 마지막 최신 일정도 무조건 바텀시트 상단으로 스크롤 가능하게 확보
                    item { Spacer(modifier = Modifier.height(1000.dp)) }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                            width = 6.dp,
                            color = getDayColor(d),
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
                            iconTintColor = getDayColor(place.day)
                        )
                    }
                }

                travelRoute.forEach { place ->
                    val dayColor = getDayColor(place.day)
                    val dayIndex = travelRoute.filter { it.day == place.day }.indexOf(place) + 1

                    val customMarkerIcon = rememberNumberedMarker(number = dayIndex, composeColor = dayColor)

                    key("route_${place.id}") {
                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                            icon = customMarkerIcon,
                            onClick = {
                                coroutineScope.launch {
                                    // 🚀 클릭하는 즉시 실시간 인덱스를 다시 추적해서 최신 항목도 완벽하게 잡아냄
                                    val freshIndex = viewModel.travelRoute.indexOfFirst { it.id == place.id }
                                    if (freshIndex != -1) {
                                        lazyListState.animateScrollToItem(freshIndex)
                                    }

                                    highlightedPlaceId = place.id
                                    delay(800)
                                    if (highlightedPlaceId == place.id) {
                                        highlightedPlaceId = null
                                    }
                                }
                                true
                            }
                        )
                    }
                }
            }

            // 상단 검색바
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchPlacesRealtime(it) },
                    placeholder = { Text("장소, 식당, 카페 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = ""; viewModel.isSearchActive.value = false }) {
                                Icon(Icons.Default.Close, contentDescription = "지우기")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                AnimatedVisibility(visible = isSearchActive) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            viewModel.recommendedPlaces.forEach { place ->
                                val interactionSource = remember { MutableInteractionSource() }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {
                                            viewModel.selectedPlace.value = place
                                            viewModel.isSearchActive.value = false
                                            keyboardController?.hide()
                                        }
                                        .background(Color.White)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, tint = Color.LightGray, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(place.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(place.tag, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(place.address, fontSize = 13.sp, color = Color.DarkGray)
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                            }
                        }
                    }
                }
            }

            // 하단 선택 장소 카드 팝업
            AnimatedVisibility(
                visible = selectedPlace != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                selectedPlace?.let { place ->
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = place.imageUrl,
                                    contentDescription = "장소 이미지",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(place.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(place.address, fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(place.tag, color = Color(0xFF2E7D32)) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFE8F5E9)),
                                        border = null
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val encodedName = Uri.encode(place.name)
                                        val url = "https://m.map.naver.com/search.naver?query=$encodedName"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2E7D32))
                                ) {
                                    Text("네이버 길찾기/로드뷰", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.addPlaceToRoute(place)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "일정에 추가", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("일정 추가", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}