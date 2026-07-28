package com.kmj.ansik.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun AnsikApp() {

    val navController = rememberNavController()
    // 앱 전체에서 단 하나만 존재하며 상태를 공유하는 ViewModel 생성
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "profile") {
        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToMap = { navController.navigate("map") }
            )
        }
        composable("map") {
            MapScreen(viewModel = viewModel)
        }
    }
}