package com.kmj.ansik.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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

@Composable
internal fun RestaurantHorizontalCard(
    restaurant: RestaurantSummary,
    onCardClick: () -> Unit,
    onImageClick: () -> Unit,
    onReviewClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    val secureImageUrl = restaurant.imageUrl
        .replace("http://", "https://")
        .ifBlank { RestaurantRepository.DEFAULT_IMAGE_URL }

    Card(
        modifier = Modifier
            .width(230.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = secureImageUrl,
                contentDescription = stringResource(id = R.string.place_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onImageClick),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = restaurant.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = restaurant.address,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            RestaurantSourceRow(
                sources = restaurant.sources,
                distanceMeters = restaurant.distanceMeters,
                koreanFallback = restaurant.koreanFallback
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onReviewClick),
                    color = Color(0xFFFFF9C4),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.review_view),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onDetailClick),
                    color = AppColors.SuccessSoft,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.detail_view),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Success,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun AppDialogs(
    viewModel: MainViewModel,
    viewerImages: List<String>?,
    onDismissViewer: () -> Unit,
    showRadiusDialog: Boolean,
    onDismissRadiusDialog: () -> Unit,
    onConfirmRadius: () -> Unit,
    showDetailPopup: Boolean,
    onDismissDetailPopup: () -> Unit
) {
    viewerImages?.let {
        FullScreenImageViewer(
            imageUrls = it,
            onDismiss = onDismissViewer
        )
    }

    if (showRadiusDialog) {
        AlertDialog(
            onDismissRequest = onDismissRadiusDialog,
            title = {
                Text(
                    text = stringResource(id = R.string.radius_settings_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    var tempRadius by remember {
                        mutableFloatStateOf(viewModel.searchRadius.intValue.toFloat())
                    }
                    val snappedRadius = (tempRadius / 100).toInt() * 100
                    val radiusText = if (snappedRadius >= 1000) {
                        if (snappedRadius % 1000 == 0) {
                            "${snappedRadius / 1000}km"
                        } else {
                            String.format("%.1fkm", snappedRadius / 1000f)
                        }
                    } else {
                        "${snappedRadius}m"
                    }

                    Text(
                        text = radiusText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Success,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = tempRadius,
                        onValueChange = { tempRadius = it },
                        onValueChangeFinished = {
                            viewModel.updateSearchRadius(snappedRadius)
                        },
                        valueRange = 100f..3000f,
                        colors = SliderDefaults.colors(
                            thumbColor = AppColors.Success,
                            activeTrackColor = AppColors.Success,
                            inactiveTrackColor = AppColors.SuccessSoft
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmRadius()
                        onDismissRadiusDialog()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm),
                        color = AppColors.Success,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White
        )
    }

    if (
        showDetailPopup &&
        viewModel.isFetchingRestaurantDetail.value &&
        viewModel.selectedRestaurantDetail.value == null
    ) {
        AlertDialog(
            onDismissRequest = onDismissDetailPopup,
            confirmButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = stringResource(id = R.string.loading_restaurant_details),
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Success
                )
            },
            text = {
                LoadingSkeleton(
                    label = stringResource(id = R.string.loading_restaurant_details),
                    rows = 4
                )
            }
        )
    }

    if (showDetailPopup && viewModel.selectedRestaurantDetail.value != null) {
        val detailState = viewModel.selectedRestaurantDetail.value!!
        val detail = detailState.tourDetail
        val researchedMenus = detailState.menuGuide?.menus.orEmpty()
        val menuNames = remember(detail?.firstmenu, detail?.treatmenu, researchedMenus) {
            researchedMenus
                .map { it.name }
                .filter { it.isNotBlank() }
                .ifEmpty { extractMenuNames(detail?.firstmenu, detail?.treatmenu) }
        }

        AlertDialog(
            onDismissRequest = onDismissDetailPopup,
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Column {
                    Text(
                        text = detailState.restaurant.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.Success
                    )
                    if (detailState.restaurant.address.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = detailState.restaurant.address,
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 430.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        RestaurantSourceRow(
                            sources = detailState.restaurant.sources,
                            distanceMeters = detailState.restaurant.distanceMeters,
                            koreanFallback = detailState.restaurant.koreanFallback
                        )
                    }

                    if (menuNames.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.restaurant_all_menus),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Success
                            )
                            Text(
                                text = stringResource(id = R.string.menu_tap_for_details),
                                fontSize = 11.sp,
                                color = AppColors.TextSecondary
                            )
                            if (researchedMenus.isNotEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.menu_guide_ai_notice),
                                    fontSize = 10.sp,
                                    color = AppColors.TextSecondary,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }

                        items(menuNames, key = { it }) { menuName ->
                            val selectedDetail = viewModel.selectedMenuDetail.value
                            val isSelected = selectedDetail?.menuName == menuName
                            val researchedMenu = researchedMenus.firstOrNull { it.name == menuName }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (researchedMenu != null) {
                                            viewModel.showResearchedMenuDetail(researchedMenu)
                                        } else {
                                            viewModel.fetchMenuDetail(menuName)
                                        }
                                    },
                                color = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF7F7F7),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF81C784) else Color(0xFFE0E0E0)
                                )
                            ) {
                                Text(
                                    text = menuName,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isSelected) {
                                if (viewModel.isFetchingMenuDetail.value) {
                                    LoadingSkeleton(
                                        label = stringResource(id = R.string.loading_menu_details),
                                        rows = 3
                                    )
                                } else {
                                    MenuDetailCard(
                                        menuName = menuName,
                                        profile = selectedDetail?.profile,
                                        imageUrls = selectedDetail?.imageUrls.orEmpty()
                                    )
                                }
                            }
                        }
                    }

                    if (!detailState.hasMenuEvidence) {
                        item {
                            Surface(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.no_menu_evidence),
                                    modifier = Modifier.padding(14.dp),
                                    fontSize = 13.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }

                    detail?.opentimefood
                        ?.takeIf { it.isNotBlank() }
                        ?.let { value ->
                            item {
                                RestaurantInfoRow(
                                    title = stringResource(id = R.string.restaurant_open_time),
                                    value = value
                                )
                            }
                        }

                    detail?.packing
                        ?.takeIf { it.isNotBlank() }
                        ?.let { value ->
                            item {
                                RestaurantInfoRow(
                                    title = stringResource(id = R.string.restaurant_packing),
                                    value = value
                                )
                            }
                        }

                    detail?.parkingfood
                        ?.takeIf { it.isNotBlank() }
                        ?.let { value ->
                            item {
                                RestaurantInfoRow(
                                    title = stringResource(id = R.string.restaurant_parking),
                                    value = value
                                )
                            }
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissDetailPopup) {
                    Text(
                        text = stringResource(id = R.string.close),
                        color = AppColors.Success,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

}

@Composable
private fun MenuDetailCard(
    menuName: String,
    profile: MenuProfile?,
    imageUrls: List<String>
) {
    val uriHandler = LocalUriHandler.current
    val candidates = remember(imageUrls) {
        imageUrls.filter { it.isNotBlank() }.distinct()
    }
    var imageIndex by remember(candidates) { mutableIntStateOf(0) }
    val imageUrl = candidates.getOrNull(imageIndex)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FBF7)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(id = R.string.menu_details),
                fontSize = 11.sp,
                color = AppColors.Success,
                fontWeight = FontWeight.Bold
            )
            Text(text = menuName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)

            imageUrl?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = it,
                    contentDescription = stringResource(id = R.string.menu_reference_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    onError = {
                        if (imageIndex < candidates.lastIndex) imageIndex += 1
                    }
                )
                Text(
                    text = stringResource(id = R.string.menu_reference_photo),
                    fontSize = 10.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!profile?.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = profile!!.description, fontSize = 14.sp, lineHeight = 20.sp)
                if (profile.descriptionSource.startsWith("NAVER") ||
                    profile.descriptionSource.startsWith("OPENAI")) {
                    val sourceLabel = if (profile.descriptionSource.startsWith("OPENAI")) {
                        stringResource(id = R.string.menu_description_source_openai)
                    } else {
                        stringResource(id = R.string.menu_description_source_naver)
                    }
                    Text(
                        text = sourceLabel,
                        fontSize = 10.sp,
                        color = AppColors.Success,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .then(
                                if (profile.descriptionSourceUrl.isNotBlank()) {
                                    Modifier.clickable {
                                        uriHandler.openUri(profile.descriptionSourceUrl)
                                    }
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.menu_knowledge_unavailable),
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )
            }

            profile?.tasteTags?.takeIf { it.isNotEmpty() }?.let {
                MenuTextSection(stringResource(id = R.string.menu_taste), it.joinToString(" · "))
            }
            profile?.typicalIngredients?.takeIf { it.isNotEmpty() }?.let {
                MenuTextSection(stringResource(id = R.string.menu_typical_ingredients), it.joinToString(" · "))
            }
            profile?.possibleAllergens?.takeIf { it.isNotEmpty() }?.let {
                MenuTextSection(stringResource(id = R.string.menu_possible_allergens), it.joinToString(" · "))
            }

            profile?.nutrition?.let { nutrition ->
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFDDEBDD))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(id = R.string.menu_nutrition_reference),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Success
                )
                if (nutrition.basisAmount.isNotBlank()) {
                    Text(
                        text = stringResource(id = R.string.menu_nutrition_basis, nutrition.basisAmount),
                        fontSize = 10.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Text(
                    text = listOfNotNull(
                        nutrition.energyKcal?.let { stringResource(R.string.menu_energy_value, it) },
                        nutrition.carbohydrateG?.let { stringResource(R.string.menu_carbohydrate_value, it) },
                        nutrition.proteinG?.let { stringResource(R.string.menu_protein_value, it) },
                        nutrition.fatG?.let { stringResource(R.string.menu_fat_value, it) },
                        nutrition.sugarG?.let { stringResource(R.string.menu_sugar_value, it) },
                        nutrition.sodiumMg?.let { stringResource(R.string.menu_sodium_value, it) }
                    ).joinToString("  ·  "),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = stringResource(id = R.string.menu_nutrition_source),
                    fontSize = 10.sp,
                    color = AppColors.TextSecondary
                )
            }

            val disclaimer = profile?.disclaimer?.takeIf { it.isNotBlank() }
                ?: stringResource(id = R.string.menu_general_disclaimer)
            disclaimer.let {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF795548)
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuTextSection(title: String, value: String) {
    Spacer(modifier = Modifier.height(10.dp))
    Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Success)
    Text(text = value, fontSize = 13.sp, lineHeight = 18.sp)
}

