package com.example.equationboard.data

data class SettingsState(
    val theme: String = THEME_SYSTEM,
    val dynamicColor: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animationsEnabled: Boolean = true,
    val showHints: Boolean = true,
    val showCoordinates: Boolean = false,
    val confirmRestart: Boolean = true,
    val threeNumberEquations: Boolean = true
) {
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
}

data class StatisticsState(
    val gamesPlayed: Int = 0,
    val completedGames: Int = 0,
    val bestScore: Int = 0,
    val bestAccuracy: Int = 0,
    val perfectRuns: Int = 0,
    val totalSolves: Int = 0,
    val totalSkips: Int = 0,
    val totalFailedAttempts: Int = 0,
    val totalHints: Int = 0,
    val totalReshuffles: Int = 0,
    val twoNumberSolves: Int = 0,
    val threeNumberSolves: Int = 0
)
