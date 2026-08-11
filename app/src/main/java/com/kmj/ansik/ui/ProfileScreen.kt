package com.kmj.ansik.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmj.ansik.R

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit // 톱니바퀴 클릭 시 이동할 콜백 추가
) {
    val selectedConditions by viewModel.selectedConditions

    val handleToggle = remember {
        { condition: String ->
            viewModel.toggleCondition(condition)
        }
    }

    // strings.xml에서 다국어 배열 리소스 불러오기
    val basicConditions = stringArrayResource(id = R.array.basic_conditions).toList()
    val allergyItems = stringArrayResource(id = R.array.allergy_items).toList()
    val dietItems = stringArrayResource(id = R.array.diet_items).toList()
    val religionItems = stringArrayResource(id = R.array.religion_items).toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_title),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                },
                actions = {
                    // 상단 우측에 설정(언어 변경 등) 아이콘 추가
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.settings), // strings.xml에 추가 필요
                            tint = Color(0xFF1B5E20)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F5E9)
                )
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

                Text(
                    text = stringResource(id = R.string.health_management_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.health_management_desc),
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.basic_disease_care),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2E7D32)
                )

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
                    title = stringResource(id = R.string.allergy_triggers),
                    items = allergyItems,
                    selectedItems = selectedConditions,
                    onToggle = handleToggle
                )
            }

            item {
                ExpandableCategorySection(
                    title = stringResource(id = R.string.health_diet),
                    items = dietItems,
                    selectedItems = selectedConditions,
                    onToggle = handleToggle
                )
            }

            item {
                ExpandableCategorySection(
                    title = stringResource(id = R.string.religion_belief_diet),
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.go_to_travel_route),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun AnimatedChip(
    label: String,
    isSelected: Boolean,
    onToggle: (String) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2E7D32) else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.DarkGray,
        animationSpec = tween(durationMillis = 200),
        label = "textColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .shadow(
                if (isSelected) 4.dp else 0.dp,
                RoundedCornerShape(20.dp)
            )
            .background(
                backgroundColor,
                RoundedCornerShape(20.dp)
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onToggle(label)
            }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isExpanded = !isExpanded
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9F9F9)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = LinearOutSlowInEasing
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(id = R.string.expand),
                    tint = Color.Gray
                )
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
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

@Composable
fun CustomInputRow(
    onAddCustomItem: (String) -> Unit
) {
    var customInput by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = customInput,
            onValueChange = { customInput = it },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.enter_custom_other),
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .weight(1f)
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
                    onAddCustomItem(customInput.trim())
                    customInput = ""
                }
            },
            modifier = Modifier
                .size(56.dp)
                .background(
                    Color(0xFFE8F5E9),
                    RoundedCornerShape(8.dp)
                )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(id = R.string.add),
                tint = Color(0xFF2E7D32)
            )
        }
    }
}