package com.example.equationboard.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.equationboard.game.EquationBoardViewModel
import com.example.equationboard.ui.screens.AboutScreen
import com.example.equationboard.ui.screens.GameScreen
import com.example.equationboard.ui.screens.HomeScreen
import com.example.equationboard.ui.screens.ResultScreen
import com.example.equationboard.ui.screens.SettingsScreen
import com.example.equationboard.ui.screens.SplashScreen

@Composable
fun EquationBoardNavHost(
    viewModel: EquationBoardViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val gameState by viewModel.gameState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val statistics by viewModel.statistics.collectAsState()

    // Show bottom navigation ONLY on Home, Settings, and About
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Settings.route,
        Screen.About.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val bottomNavItems = listOf(
                        Triple(Screen.Home.route, "Home", Icons.Default.Home),
                        Triple(Screen.Settings.route, "Settings", Icons.Default.Settings),
                        Triple(Screen.About.route, "About", Icons.Default.Info)
                    )

                    bottomNavItems.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    statistics = statistics,
                    onPlayClick = {
                        viewModel.startNewGame()
                        navController.navigate(Screen.Game.route)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onAboutClick = {
                        navController.navigate(Screen.About.route)
                    }
                )
            }

            composable(Screen.Game.route) {
                GameScreen(
                    viewModel = viewModel,
                    gameState = gameState,
                    settings = settings,
                    onNavigateToResult = {
                        navController.navigate(Screen.Result.route) {
                            popUpTo(Screen.Game.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Result.route) {
                ResultScreen(
                    gameState = gameState,
                    onPlayAgainClick = {
                        viewModel.startNewGame()
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Result.route) { inclusive = true }
                        }
                    },
                    onHomeClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = settings,
                    onSettingsChanged = { viewModel.updateSettings(it) },
                    onResetSettings = { viewModel.resetSettings() },
                    onResetStatistics = { viewModel.resetStatistics() }
                )
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
