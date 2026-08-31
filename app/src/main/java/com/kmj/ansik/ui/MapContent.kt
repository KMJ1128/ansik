package com.kmj.ansik.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.CircleOverlay
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.LocationOverlay
import com.naver.maps.map.compose.PathOverlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalNaverMapApi::class)
@Composable
internal fun NaverMapContent(
    viewModel: MainViewModel,
    cameraPositionState: CameraPositionState,
    scheduleListState: LazyListState,
    highlightedPlaceId: String?,
    onHighlightPlace: (String?) -> Unit,
    onShowDetail: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val restaurantMarker = rememberRestaurantMarker()

    LaunchedEffect(viewModel.selectedPlace.value) {
        viewModel.selectedPlace.value?.let { place ->
            val target = LatLng(place.latitude, place.longitude)
            cameraPositionState.animate(
                CameraUpdate.scrollTo(target).animate(CameraAnimation.Easing)
            )
        }
    }

    NaverMap(
        cameraPositionState = cameraPositionState,
        onSymbolClick = { symbol ->
            viewModel.selectLocationFromMap(
                name = symbol.caption,
                lat = symbol.position.latitude,
                lng = symbol.position.longitude
            )
            true
        },
        onMapClick = { _, _ -> viewModel.clearSelectedPlace() }
    ) {
        viewModel.currentUserLocation.value?.let { currentLocation ->
            CircleOverlay(
                center = currentLocation,
                radius = viewModel.searchRadius.intValue.toDouble(),
                color = Color(0x2234A853),
                outlineColor = Color(0xAA34A853),
                outlineWidth = 2.dp
            )
            LocationOverlay(
                position = currentLocation,
                circleColor = Color(0x333A86FF),
                circleOutlineColor = Color.White,
                circleOutlineWidth = 2.dp
            )
        }

        for (day in 1..viewModel.days.value) {
            val coords = viewModel.getRouteCoordsForDay(day)
            if (coords.size >= 2) {
                PathOverlay(
                    coords = coords,
                    width = 5.dp,
                    color = getDayColor(day),
                    outlineWidth = 1.dp,
                    outlineColor = Color.White
                )
            }
        }

        viewModel.selectedPlace.value?.let { place ->
            key("selected_${place.id}_${place.latitude}_${place.longitude}") {
                Marker(
                    state = MarkerState(
                        position = LatLng(place.latitude, place.longitude)
                    ),
                    icon = MarkerIcons.BLACK,
                    iconTintColor = getDayColor(place.day)
                )
            }
        }

        viewModel.travelRoute.forEach { place ->
            val dayIndex = viewModel.travelRoute
                .filter { it.day == place.day }
                .indexOfFirst { it.id == place.id } + 1

            val markerIcon = rememberNumberedMarker(
                number = dayIndex,
                composeColor = getDayColor(place.day)
            )

            key("route_${place.id}") {
                Marker(
                    state = MarkerState(
                        position = LatLng(place.latitude, place.longitude)
                    ),
                    icon = markerIcon,
                    onClick = {
                        coroutineScope.launch {
                            val freshIndex = viewModel.travelRoute
                                .indexOfFirst { it.id == place.id }

                            if (
                                freshIndex != -1 &&
                                freshIndex < scheduleListState.layoutInfo.totalItemsCount
                            ) {
                                scheduleListState.animateScrollToItem(freshIndex)
                            }

                            onHighlightPlace(place.id)
                            delay(800)
                            onHighlightPlace(null)
                        }
                        true
                    }
                )
            }
        }

        viewModel.nearbyRestaurants.forEach { restaurant ->
            val lat = restaurant.latitude
            val lng = restaurant.longitude

            if (lat != 0.0 && lng != 0.0) {
                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    icon = restaurantMarker,
                    captionText = restaurant.title,
                    onClick = {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdate.scrollTo(LatLng(lat, lng))
                                    .animate(CameraAnimation.Easing)
                            )
                        }
                        viewModel.fetchRestaurantDetail(restaurant)
                        onShowDetail()
                        true
                    }
                )
            }
        }
    }
}

@Composable
private fun rememberNumberedMarker(
    number: Int,
    composeColor: Color
): OverlayImage {
    return remember(number, composeColor) {
        val size = 90
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val fillPaint = Paint().apply {
            isAntiAlias = true
            color = composeColor.toArgb()
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, fillPaint)

        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 3f, borderPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val metrics = textPaint.fontMetrics
        canvas.drawText(
            number.toString(),
            size / 2f,
            (size / 2f) - (metrics.ascent + metrics.descent) / 2f,
            textPaint
        )

        OverlayImage.fromBitmap(bitmap)
    }
}

@Composable
private fun rememberRestaurantMarker(): OverlayImage {
    return remember {
        val size = 50
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            size / 2f,
            Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.rgb(25, 118, 210)
            }
        )
        canvas.drawCircle(
            size / 2f,
            size / 2f,
            (size / 2f) - 5f,
            Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.WHITE
            }
        )
        canvas.drawCircle(
            size / 2f,
            size / 2f,
            10f,
            Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.rgb(25, 118, 210)
            }
        )

        OverlayImage.fromBitmap(bitmap)
    }
}
