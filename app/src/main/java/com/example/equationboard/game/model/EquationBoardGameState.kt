package com.example.equationboard.game.model

data class EquationBoardGameState(
    val cells: List<NumberCell> = emptyList(),
    val target: Int = 0,
    val selectedCellIds: List<Int> = emptyList(),
    val operations: List<Operation> = emptyList(),
    val currentEquation: Equation? = null,
    val targetNumber: Int = 1,
    val targetIndex: Int = 1,
    val totalTargets: Int = 10,
    val solvedTargets: Int = 0,
    val skippedTargets: Int = 0,
    val failedAttempts: Int = 0,
    val score: Int = 0,
    val hintsUsed: Int = 0,
    val reshufflesUsed: Int = 0,
    val skipsUsed: Int = 0,
    val lastResult: Int? = null,
    val lastEquationCorrect: Boolean? = null,
    val phase: GamePhase = GamePhase.BUILDING,
    val currentHint: String? = null,
    val hintUsedForCurrentTarget: Boolean = false,
    val failedOnCurrentTarget: Boolean = false,
    val feedbackMessage: String? = null,
    val twoNumberSolves: Int = 0,
    val threeNumberSolves: Int = 0,
    val noSolutionAvailable: Boolean = false
) {
    val totalAttempts: Int
        get() = solvedTargets + failedAttempts

    val accuracy: Int
        get() = if (totalAttempts > 0) {
            (solvedTargets * 100) / totalAttempts
        } else {
            100
        }

    val isPerfectRun: Boolean
        get() = solvedTargets == totalTargets &&
                failedAttempts == 0 &&
                hintsUsed == 0 &&
                skipsUsed == 0

    val maxSkips: Int = 3
    val maxReshuffles: Int = 2
}
