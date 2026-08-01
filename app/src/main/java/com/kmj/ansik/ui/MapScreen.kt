package com.kmj.ansik.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.LocationOn
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

// 번호가 박힌 커스텀 원형 핀 이미지를 생성하는 함수
@Composable
fun rememberNumberedMarker(number: Int, composeColor: Color): OverlayImage {
    return remember(number, composeColor) {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            color = composeColor.toArgb()
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 4f, borderPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 46f
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
        sheetPeekHeight = 250.dp,
        sheetContainerColor = AppColors.Surface,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .heightIn(min = 250.dp, max = 720.dp)
            ) {
                // 드래그 핸들 바
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 14.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppColors.Divider)
                )

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
                    Column {
                        Text("내 여행 일정", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text("${travelRoute.size}개 장소 추가됨", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.SurfaceMuted)
                            .padding(horizontal = 6.dp)
                    ) {
                        IconButton(onClick = { if (viewModel.nights.value > 0) { viewModel.nights.value--; viewModel.days.value-- } }) {
                            Text("−", fontSize = 22.sp, color = AppColors.PrimaryDark, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "${viewModel.nights.value}박 ${viewModel.days.value}일",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AppColors.PrimaryDark
                        )
                        IconButton(onClick = { viewModel.nights.value++; viewModel.days.value++ }) {
                            Text("+", fontSize = 22.sp, color = AppColors.PrimaryDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    viewModel.movePlace(from.index, to.index)
                }

                if (travelRoute.isEmpty()) {
                    EmptyRouteHint()
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
                            val elevation = if (isDragging) 10.dp else 2.dp
                            val dayColor = getDayColor(place.day)
                            val dayIndex = travelRoute.filter { it.day == place.day }.indexOf(place) + 1

                            val isHighlighted = (place.id == highlightedPlaceId)
                            val cardBgColor = when {
                                isDragging -> AppColors.SurfaceMuted
                                isHighlighted -> Color(0xFFFFF6D9)
                                else -> AppColors.Surface
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.08f))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBgColor)
                            ) {
                                // 컬러 액센트 바
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .fillMaxHeight()
                                        .background(dayColor)
                                )
                                Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "순서 변경",
                                            tint = AppColors.TextSecondary.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .draggableHandle()
                                                .padding(end = 10.dp)
                                        )
                                        AsyncImage(
                                            model = place.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(place.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(dayColor.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Day ${place.day} · $dayIndex", fontSize = 10.sp, color = dayColor, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(place.tag, color = AppColors.TextSecondary, fontSize = 12.sp)
                                            }
                                        }
                                        IconButton(onClick = { viewModel.removePlace(place) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = AppColors.Accent, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

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
                                                    Text(text = "Day $d", fontSize = 11.sp, maxLines = 1)
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = AppColors.SurfaceMuted,
                                                    selectedContainerColor = chipColor,
                                                    labelColor = AppColors.TextSecondary,
                                                    selectedLabelColor = Color.White
                                                ),
                                                border = null,
                                                modifier = Modifier.height(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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

            // 상단 플로팅 검색바
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.12f))
                        .clip(RoundedCornerShape(28.dp))
                        .background(AppColors.Surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "검색", tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextFieldSearch(
                        value = searchQuery,
                        onValueChange = { viewModel.searchPlacesRealtime(it) },
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = ""; viewModel.isSearchActive.value = false }) {
                            Icon(Icons.Default.Close, contentDescription = "지우기", tint = AppColors.TextSecondary)
                        }
                    }
                }

                AnimatedVisibility(visible = isSearchActive) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppColors.Surface)
                    ) {
                        viewModel.recommendedPlaces.forEachIndexed { index, place ->
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
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.SurfaceMuted),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationOn, tint = AppColors.Primary, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(place.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(AppColors.SurfaceMuted)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(place.tag, fontSize = 10.sp, color = AppColors.TextSecondary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(place.address, fontSize = 12.sp, color = AppColors.TextSecondary)
                                }
                            }
                            if (index < viewModel.recommendedPlaces.lastIndex) {
                                HorizontalDivider(color = AppColors.Divider)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.15f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(AppColors.Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = place.imageUrl,
                                    contentDescription = "장소 이미지",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(place.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                        IconButton(
                                            onClick = { viewModel.selectedPlace.value = null },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "닫기", tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(place.address, fontSize = 12.sp, color = AppColors.TextSecondary)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.Primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(place.tag, color = AppColors.PrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = AppColors.Divider)
                            Spacer(modifier = Modifier.height(14.dp))

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
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.PrimaryDark)
                                ) {
                                    Text("길찾기/로드뷰", fontSize = 12.sp, color = AppColors.PrimaryDark, fontWeight = FontWeight.Bold)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AppColors.PrimaryDark)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { viewModel.addPlaceToRoute(place) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = "일정에 추가", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("일정 추가", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

// 검색창 전용 심플 텍스트필드 (배경 없는 투명 스타일)
@Composable
private fun BasicTextFieldSearch(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("장소, 식당, 카페 검색", color = AppColors.TextSecondary, fontSize = 14.sp) },
        singleLine = true,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = AppColors.Primary
        )
    )
}

@Composable
private fun EmptyRouteHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.SurfaceMuted)
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🗺️", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("지도에서 장소를 검색하고 추가해 보세요", fontSize = 13.sp, color = AppColors.TextSecondary)
        }
    }
}