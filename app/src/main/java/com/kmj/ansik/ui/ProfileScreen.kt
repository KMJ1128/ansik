package com.kmj.ansik.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================================
// 데이터 소스 (하드코딩 방지를 위한 리스트화)
// =========================================================================
val basicConditions = listOf(
    "당뇨 관리 (저당)", "고혈압 (저나트륨)", "통풍 (저퓨린)", "고지혈증 (저콜레스테롤)",
    "만성 신부전 (저칼륨)", "위장 장애 (저자극)", "심혈관 질환", "비만/체중 관리", "임산부/수유부"
)

val allergyItems = listOf(
    "갑각류", "견과류", "우유/유제품", "대두(콩)", "밀(글루텐)",
    "계란", "생선", "조개류", "복숭아", "토마토", "돼지고기", "아황산류(와인 등)"
)

val dietItems = listOf(
    "비건 (엄격한 채식)", "락토오보 (우유/계란 허용)", "페스카테리안 (해산물 허용)",
    "키토제닉 (저탄고지)", "고단백/벌크업", "무글루텐 (Gluten-Free)",
    "간헐적 단식", "저지방 식단", "지중해식 식단", "당질 제한식"
)

val religionItems = listOf(
    "할랄 (이슬람교)", "코셔 (유대교)", "소고기 금지 (힌두교)", "돼지고기 금지",
    "오신채 금지 (불교)", "육식 금지 (제칠일안식일예수재림교)", "뿌리채소 금지 (자이나교)",
    "유기농 선호", "동물권 보호식", "공정무역 식재료"
)

// =========================================================================
// 메인 화면
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel, onNavigateToMap: () -> Unit) {
    val selectedConditions by viewModel.selectedConditions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("안식 (An-Sik)", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE8F5E9))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("🩺 어떤 건강 관리가 필요하신가요?", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("다중 선택이 가능합니다. 맞춤형 🔴🟡🟢 가이드를 제공해 드려요.", color = Color.Gray, fontSize = 13.sp)
            }

            // 1. 기본 만성 질환 섹션
            item {
                Text("기본 질환 관리", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    basicConditions.forEach { condition ->
                        AnimatedChip(
                            label = condition,
                            isSelected = selectedConditions.contains(condition),
                            onClick = { viewModel.toggleCondition(condition) }
                        )
                    }
                }
            }

            // 2. 알레르기 섹션
            item {
                ExpandableCategorySection(
                    title = "⚠️ 알레르기 유발 물질",
                    items = allergyItems,
                    selectedItems = selectedConditions,
                    onToggle = { viewModel.toggleCondition(it) }
                )
            }

            // 3. 헬스/식단 섹션
            item {
                ExpandableCategorySection(
                    title = "🥗 헬스 및 다이어트 식단",
                    items = dietItems,
                    selectedItems = selectedConditions,
                    onToggle = { viewModel.toggleCondition(it) }
                )
            }

            // 4. 종교/신념 섹션
            item {
                ExpandableCategorySection(
                    title = "🙏 종교 및 신념 기반 식단",
                    items = religionItems,
                    selectedItems = selectedConditions,
                    onToggle = { viewModel.toggleCondition(it) }
                )
            }

            // 하단 여백 및 제출 버튼
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("여행 동선 짜러 가기 🗺️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// =========================================================================
// 컴포넌트 1: 모던 애니메이션 칩 (Active State 적용)
// =========================================================================
@Composable
fun AnimatedChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2E7D32) else Color.White,
        animationSpec = tween(durationMillis = 300), label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.DarkGray,
        animationSpec = tween(durationMillis = 300), label = "textColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 여기는 애니메이션을 직접 주므로 기본 물결 끄기 (안전함)
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Text(
            text = label,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// =========================================================================
// 컴포넌트 2: 확장형(아코디언) 카테고리 + 직접 입력 뷰
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableCategorySection(
    title: String,
    items: List<String>,
    selectedItems: Set<String>,
    onToggle: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }
    val customItems = remember { mutableStateListOf<String>() }

    // 🚀 에러 픽스 핵심 부분: Compose 1.7.0 크래시 방지용 InteractionSource 설정
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current, // 🚀 구글 권장 방식 명시적 선언
                onClick = { isExpanded = !isExpanded }
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "확장",
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items.forEach { item ->
                            AnimatedChip(
                                label = item,
                                isSelected = selectedItems.contains(item),
                                onClick = { onToggle(item) }
                            )
                        }
                        customItems.forEach { customItem ->
                            AnimatedChip(
                                label = customItem,
                                isSelected = selectedItems.contains(customItem),
                                onClick = { onToggle(customItem) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customInput,
                            onValueChange = { customInput = it },
                            placeholder = { Text("기타 직접 입력", fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2E7D32),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (customInput.isNotBlank() && !customItems.contains(customInput)) {
                                    customItems.add(customInput)
                                    onToggle(customInput)
                                    customInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "추가", tint = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 미리보기
// =========================================================================
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        viewModel = MainViewModel(),
        onNavigateToMap = {}
    )
}