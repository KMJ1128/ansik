package com.kmj.ansik.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        ProfileHeader(selectedCount = selectedConditions.size)
        HorizontalDivider(color = AppColors.Divider)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                SectionLabel("기본 질환 관리")
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    icon = "⚠️",
                    title = "알레르기 유발 물질",
                    items = allergyItems,
                    selectedItems = selectedConditions,
                    onToggle = handleToggle
                )
            }

            item {
                ExpandableCategorySection(
                    icon = "🥗",
                    title = "헬스 및 다이어트 식단",
                    items = dietItems,
                    selectedItems = selectedConditions,
                    onToggle = handleToggle
                )
            }

            item {
                ExpandableCategorySection(
                    icon = "🙏",
                    title = "종교 및 신념 기반 식단",
                    items = religionItems,
                    selectedItems = selectedConditions,
                    onToggle = handleToggle
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                GradientCtaButton(
                    text = "여행 동선 짜러 가기",
                    onClick = onNavigateToMap
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// =========================================================================
// 상단 헤더 (플랫)
// =========================================================================
@Composable
fun ProfileHeader(selectedCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.PrimaryDark),
                contentAlignment = Alignment.Center
            ) {
                Text("🌿", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("안식 An-Sik", color = AppColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("나에게 딱 맞는 여행 식단 가이드", color = AppColors.TextSecondary, fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.PrimarySoft)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AppColors.PrimaryDark)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (selectedCount == 0) "아래에서 해당하는 항목을 선택해 주세요"
                else "$selectedCount 개 항목 선택됨 · 맞춤 가이드 준비 완료",
                color = AppColors.PrimaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
}

// =========================================================================
// 컴포넌트: CTA 버튼 (플랫)
// =========================================================================
@Composable
fun GradientCtaButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.PrimaryDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

// =========================================================================
// 컴포넌트 1: 모던 필 스타일 칩
// =========================================================================
@Composable
fun AnimatedChip(
    label: String,
    isSelected: Boolean,
    onToggle: (String) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.PrimaryDark else Color.White,
        animationSpec = tween(durationMillis = 200), label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else AppColors.TextPrimary,
        animationSpec = tween(durationMillis = 200), label = "textColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = tween(durationMillis = 150), label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .scale(scale)
            .shadow(if (isSelected) 6.dp else 0.dp, RoundedCornerShape(20.dp), spotColor = AppColors.Primary)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else AppColors.Divider,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onToggle(label) }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        AnimatedVisibility(visible = isSelected) {
            Row {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// =========================================================================
// 컴포넌트 2: 카드형 아코디언 메뉴
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableCategorySection(
    icon: String,
    title: String,
    items: List<String>,
    selectedItems: Set<String>,
    onToggle: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val customItems = remember { mutableStateListOf<String>() }
    val interactionSource = remember { MutableInteractionSource() }
    val selectedCountInSection = items.count { selectedItems.contains(it) } + customItems.count { selectedItems.contains(it) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220), label = "chevron"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = { isExpanded = !isExpanded }
            )
            .animateContentSize(animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.SurfaceMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                    if (selectedCountInSection > 0) {
                        Text("$selectedCountInSection 개 선택", fontSize = 11.sp, color = AppColors.Primary)
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "확장",
                tint = AppColors.TextSecondary,
                modifier = Modifier.rotate(chevronRotation)
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
            placeholder = { Text("기타 직접 입력", fontSize = 14.sp, color = AppColors.TextSecondary) },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Divider,
                focusedContainerColor = AppColors.SurfaceMuted,
                unfocusedContainerColor = AppColors.SurfaceMuted
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.PrimaryDark)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (customInput.isNotBlank()) {
                        onAddCustomItem(customInput)
                        customInput = ""
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "추가", tint = Color.White)
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