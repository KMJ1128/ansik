package com.kmj.ansik.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun AnsikApp() {

    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    val sharedPref = context.getSharedPreferences(
        "AnsikPrefs",
        Context.MODE_PRIVATE
    )

    val isFirstLaunch = sharedPref.getBoolean(
        "isFirstLaunch",
        true
    )

    NavHost(
        navController = navController,
        startDestination = if (isFirstLaunch) {
            "language"
        } else {
            "main"
        }
    ) {

        // ====================================================
        // 언어 선택
        // ====================================================

        composable("language") {

            LanguageScreen(
                onLanguageSelected = { languageTag ->

                    sharedPref.edit()
                        .putBoolean("isFirstLaunch", false)
                        .apply()

                    val localeList =
                        LocaleListCompat.forLanguageTags(languageTag)

                    AppCompatDelegate.setApplicationLocales(
                        localeList
                    )

                    navController.navigate("main") {
                        popUpTo("language") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ====================================================
        // 메인 화면
        // 하단 네비게이션 포함
        // ====================================================

        composable("main") {

            MainTabScreen(
                viewModel = viewModel
            )
        }
    }
}


// ============================================================
// 메인 하단 탭 화면
// ============================================================

@Composable
private fun MainTabScreen(
    viewModel: MainViewModel
) {

    val navController = rememberNavController()

    val navBackStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        navBackStackEntry?.destination?.route
            ?: "map"

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        bottomBar = {

            NavigationBar {

                // ------------------------------------------------
                // 지도
                // ------------------------------------------------

                NavigationBarItem(
                    selected = currentRoute == "map",
                    onClick = {
                        navController.navigate("map") {
                            popUpTo("map") {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "지도"
                        )
                    },
                    label = {
                        Text("지도")
                    }
                )

                // ------------------------------------------------
                // AI 추천 코스
                // ------------------------------------------------

                NavigationBarItem(
                    selected = currentRoute == "ai",
                    onClick = {
                        navController.navigate("ai") {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI 추천 코스"
                        )
                    },
                    label = {
                        Text("AI 추천 코스")
                    }
                )

                // ------------------------------------------------
                // 설정
                // ------------------------------------------------

                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정"
                        )
                    },
                    label = {
                        Text("설정")
                    }
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            NavHost(
                navController = navController,
                startDestination = "map"
            ) {

                // ====================================================
                // 지도
                // ====================================================

                composable("map") {

                    MapScreen(
                        viewModel = viewModel
                    )
                }

                // ====================================================
                // AI 추천 코스
                // ====================================================

                composable("ai") {

                    AiRecommendationScreen(
                        viewModel = viewModel
                    )
                }

                // ====================================================
                // 설정
                // ====================================================

                composable("settings") {

                    SettingsScreen(
                        onNavigateBack = {
                            navController.navigate("map") {
                                popUpTo("map") {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLanguage = {
                            navController.navigate("language")
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// AI 추천 코스 화면
// ============================================================

@Composable
private fun AiRecommendationScreen(
    viewModel: MainViewModel
) {

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 24.dp,
                start = 20.dp,
                end = 20.dp
            )
    ) {

        Text(
            text = "AI 추천 코스",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "건강 조건과 여행 정보를 기반으로\n맞춤 여행 코스를 추천해드려요.",
            fontSize = 15.sp,
            color = Color(0xFF757575)
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "✨ AI 여행 코스 추천",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "선택하신 건강·식단 조건과\n여행 일정을 분석하여 최적의 코스를 추천합니다.",
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = {
                        // 추후 AI 추천 기능 연결
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {

                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "AI 추천 코스 만들기",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (viewModel.selectedConditions.value.isNotEmpty()) {

            Text(
                text = "현재 선택된 조건",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    viewModel.selectedConditions.value.toList()
                ) { condition ->

                    Surface(
                        color = Color(0xFFF1F8E9),
                        shape = RoundedCornerShape(50)
                    ) {

                        Text(
                            text = condition,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),
                            fontSize = 13.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}