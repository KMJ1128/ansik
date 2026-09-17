package com.kmj.ansik.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kmj.ansik.R
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseMealCheckSheet(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val places = remember { viewModel.travelRoute.toList().filter {
        it.isRestaurant || it.tag in listOf("음식점", "식당", "카페", "Restaurant", "Restaurants", "餐厅", "レストラン")
    }.distinctBy { "${it.name}|${it.address}" }.sortedBy { it.day } }
    val conditions = viewModel.selectedConditions.value.sorted()
    val results = remember { mutableStateMapOf<String, RestaurantDetailState>() }
    val failed = remember { mutableStateMapOf<String, Boolean>() }
    var loading by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(attempt) {
        failed.clear()
        try {
            for (place in places) {
                if (results.containsKey(place.id)) continue
                loading = place.id
                try {
                    val detail = RestaurantRepository().getRestaurantDetail(
                        RestaurantSummary(id = place.id, title = place.name, address = place.address,
                            latitude = place.latitude, longitude = place.longitude,
                            tourContentId = place.tourContentId),
                        androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { "ko" },
                        conditions
                    )
                    if (detail.menuGuide?.menus.isNullOrEmpty()) failed[place.id] = true
                    else results[place.id] = detail
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { failed[place.id] = true }
            }
        } finally { loading = null }
    }
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.Background) {
        LazyColumn(Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text(stringResource(R.string.course_meal_check), style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(if (conditions.isEmpty()) stringResource(R.string.health_profile_needed)
                    else conditions.joinToString(" · "), color = AppColors.TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.health_analysis_disclaimer), style = MaterialTheme.typography.bodySmall)
            }
            if (places.isEmpty()) item { Text(stringResource(R.string.course_no_restaurants)) }
            items(places, key = { it.id }) { place ->
                var expanded by remember(place.id) { mutableStateOf(false) }
                Card(colors = CardDefaults.cardColors(containerColor = AppColors.Surface), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${place.day} · ${place.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(place.address, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                        val menus = results[place.id]?.menuGuide?.menus.orEmpty()
                        results[place.id]?.tourDetail?.firstmenu?.takeIf { it.isNotBlank() }?.let {
                            Text(stringResource(R.string.restaurant_main_menu) + ": " + it,
                                fontWeight = FontWeight.Medium)
                        }
                        if (menus.isNotEmpty() && conditions.isNotEmpty()) {
                            Text(stringResource(R.string.health_menu_analysis_counts,
                                menus.count { it.healthRiskLevel == "avoid" },
                                menus.count { it.healthRiskLevel == "caution" },
                                menus.count { it.healthRiskLevel == "safe" }),
                                style = MaterialTheme.typography.bodySmall)
                        }
                        if (menus.isEmpty() && failed[place.id] != true) {
                            if (loading == place.id) LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text(stringResource(if (loading == place.id) R.string.loading_restaurant_details else R.string.course_analysis_waiting))
                        }
                        if (failed[place.id] == true) Text(stringResource(R.string.health_analysis_unavailable))
                        (if (expanded) menus else menus.take(2)).forEach { menu ->
                            HorizontalDivider(color = AppColors.Divider)
                            val translatedName = menu.displayName.takeIf { !it.isNullOrBlank() } ?: menu.name
                            Text(translatedName, fontWeight = FontWeight.Bold)
                            if (viewModel.showOriginalMenuNames.value && translatedName != menu.name) {
                                Text(menu.name, style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.TextSecondary)
                            }
                            Text(stringResource(R.string.menu_evidence_label),
                                style = MaterialTheme.typography.labelMedium, color = AppColors.InfoDark)
                            Text(stringResource(R.string.menu_evidence_detail),
                                style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                            val level = if (conditions.isEmpty()) "unknown" else menu.healthRiskLevel.lowercase()
                            val label = when (level) {
                                "avoid", "high", "red" -> R.string.health_risk_avoid
                                "caution", "medium", "yellow" -> R.string.health_risk_caution
                                "safe", "low", "green" -> R.string.health_risk_safe
                                else -> R.string.health_risk_unknown
                            }
                            val tint = when (label) {
                                R.string.health_risk_avoid -> AppColors.Danger
                                R.string.health_risk_caution -> AppColors.TextPrimary
                                R.string.health_risk_safe -> AppColors.InfoDark
                                else -> AppColors.TextSecondary
                            }
                            Surface(color = tint.copy(alpha = 0.10f), shape = RoundedCornerShape(10.dp)) {
                                Text(stringResource(label), color = tint, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                            if (menu.typicalIngredients.isNotEmpty()) Text(menu.typicalIngredients.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                            if (menu.healthRiskSummary.isNotBlank()) Text(menu.healthRiskSummary)
                            menu.healthRiskReasons.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                            if (menu.questionsForRestaurant.isNotEmpty()) {
                                Text(stringResource(R.string.questions_for_restaurant), fontWeight = FontWeight.Medium)
                                menu.questionsForRestaurant.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                        if (menus.size > 2) TextButton(onClick = { expanded = !expanded }) {
                            Text(stringResource(if (expanded) R.string.close else R.string.restaurant_all_menus))
                        }
                    }
                }
            }
            if (failed.isNotEmpty() && loading == null) item {
                TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.course_analysis_retry)) }
            }
        }
    }
}
