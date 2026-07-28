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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.NaverMap
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(viewModel: MainViewModel) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    val searchQuery by viewModel.searchQuery
    val isSearchActive by viewModel.isSearchActive
    val selectedPlace by viewModel.selectedPlace
    val travelRoute = viewModel.travelRoute

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 280.dp, // 바텀시트 기본 노출 높이
        sheetContainerColor = Color.White,
        sheetContent = {
            // ==========================================
            // 1. 바텀시트 영역: 일정 설정 및 리스트 (드래그 앤 드롭)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .heightIn(min = 300.dp, max = 700.dp)
            ) {
                // 일정 조율기 (몇 박 몇 일)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("내 여행 일정", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if(viewModel.nights.value > 0) { viewModel.nights.value--; viewModel.days.value-- } }) { Text("−", fontSize = 24.sp) }
                        Text("${viewModel.nights.value}박 ${viewModel.days.value}일", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        IconButton(onClick = { viewModel.nights.value++; viewModel.days.value++ }) { Text("+", fontSize = 24.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 🚀 드래그 앤 드롭 리스트 설정
                val state = rememberReorderableLazyListState(onMove = { from, to ->
                    viewModel.movePlace(from.index, to.index)
                })

                LazyColumn(
                    state = state.listState,
                    modifier = Modifier.reorderable(state),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🚀 key 파라미터를 명시적으로 선언하여 컴파일 오류 방지
                    items(items = travelRoute, key = { it.id }) { place ->
                        ReorderableItem(state, key = place.id) { isDragging ->
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
                                    // 드래그 핸들 아이콘 (이 부분을 잡고 끌어올림)
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "순서 변경",
                                        tint = Color.LightGray,
                                        modifier = Modifier.detectReorder(state).padding(end = 12.dp)
                                    )

                                    // 썸네일 이미지
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
        // ==========================================
        // 2. 메인 화면: 네이버 지도 + 상단 검색창 + 상세 카드
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 🗺️ 네이버 지도 컴포넌트 (가장 아래에 깔림)
            NaverMap(modifier = Modifier.fillMaxSize())

            // 🔍 상단 검색바 및 추천 검색어
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.searchQuery.value = it
                        viewModel.isSearchActive.value = it.isNotEmpty()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // 추천 검색어 드롭다운
                AnimatedVisibility(visible = isSearchActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            viewModel.recommendedPlaces.filter { it.name.contains(searchQuery) }.forEach { place ->

                                // 🚀 에러 픽스: 검색 추천어 클릭 시 튕김 방지를 위한 InteractionSource 설정
                                val interactionSource = remember { MutableInteractionSource() }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = LocalIndication.current,
                                            onClick = {
                                                viewModel.selectedPlace.value = place // 카드 띄우기
                                                viewModel.isSearchActive.value = false
                                            }
                                        )
                                        .background(Color.White)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, tint = Color.Gray, contentDescription = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(place.name, fontSize = 16.sp)
                                }
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                            }
                        }
                    }
                }
            }

            // 💳 선택된 장소 상세 정보 카드 (바텀시트 바로 위쪽에 플로팅)
            AnimatedVisibility(
                visible = selectedPlace != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp) // 바텀시트 위에 뜨도록 패딩 설정
            ) {
                selectedPlace?.let { place ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            // 썸네일 이미지
                            AsyncImage(
                                model = place.imageUrl,
                                contentDescription = "장소 이미지",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            // 정보 및 추가 버튼
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