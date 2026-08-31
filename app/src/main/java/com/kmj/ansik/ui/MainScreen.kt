package com.kmj.ansik.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kmj.ansik.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalNaverMapApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val cameraPositionState =
        rememberCameraPositionState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var locationPermissionGranted by remember {
        mutableStateOf(hasLocationPermission(context))
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    DisposableEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) return@DisposableEffect onDispose { }
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationListener { location ->
            viewModel.updateCurrentLocation(location.latitude, location.longitude)
        }
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        providers.forEach { provider ->
            if (locationManager.isProviderEnabled(provider)) {
                locationManager.getLastKnownLocation(provider)?.let { location ->
                    viewModel.updateCurrentLocation(location.latitude, location.longitude)
                }
                locationManager.requestLocationUpdates(provider, 2_000L, 5f, listener)
            }
        }
        onDispose { locationManager.removeUpdates(listener) }
    }

    val focusManager =
        LocalFocusManager.current

    val keyboardController =
        LocalSoftwareKeyboardController.current

    val scheduleListState =
        rememberLazyListState()

    var isScheduleExpanded by remember {
        mutableStateOf(false)
    }

    var highlightedPlaceId by remember {
        mutableStateOf<String?>(null)
    }

    var showRadiusDialog by remember {
        mutableStateOf(false)
    }

    var searchCurrentLocationOnRadiusConfirm by remember {
        mutableStateOf(false)
    }

    var showDetailPopup by remember {
        mutableStateOf(false)
    }

    var viewerImages by remember {
        mutableStateOf<List<String>?>(null)
    }

    AppDialogs(
        viewModel = viewModel,
        viewerImages =
            viewerImages,
        onDismissViewer = {
            viewerImages = null
        },
        showRadiusDialog =
            showRadiusDialog,
        onDismissRadiusDialog = {
            showRadiusDialog =
                false
        },
        onConfirmRadius = {
            if (searchCurrentLocationOnRadiusConfirm) {
                viewModel.searchRestaurantsFromCurrentLocation()
            }
            searchCurrentLocationOnRadiusConfirm = false
        },
        showDetailPopup =
            showDetailPopup,
        onDismissDetailPopup = {
            showDetailPopup =
                false
        }
    )

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        NaverMapContent(
            viewModel =
                viewModel,
            cameraPositionState =
                cameraPositionState,
            scheduleListState =
                scheduleListState,
            highlightedPlaceId =
                highlightedPlaceId,
            onHighlightPlace = {
                highlightedPlaceId =
                    it
            },
            onShowDetail = {
                showDetailPopup =
                    true
            }
        )

        TopSearchLayout(
            viewModel =
                viewModel,
            focusManager =
                focusManager,
            keyboardController =
                keyboardController
        )

        ScheduleDrawer(
            viewModel =
                viewModel,
            isExpanded =
                isScheduleExpanded,
            onToggleExpand = {
                isScheduleExpanded =
                    it
            },
            listState =
                scheduleListState,
            highlightedPlaceId =
                highlightedPlaceId
        )

        BottomCards(
            viewModel =
                viewModel,
            cameraPositionState =
                cameraPositionState,
            onShowRadiusDialog = {
                searchCurrentLocationOnRadiusConfirm = false
                showRadiusDialog =
                    true
            },
            onShowDetailPopup = {
                showDetailPopup =
                    true
            },
            onShowViewer = {
                viewerImages =
                    it
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = {
                    if (!locationPermissionGranted) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        viewModel.currentUserLocation.value?.let { location ->
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdate.scrollAndZoomTo(
                                        LatLng(location.latitude, location.longitude),
                                        15.5
                                    ).animate(CameraAnimation.Easing)
                                )
                            }
                        } ?: Toast.makeText(
                            context,
                            context.getString(R.string.getting_current_location),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.size(50.dp),
                containerColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.my_location),
                    tint = AppColors.Info
                )
            }

            FloatingActionButton(
                onClick = {
                    if (!locationPermissionGranted) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else if (viewModel.currentUserLocation.value != null) {
                        searchCurrentLocationOnRadiusConfirm = true
                        showRadiusDialog = true
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.getting_current_location),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.size(50.dp),
                containerColor = AppColors.Success
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = stringResource(R.string.find_restaurants_from_my_location),
                    tint = Color.White
                )
            }
        }
    }

    ReviewBottomSheet(
        viewModel = viewModel
    )
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
