package com.kmj.ansik.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.rememberCameraPositionState

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val cameraPositionState =
        rememberCameraPositionState()

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
    }

    ReviewBottomSheet(
        viewModel = viewModel
    )
}