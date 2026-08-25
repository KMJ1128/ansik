package com.kmj.ansik.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kmj.ansik.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun BoxScope.TopSearchLayout(
    viewModel: MainViewModel,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?
) {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 20.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = viewModel.searchQuery.value,
            onValueChange = viewModel::searchPlacesRealtime,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search_placeholder),
                    color = Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = AppColors.Success
                )
            },
            trailingIcon = {
                if (viewModel.searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearSearch) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.clear),
                            tint = Color.Gray
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(50),
                    spotColor = Color(0x26000000)
                ),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.98f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )

        AnimatedVisibility(
            visible = viewModel.isSearchActive.value &&
                viewModel.recommendedPlaces.isNotEmpty()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    items(
                        items = viewModel.recommendedPlaces,
                        key = { "${it.id}_${it.latitude}_${it.longitude}" }
                    ) { place ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectLocationFromMap(
                                        place.name,
                                        place.latitude,
                                        place.longitude
                                    )
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFF5F5F5), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = Color(0xFFBDBDBD),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = place.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = place.tag,
                                        fontSize = 12.sp,
                                        color = AppColors.Success
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = place.address,
                                    fontSize = 13.sp,
                                    color = Color(0xFF9E9E9E),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@Composable
internal fun BoxScope.ScheduleDrawer(
    viewModel: MainViewModel,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit,
    listState: LazyListState,
    highlightedPlaceId: String?
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 90.dp, end = 16.dp)
    ) {
        if (!isExpanded) {
            Card(
                modifier = Modifier.clickable { onToggleExpand(true) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = null,
                        tint = AppColors.Success,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.my_schedule_short),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = AppColors.SuccessSoft,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${viewModel.travelRoute.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Success,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .width(330.dp)
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.97f)
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.my_travel_schedule),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = viewModel::decreaseDays,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFF5F5F5), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(
                                        id = R.string.nights_days,
                                        viewModel.nights.value,
                                        viewModel.days.value
                                    ),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Success
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                IconButton(
                                    onClick = viewModel::increaseDays,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFF5F5F5), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { onToggleExpand(false) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
                        }
                    }

                    HorizontalDivider()

                    val reorderableState = rememberReorderableLazyListState(listState) {
                            from,
                            to ->
                        viewModel.movePlace(from.index, to.index)
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = viewModel.travelRoute.toList(),
                            key = { it.id }
                        ) { place ->
                            ReorderableItem(
                                reorderableState,
                                key = place.id
                            ) { isDragging ->
                                val isHighlighted = place.id == highlightedPlaceId

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(
                                            elevation = if (isDragging || isHighlighted) 10.dp else 2.dp,
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isHighlighted) {
                                            Color(0xFFFFF9C4)
                                        } else {
                                            Color.White
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Menu,
                                                contentDescription = null,
                                                tint = Color.LightGray,
                                                modifier = Modifier
                                                    .draggableHandle()
                                                    .size(24.dp)
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            AsyncImage(
                                                model = place.imageUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = place.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = place.tag,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.removePlace(place) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(id = R.string.delete),
                                                    tint = Color(0xFFFF8A80)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(count = viewModel.days.value) { index ->
                                                val day = index + 1
                                                val isSelected = place.day == day

                                                Surface(
                                                    modifier = Modifier.clickable {
                                                        viewModel.changePlaceDay(place, day)
                                                    },
                                                    shape = RoundedCornerShape(50),
                                                    color = if (isSelected) {
                                                        getDayColor(day)
                                                    } else {
                                                        Color(0xFFF5F5F5)
                                                    }
                                                ) {
                                                    Text(
                                                        text = stringResource(
                                                            id = R.string.day_format,
                                                            day
                                                        ),
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) {
                                                            FontWeight.Bold
                                                        } else {
                                                            FontWeight.Medium
                                                        },
                                                        color = if (isSelected) {
                                                            Color.White
                                                        } else {
                                                            Color.Gray
                                                        },
                                                        modifier = Modifier.padding(
                                                            horizontal = 10.dp,
                                                            vertical = 5.dp
                                                        )
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
            }
        }
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
internal fun BoxScope.BottomCards(
    viewModel: MainViewModel,
    cameraPositionState: CameraPositionState,
    onShowRadiusDialog: () -> Unit,
    onShowDetailPopup: () -> Unit,
    onShowViewer: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = viewModel.selectedPlace.value != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        viewModel.selectedPlace.value?.let { place ->
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row {
                        AsyncImage(
                            model = place.imageUrl,
                            contentDescription = stringResource(id = R.string.place_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(82.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onShowViewer(
                                        place.imageUrls.ifEmpty {
                                            listOf(place.imageUrl)
                                        }
                                    )
                                }
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = place.name,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = place.address,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val url = "https://m.map.naver.com/search.naver?query=" +
                                    Uri.encode(place.name)
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.directions_roadview),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.addPlaceToRoute(place) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Success
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.add_schedule),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.searchNearbyRestaurants(
                                    place.latitude,
                                    place.longitude
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Info
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.find_nearby_safe_restaurants
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.fetchPlaceReviews(place.name, place.address)
                            },
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AppColors.Warning
                            )
                        }

                        OutlinedButton(
                            onClick = onShowRadiusDialog,
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = AppColors.Info
                            )
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = viewModel.selectedPlace.value == null &&
            (
                viewModel.isFetchingRestaurants.value ||
                    viewModel.hasSearchedRestaurants.value
                ),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.restaurant_list_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = viewModel::clearNearbyRestaurants,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (viewModel.isFetchingRestaurants.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Success)
                    }
                } else if (viewModel.nearbyRestaurants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_restaurants_found),
                            color = AppColors.TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = viewModel.nearbyRestaurants,
                            key = { it.id }
                        ) { restaurant ->
                            RestaurantHorizontalCard(
                                restaurant = restaurant,
                                onCardClick = {
                                    val lat = restaurant.latitude
                                    val lng = restaurant.longitude

                                    if (lat != 0.0 && lng != 0.0) {
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdate.scrollTo(LatLng(lat, lng))
                                                    .animate(CameraAnimation.Easing)
                                            )
                                        }
                                    }
                                },
                                onImageClick = {
                                    onShowViewer(
                                        restaurant.imageUrls.ifEmpty {
                                            listOf(
                                                restaurant.imageUrl.ifBlank {
                                                    RestaurantRepository.DEFAULT_IMAGE_URL
                                                }
                                            )
                                        }
                                    )
                                },
                                onReviewClick = {
                                    viewModel.fetchPlaceReviews(
                                        restaurant.title,
                                        restaurant.address
                                    )
                                },
                                onDetailClick = {
                                    viewModel.fetchRestaurantDetail(
                                        restaurant
                                    )
                                    onShowDetailPopup()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
