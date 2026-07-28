package com.kmj.ansik.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
// 🚀 변경: 유지보수가 끊긴 org.burnoutcrew.reorderable 대신
//    sh.calvin.reorderable 라이브러리로 교체 (일정 추가 시 크래시 원인)
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(viewModel: MainViewModel) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    val searchQuery by viewModel.searchQuery
    val isSearchActive by viewModel.isSearchActive
    val selectedPlace by viewModel.selectedPlace
    val travelRoute = viewModel.travelRoute

    val cameraPositionState = rememberCameraPositionState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            val targetLocation = LatLng(place.latitude, place.longitude)
            cameraPositionState.animate(CameraUpdate.scrollTo(targetLocation))
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 280.dp,
        sheetContainerColor = Color.White,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .heightIn(min = 300.dp, max = 700.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("내 여행 일정", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (viewModel.nights.value > 0) { viewModel.nights.value--; viewModel.days.value-- } }) { Text("−", fontSize = 24.sp) }
                        Text("${viewModel.nights.value}박 ${viewModel.days.value}일", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        IconButton(onClick = { viewModel.nights.value++; viewModel.days.value++ }) { Text("+", fontSize = 24.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 🚀 변경: 이 라이브러리는 LazyListState를 직접 만들어서 넘겨줘야 함 (.listState 프로퍼티 없음)
                val lazyListState = rememberLazyListState()
                val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    viewModel.movePlace(from.index, to.index)
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = travelRoute, key = { it.id }) { place ->
                        ReorderableItem(reorderableState, key = place.id) { isDragging ->
                            val elevation = if (isDragging) 8.dp else 0.dp
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = if (isDragging) Color(0xFFE8F5E9) else Color(0xFFF9F9F9)),
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "순서 변경",
                                        tint = Color.LightGray,
                                        // 🚀 변경: detectReorder(state) 대신 draggableHandle() 사용
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
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
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
                // 🚀 네이버 지도처럼 특정 건물/상점/지역을 클릭했을 때의 이벤트
                onSymbolClick = { symbol ->
                    viewModel.selectLocationFromMap(symbol.caption, symbol.position.latitude, symbol.position.longitude)
                    keyboardController?.hide()
                    true // true 반환 시 클릭 이벤트 소비
                },
                // 🚀 지도 상의 빈 공간(도로 등)을 클릭했을 때 정보창 닫기
                onMapClick = { _, _ ->
                    viewModel.selectedPlace.value = null
                    keyboardController?.hide()
                }
            ) {
                selectedPlace?.let { place ->
                    key("selected_${place.id}") {
                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude))
                            // 🚀 captionText 삭제로 텍스트 라벨 미노출
                        )
                    }
                }

                travelRoute.forEach { place ->
                    key("route_${place.id}") {
                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude))
                            // 🚀 여기도 캡션 제거 완료
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.searchPlacesRealtime(it)
                    },
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
                                            indication = LocalIndication.current,
                                            onClick = {
                                                viewModel.selectedPlace.value = place
                                                viewModel.isSearchActive.value = false
                                                keyboardController?.hide()
                                            }
                                        )
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
                        Row(modifier = Modifier.padding(16.dp)) {
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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(place.tag, color = Color(0xFF2E7D32)) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFE8F5E9)),
                                        border = null
                                    )

                                    Button(
                                        onClick = { viewModel.addPlaceToRoute(place) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "일정에 추가", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("일정 추가", fontSize = 12.sp)
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