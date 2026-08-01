package com.kmj.ansik.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================================
// 데이터 소스
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
    val handleToggle = remember { { condition: String -> viewModel.toggleCondition(condition) } }

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
                            onToggle = handleToggle
                        )
                    }
                }
            }

            item {
                ExpandableCategorySection(
                    title = "⚠️ 알레르기 유발 물질",
                    items = allergyItems,
                    selectedItems = selectedConditions,
                    onToggle = handleToggle
                )
            }

            item {
                ExpandableCategorySection(
                    title = "🥗 헬스 및 다이어트 식단",
                    items = dietItems,
                    selectedItems = selectedConditions,
                    onToggle = handleToggle
                )
            }

            item {
                ExpandableCategorySection(
                    title = "🙏 종교 및 신념 기반 식단",
                    items = religionItems,
                    selectedItems = selectedConditions,
                    onToggle = handleToggle
                )
            }

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
// 컴포넌트 1: 렌더링을 극대화한 가벼운 칩
// =========================================================================
@Composable
fun AnimatedChip(
    label: String,
    isSelected: Boolean,
    onToggle: (String) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2E7D32) else Color.White,
        animationSpec = tween(durationMillis = 200), label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.DarkGray,
        animationSpec = tween(durationMillis = 200), label = "textColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 150), label = "scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(20.dp))
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onToggle(label) }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// =========================================================================
// 컴포넌트 2: 마법의 최적화가 적용된 아코디언 메뉴
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
    val customItems = remember { mutableStateListOf<String>() }
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = { isExpanded = !isExpanded }
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing))
                // 🚀 내부 패딩을 16dp -> 20dp로 살짝 넓혀 텍스트 잘림을 예방
                .padding(20.dp)
        ) {
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

            if (isExpanded) {
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
                                onToggle = onToggle
                            )
                        }
                        customItems.forEach { customItem ->
                            AnimatedChip(
                                label = customItem,
                                isSelected = selectedItems.contains(customItem),
                                onToggle = onToggle
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomInputRow(
                        onAddCustomItem = { newCustom ->
                            if (!customItems.contains(newCustom)) {
                                customItems.add(newCustom)
                                onToggle(newCustom)
                            }
                        }
                    )
                }
            }
        }
    }
}

// =========================================================================
// 컴포넌트 3: 기타 직접 입력 전용 영역
// =========================================================================
@Composable
fun CustomInputRow(onAddCustomItem: (String) -> Unit) {
    var customInput by remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = customInput,
            onValueChange = { customInput = it },
            placeholder = { Text("기타 직접 입력", fontSize = 14.sp) },
            modifier = Modifier
                .weight(1f)
                // 🚀 [수정]: 50.dp -> 56.dp (Material 3 표준 높이)로 변경하여 글자 잘림 완벽 해결!
                .height(56.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2E7D32),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (customInput.isNotBlank()) {
                    onAddCustomItem(customInput)
                    customInput = ""
                }
            },
            modifier = Modifier
                // 🚀 [수정]: 버튼 높이도 텍스트 필드에 맞춰 56.dp로 동일하게 변경
                .size(56.dp)
                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.Add, contentDescription = "추가", tint = Color(0xFF2E7D32))
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