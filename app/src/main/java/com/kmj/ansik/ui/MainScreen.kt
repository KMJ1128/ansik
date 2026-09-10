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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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

    val restaurantListState =
        rememberLazyListState()

    var isScheduleExpanded by remember {
        mutableStateOf(false)
    }

    var highlightedPlaceId by remember {
        mutableStateOf<String?>(null)
    }

    var highlightedRestaurantId by remember {
        mutableStateOf<String?>(null)
    }

    var showRadiusDialog by remember {
        mutableStateOf(false)
    }

    var pendingMapCenterSearch by remember { mutableStateOf<LatLng?>(null) }

    var showDetailPopup by remember {
        mutableStateOf(false)
    }

    var viewerImages by remember {
        mutableStateOf<List<String>?>(null)
    }

    var showSaveMyCourseDialog by remember { mutableStateOf(false) }
    var myCourseTitle by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.appliedCourseVersion.intValue) {
        if (viewModel.appliedCourseVersion.intValue > 0) {
            isScheduleExpanded = true
            if (viewModel.travelRoute.isNotEmpty()) {
                val latitude = viewModel.travelRoute.map { it.latitude }.average()
                val longitude = viewModel.travelRoute.map { it.longitude }.average()
                cameraPositionState.animate(
                    CameraUpdate.scrollAndZoomTo(LatLng(latitude, longitude), 12.5)
                        .animate(CameraAnimation.Easing)
                )
            }
        }
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
            pendingMapCenterSearch?.let { center ->
                viewModel.searchNearbyRestaurants(center.latitude, center.longitude)
            }
            pendingMapCenterSearch = null
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
            restaurantListState =
                restaurantListState,
            highlightedPlaceId =
                highlightedPlaceId,
            highlightedRestaurantId =
                highlightedRestaurantId,
            onHighlightPlace = {
                highlightedPlaceId =
                    it
            },
            onHighlightRestaurant = {
                highlightedRestaurantId =
                    it
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
            onSaveMyCourse = {
                myCourseTitle = context.getString(
                    R.string.my_course_default_name,
                    viewModel.savedMyCourses.size + 1
                )
                showSaveMyCourseDialog = true
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
            restaurantListState =
                restaurantListState,
            highlightedRestaurantId =
                highlightedRestaurantId,
            onShowRadiusDialog = {
                pendingMapCenterSearch = cameraPositionState.position.target
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

        if (viewModel.isResolvingMapSelection.value) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AppColors.Success,
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = stringResource(id = R.string.loading_selected_place),
                        color = AppColors.TextPrimary
                    )
                }
            }
        }

        if (!viewModel.isSearchActive.value) Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    // The GPS location remains on the device. Restaurant lookup uses
                    // only the map center explicitly chosen by the user.
                    pendingMapCenterSearch = cameraPositionState.position.target
                    showRadiusDialog = true
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

    if (showSaveMyCourseDialog) {
        AlertDialog(
            onDismissRequest = { showSaveMyCourseDialog = false },
            title = { Text(stringResource(id = R.string.save_my_course)) },
            text = {
                OutlinedTextField(
                    value = myCourseTitle,
                    onValueChange = { myCourseTitle = it.take(60) },
                    label = { Text(stringResource(id = R.string.course_name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (viewModel.saveCurrentMyCourse(myCourseTitle)) {
                        showSaveMyCourseDialog = false
                        Toast.makeText(
                            context,
                            context.getString(R.string.my_course_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) { Text(stringResource(id = R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveMyCourseDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
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
