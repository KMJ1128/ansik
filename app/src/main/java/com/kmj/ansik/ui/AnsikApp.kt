package com.kmj.ansik.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kmj.ansik.R

@Composable
fun AnsikApp() {

    val context = LocalContext.current
    val rootNavController = rememberNavController()
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
        navController = rootNavController,
        startDestination = if (isFirstLaunch) {
            "language"
        } else {
            "main"
        }
    ) {
        composable("language") {
            LanguageScreen(
                onLanguageSelected = { languageTag ->
                    sharedPref.edit()
                        .putBoolean("isFirstLaunch", false)
                        .apply()
                    val localeList = LocaleListCompat.forLanguageTags(languageTag)
                    AppCompatDelegate.setApplicationLocales(localeList)
                    rootNavController.navigate("main") {
                        popUpTo("language") {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable("main") {
            MainTabScreen(
                viewModel = viewModel,
                onNavigateToLanguage = {
                    rootNavController.navigate("language")
                }
            )
        }
    }
}

@Composable
private fun MainTabScreen(
    viewModel: MainViewModel,
    onNavigateToLanguage: () -> Unit
) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "map"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "map",
                    onClick = {
                        navController.navigate("map") {
                            popUpTo("map") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Map, contentDescription = stringResource(id = R.string.tab_map)) },
                    label = { Text(stringResource(id = R.string.tab_map)) }
                )

                NavigationBarItem(
                    selected = currentRoute == "ai",
                    onClick = {
                        navController.navigate("ai") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(id = R.string.tab_ai_course)) },
                    label = { Text(stringResource(id = R.string.tab_ai_course)) }
                )

                NavigationBarItem(
                    selected = currentRoute == "profile",
                    onClick = {
                        navController.navigate("profile") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = stringResource(id = R.string.tab_profile)) },
                    label = { Text(stringResource(id = R.string.tab_profile)) }
                )

                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(id = R.string.settings)) },
                    label = { Text(stringResource(id = R.string.settings)) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = "map"
            ) {
                composable("map") { MapScreen(viewModel = viewModel) }
                composable("ai") { AiRecommendationScreen(viewModel = viewModel) }
                composable("profile") {
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToMap = {
                            navController.navigate("map") {
                                popUpTo("map") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLanguage = { onNavigateToLanguage() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AiRecommendationScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Text(
            text = stringResource(id = R.string.ai_course_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.ai_course_desc),
            fontSize = 15.sp,
            color = Color(0xFF757575)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(id = R.string.ai_course_card_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.ai_course_card_desc),
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { /* 추후 기능 연결 */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.ai_course_button), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.selectedConditions.value.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.current_selected_conditions),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.selectedConditions.value.toList()) { condition ->
                    Surface(
                        color = Color(0xFFF1F8E9),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = condition,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}