private fun extractMenuNames(firstMenu: String?, treatMenu: String?): List<String> {
    val normalized = listOfNotNull(firstMenu, treatMenu)
        .filter { it.isNotBlank() }
        .joinToString("\n")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), " ")
        .replace("&nbsp;", " ")
        .replace(Regex("\\d{1,3}(?:,\\d{3})+(?:원)?"), " ")

    return normalized
        .split(Regex("\\s*(?:\\r?\\n|,|/|·|ㆍ|•|\\||;)\\s*"))
        .map { value ->
            value
                .replace(Regex("^[\\-–—·ㆍ•]+\\s*"), "")
                .replace(Regex("\\s+\\d+(?:원)?$"), "")
                .trim()
        }
        .filter { it.length >= 2 && it.any(Char::isLetter) }
        .distinctBy { it.lowercase() }
}

@Composable
private fun RestaurantSourceRow(
    sources: List<String>,
    distanceMeters: Int,
    koreanFallback: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        sources.distinct().forEach { source ->
            val label = when (source) {
                "TOUR_API" -> stringResource(id = R.string.source_tour_api)
                "KAKAO" -> stringResource(id = R.string.source_kakao)
                else -> source
            }

            Surface(
                color = if (source == "TOUR_API") {
                    AppColors.SuccessSoft
                } else {
                    Color(0xFFFFF3E0)
                },
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (source == "TOUR_API") {
                        AppColors.Success
                    } else {
                        Color(0xFFEF6C00)
                    }
                )
            }
        }

        if (koreanFallback) {
            Surface(
                color = Color(0xFFECEFF1),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = stringResource(id = R.string.korean_original_data),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF546E7A)
                )
            }
        }

        if (distanceMeters > 0) {
            Text(
                text = formatDistance(distanceMeters),
                fontSize = 11.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

private fun formatDistance(distanceMeters: Int): String {
    return if (distanceMeters >= 1000) {
        String.format("%.1f km", distanceMeters / 1000f)
    } else {
        "${distanceMeters} m"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewBottomSheet(viewModel: MainViewModel) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
                ?: 0

            totalItems > 0 && lastVisibleItem >= totalItems - 1
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (
            shouldLoadMore &&
            viewModel.hasMoreReviews.value &&
            !viewModel.isFetchingReviews.value
        ) {
            viewModel.fetchPlaceReviews(
                viewModel.currentReviewPlaceName,
                viewModel.currentReviewAddress,
                isLoadMore = true
            )
        }
    }

    if (!viewModel.showReviewSheet.value) return

    ModalBottomSheet(
        onDismissRequest = viewModel::dismissReviewSheet,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.naver_blog_reviews),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                viewModel.isFetchingReviews.value &&
                    viewModel.selectedPlaceReviews.isEmpty() -> {
                    LoadingSkeleton(
                        label = stringResource(id = R.string.loading_public_data),
                        rows = 3
                    )
                }

                viewModel.selectedPlaceReviews.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_reviews),
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    val context = LocalContext.current

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        items(viewModel.selectedPlaceReviews) { review ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clickable {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(review.link)
                                            )
                                        )
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF9F9F9)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = review.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.Black
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = review.description,
                                        fontSize = 13.sp,
                                        color = Color.DarkGray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = stringResource(id = R.string.open_original),
                                        fontSize = 12.sp,
                                        color = AppColors.Success,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        if (viewModel.isFetchingReviews.value) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = AppColors.Success,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingSkeleton(
    label: String,
    rows: Int
) {
    val transition = rememberInfiniteTransition(label = "loading-skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading-skeleton-alpha"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.TextSecondary
        )
        repeat(rows) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == rows - 1) 0.68f else 1f)
                    .height(if (index == 0) 54.dp else 14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDDE5DF).copy(alpha = alpha))
            )
        }
    }
}

@Composable
internal fun FullScreenImageViewer(
    imageUrls: List<String>,
    onDismiss: () -> Unit
) {
    if (imageUrls.isEmpty()) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val pagerState = rememberPagerState(pageCount = { imageUrls.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = imageUrls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.close),
                    tint = Color.White
                )
            }

            Text(
                text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun RestaurantInfoRow(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Success
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF424242),
            maxLines = 4
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 6.dp),
            color = Color(0xFFEEEEEE)
        )
    }
}
