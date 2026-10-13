package com.example.equationboard.navigation

sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "Splash")
    object Home : Screen("home", "Home")
    object Game : Screen("game", "Game")
    object Result : Screen("result", "Result")
    object Settings : Screen("settings", "Settings")
    object About : Screen("about", "About")
}
