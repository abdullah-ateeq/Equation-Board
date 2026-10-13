package com.example.equationboard.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(
    private val sharedPreferences: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    companion object {
        const val PREFS_NAME = "equation_board_prefs"

        // Settings Keys
        private const val KEY_THEME = "setting_theme"
        private const val KEY_DYNAMIC_COLOR = "setting_dynamic_color"
        private const val KEY_SOUND = "setting_sound"
        private const val KEY_HAPTICS = "setting_haptics"
        private const val KEY_ANIMATIONS = "setting_animations"
        private const val KEY_SHOW_HINTS = "setting_show_hints"
        private const val KEY_SHOW_COORDINATES = "setting_show_coordinates"
        private const val KEY_CONFIRM_RESTART = "setting_confirm_restart"
        private const val KEY_THREE_NUMBER_EQUATIONS = "setting_three_number_equations"

        // Statistics Keys
        private const val KEY_GAMES_PLAYED = "stat_games_played"
        private const val KEY_COMPLETED_GAMES = "stat_completed_games"
        private const val KEY_BEST_SCORE = "stat_best_score"
        private const val KEY_BEST_ACCURACY = "stat_best_accuracy"
        private const val KEY_PERFECT_RUNS = "stat_perfect_runs"
        private const val KEY_TOTAL_SOLVES = "stat_total_solves"
        private const val KEY_TOTAL_SKIPS = "stat_total_skips"
        private const val KEY_TOTAL_FAILED = "stat_total_failed"
        private const val KEY_TOTAL_HINTS = "stat_total_hints"
        private const val KEY_TOTAL_RESHUFFLES = "stat_total_reshuffles"
        private const val KEY_TWO_NUMBER_SOLVES = "stat_two_number_solves"
        private const val KEY_THREE_NUMBER_SOLVES = "stat_three_number_solves"
    }

    fun getSettings(): SettingsState {
        return SettingsState(
            theme = sharedPreferences.getString(KEY_THEME, SettingsState.THEME_SYSTEM) ?: SettingsState.THEME_SYSTEM,
            dynamicColor = sharedPreferences.getBoolean(KEY_DYNAMIC_COLOR, true),
            soundEnabled = sharedPreferences.getBoolean(KEY_SOUND, true),
            hapticFeedback = sharedPreferences.getBoolean(KEY_HAPTICS, true),
            animationsEnabled = sharedPreferences.getBoolean(KEY_ANIMATIONS, true),
            showHints = sharedPreferences.getBoolean(KEY_SHOW_HINTS, true),
            showCoordinates = sharedPreferences.getBoolean(KEY_SHOW_COORDINATES, false),
            confirmRestart = sharedPreferences.getBoolean(KEY_CONFIRM_RESTART, true),
            threeNumberEquations = sharedPreferences.getBoolean(KEY_THREE_NUMBER_EQUATIONS, true)
        )
    }

    fun saveSettings(settings: SettingsState) {
        sharedPreferences.edit()
            .putString(KEY_THEME, settings.theme)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_SOUND, settings.soundEnabled)
            .putBoolean(KEY_HAPTICS, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, settings.animationsEnabled)
            .putBoolean(KEY_SHOW_HINTS, settings.showHints)
            .putBoolean(KEY_SHOW_COORDINATES, settings.showCoordinates)
            .putBoolean(KEY_CONFIRM_RESTART, settings.confirmRestart)
            .putBoolean(KEY_THREE_NUMBER_EQUATIONS, settings.threeNumberEquations)
            .apply()
    }

    fun resetSettings(): SettingsState {
        val defaultSettings = SettingsState()
        saveSettings(defaultSettings)
        return defaultSettings
    }

    fun getStatistics(): StatisticsState {
        return StatisticsState(
            gamesPlayed = sharedPreferences.getInt(KEY_GAMES_PLAYED, 0),
            completedGames = sharedPreferences.getInt(KEY_COMPLETED_GAMES, 0),
            bestScore = sharedPreferences.getInt(KEY_BEST_SCORE, 0),
            bestAccuracy = sharedPreferences.getInt(KEY_BEST_ACCURACY, 0),
            perfectRuns = sharedPreferences.getInt(KEY_PERFECT_RUNS, 0),
            totalSolves = sharedPreferences.getInt(KEY_TOTAL_SOLVES, 0),
            totalSkips = sharedPreferences.getInt(KEY_TOTAL_SKIPS, 0),
            totalFailedAttempts = sharedPreferences.getInt(KEY_TOTAL_FAILED, 0),
            totalHints = sharedPreferences.getInt(KEY_TOTAL_HINTS, 0),
            totalReshuffles = sharedPreferences.getInt(KEY_TOTAL_RESHUFFLES, 0),
            twoNumberSolves = sharedPreferences.getInt(KEY_TWO_NUMBER_SOLVES, 0),
            threeNumberSolves = sharedPreferences.getInt(KEY_THREE_NUMBER_SOLVES, 0)
        )
    }

    fun incrementGamesPlayed() {
        val current = sharedPreferences.getInt(KEY_GAMES_PLAYED, 0)
        sharedPreferences.edit().putInt(KEY_GAMES_PLAYED, current + 1).apply()
    }

    fun recordCompletedGame(
        score: Int,
        accuracy: Int,
        isPerfect: Boolean,
        solves: Int,
        skips: Int,
        failed: Int,
        hints: Int,
        reshuffles: Int,
        twoNumSolves: Int,
        threeNumSolves: Int
    ): StatisticsState {
        val stats = getStatistics()
        val newCompleted = stats.completedGames + 1
        val newBestScore = maxOf(stats.bestScore, score)
        val newBestAccuracy = maxOf(stats.bestAccuracy, accuracy)
        val newPerfectRuns = if (isPerfect) stats.perfectRuns + 1 else stats.perfectRuns
        val newTotalSolves = stats.totalSolves + solves
        val newTotalSkips = stats.totalSkips + skips
        val newTotalFailed = stats.totalFailedAttempts + failed
        val newTotalHints = stats.totalHints + hints
        val newTotalReshuffles = stats.totalReshuffles + reshuffles
        val newTwoNum = stats.twoNumberSolves + twoNumSolves
        val newThreeNum = stats.threeNumberSolves + threeNumSolves

        sharedPreferences.edit()
            .putInt(KEY_COMPLETED_GAMES, newCompleted)
            .putInt(KEY_BEST_SCORE, newBestScore)
            .putInt(KEY_BEST_ACCURACY, newBestAccuracy)
            .putInt(KEY_PERFECT_RUNS, newPerfectRuns)
            .putInt(KEY_TOTAL_SOLVES, newTotalSolves)
            .putInt(KEY_TOTAL_SKIPS, newTotalSkips)
            .putInt(KEY_TOTAL_FAILED, newTotalFailed)
            .putInt(KEY_TOTAL_HINTS, newTotalHints)
            .putInt(KEY_TOTAL_RESHUFFLES, newTotalReshuffles)
            .putInt(KEY_TWO_NUMBER_SOLVES, newTwoNum)
            .putInt(KEY_THREE_NUMBER_SOLVES, newThreeNum)
            .apply()

        return getStatistics()
    }

    fun resetStatistics(): StatisticsState {
        val empty = StatisticsState()
        sharedPreferences.edit()
            .putInt(KEY_GAMES_PLAYED, 0)
            .putInt(KEY_COMPLETED_GAMES, 0)
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_BEST_ACCURACY, 0)
            .putInt(KEY_PERFECT_RUNS, 0)
            .putInt(KEY_TOTAL_SOLVES, 0)
            .putInt(KEY_TOTAL_SKIPS, 0)
            .putInt(KEY_TOTAL_FAILED, 0)
            .putInt(KEY_TOTAL_HINTS, 0)
            .putInt(KEY_TOTAL_RESHUFFLES, 0)
            .putInt(KEY_TWO_NUMBER_SOLVES, 0)
            .putInt(KEY_THREE_NUMBER_SOLVES, 0)
            .apply()
        return empty
    }
}
