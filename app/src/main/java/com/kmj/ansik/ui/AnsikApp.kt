package com.kmj.ansik.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.kmj.ansik.auth.AuthViewModel
import com.kmj.ansik.auth.LoginScreen

@Composable
fun AnsikApp() {

    val context = LocalContext.current
    val rootNavController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val sharedPref = context.getSharedPreferences(
        "AnsikPrefs",
        Context.MODE_PRIVATE
    )

    val isFirstLaunch = sharedPref.getBoolean(
        "isFirstLaunch",
        true
    )
    val hasGuestSession = sharedPref.getBoolean("continueAsGuest", false)
    val initialRoute = when {
        isFirstLaunch -> "language"
        authViewModel.uiState.value.isAuthenticated || hasGuestSession -> "main"
        else -> "login"
    }

    NavHost(
        navController = rootNavController,
        startDestination = initialRoute
    ) {
        composable("language") {
            LanguageScreen(
                onLanguageSelected = { languageTag ->
                    sharedPref.edit()
                        .putBoolean("isFirstLaunch", false)
                        .apply()
                    val localeList = LocaleListCompat.forLanguageTags(languageTag)
                    AppCompatDelegate.setApplicationLocales(localeList)
                    val nextRoute = if (
                        authViewModel.uiState.value.isAuthenticated ||
                        sharedPref.getBoolean("continueAsGuest", false)
                    ) "main" else "login"
                    rootNavController.navigate(nextRoute) {
                        popUpTo("language") {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    sharedPref.edit().putBoolean("continueAsGuest", false).apply()
                    rootNavController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onContinueAsGuest = {
                    sharedPref.edit().putBoolean("continueAsGuest", true).apply()
                    rootNavController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("main") {
            MainTabScreen(
                viewModel = viewModel,
                authViewModel = authViewModel,
                onNavigateToLanguage = {
                    rootNavController.navigate("language")
                },
                onNavigateToLogin = {
                    sharedPref.edit().putBoolean("continueAsGuest", false).apply()
                    rootNavController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun MainTabScreen(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel,
    onNavigateToLanguage: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "map"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.Surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == "map",
                    onClick = {
                        navController.navigate("map") {
                            popUpTo("map") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Map, contentDescription = stringResource(id = R.string.tab_map)) },
                    label = { Text(stringResource(id = R.string.tab_map), fontWeight = FontWeight.Bold) },
                    colors = playfulNavigationColors()
                )

                NavigationBarItem(
                    selected = currentRoute == "ai",
                    onClick = {
                        navController.navigate("ai") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(id = R.string.tab_ai_course)) },
                    label = { Text(stringResource(id = R.string.tab_ai_course), fontWeight = FontWeight.Bold) },
                    colors = playfulNavigationColors()
                )

                NavigationBarItem(
                    selected = currentRoute == "profile",
                    onClick = {
                        navController.navigate("profile") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = stringResource(id = R.string.tab_profile)) },
                    label = { Text(stringResource(id = R.string.tab_profile), fontWeight = FontWeight.Bold) },
                    colors = playfulNavigationColors()
                )

                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(id = R.string.settings)) },
                    label = { Text(stringResource(id = R.string.settings), fontWeight = FontWeight.Bold) },
                    colors = playfulNavigationColors()
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
                composable("map") { MainScreen(viewModel = viewModel) }
                composable("ai") {
                    AiRecommendationScreen(
                        viewModel = viewModel,
                        onCourseApplied = {
                            navController.navigate("map") {
                                popUpTo("map") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
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
                        authUser = authViewModel.uiState.value.user,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLanguage = { onNavigateToLanguage() },
                        onLogin = onNavigateToLogin,
                        onLogout = {
                            authViewModel.logout(onNavigateToLogin)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun playfulNavigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppColors.PrimaryDark,
    selectedTextColor = AppColors.PrimaryDark,
    indicatorColor = AppColors.PrimarySoft,
    unselectedIconColor = AppColors.TextSecondary,
    unselectedTextColor = AppColors.TextSecondary
)
