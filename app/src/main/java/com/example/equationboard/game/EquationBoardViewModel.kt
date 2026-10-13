package com.example.equationboard.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.equationboard.data.PreferencesManager
import com.example.equationboard.data.SettingsState
import com.example.equationboard.data.StatisticsState
import com.example.equationboard.game.logic.EquationBoardGenerator
import com.example.equationboard.game.logic.EquationEngine
import com.example.equationboard.game.logic.EquationSolutionFinder
import com.example.equationboard.game.logic.EquationTargetGenerator
import com.example.equationboard.game.model.CellState
import com.example.equationboard.game.model.Equation
import com.example.equationboard.game.model.EquationBoardGameState
import com.example.equationboard.game.model.GamePhase
import com.example.equationboard.game.model.NumberCell
import com.example.equationboard.game.model.Operation
import com.example.equationboard.util.SoundHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EquationBoardViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesManager: PreferencesManager = PreferencesManager(application)
) : AndroidViewModel(application) {

    private val _gameState = MutableStateFlow(EquationBoardGameState())
    val gameState: StateFlow<EquationBoardGameState> = _gameState.asStateFlow()

    private val _settings = MutableStateFlow(preferencesManager.getSettings())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    private val _statistics = MutableStateFlow(preferencesManager.getStatistics())
    val statistics: StateFlow<StatisticsState> = _statistics.asStateFlow()

    private var isProcessing = false

    init {
        startNewGame()
    }

    fun startNewGame() {
        val allowThree = _settings.value.threeNumberEquations
        val initialResult = EquationBoardGenerator.generateNewBoard(
            round = 1,
            allowThreeNumbers = allowThree
        )

        _gameState.value = EquationBoardGameState(
            cells = initialResult.cells,
            target = initialResult.targetPlan.target,
            targetNumber = 1,
            targetIndex = 1,
            totalTargets = 10,
            solvedTargets = 0,
            skippedTargets = 0,
            failedAttempts = 0,
            score = 0,
            hintsUsed = 0,
            reshufflesUsed = 0,
            skipsUsed = 0,
            phase = GamePhase.BUILDING
        )

        preferencesManager.incrementGamesPlayed()
        _statistics.value = preferencesManager.getStatistics()
        SoundHelper.playNewTarget(_settings.value.soundEnabled)
    }

    fun selectCell(cellId: Int) {
        if (isProcessing) return
        val current = _gameState.value
        if (current.phase != GamePhase.BUILDING) return

        val cell = current.cells.find { it.id == cellId } ?: return
        if (cell.state != CellState.ACTIVE) return
        if (current.selectedCellIds.contains(cellId)) return

        val allowThree = _settings.value.threeNumberEquations
        val maxNumbers = if (allowThree) 3 else 2
        val currentNumbersCount = current.selectedCellIds.size

        if (currentNumbersCount >= maxNumbers) return
        // Must follow NUMBER -> OPERATION -> NUMBER -> OPERATION -> NUMBER
        if (currentNumbersCount > current.operations.size) return

        val newSelectedIds = current.selectedCellIds + cellId
        val updatedCells = current.cells.map {
            if (it.id == cellId) it.copy(state = CellState.SELECTED) else it
        }

        val numbers = newSelectedIds.mapNotNull { id -> updatedCells.find { it.id == id }?.value }
        val newEquation = Equation(numbers, current.operations)

        _gameState.value = current.copy(
            cells = updatedCells,
            selectedCellIds = newSelectedIds,
            currentEquation = newEquation,
            feedbackMessage = null
        )

        SoundHelper.playTileSelect(_settings.value.soundEnabled)
    }

    fun selectOperation(op: Operation) {
        if (isProcessing) return
        val current = _gameState.value
        if (current.phase != GamePhase.BUILDING) return

        // Must have at least 1 number selected
        if (current.selectedCellIds.isEmpty()) return

        val allowThree = _settings.value.threeNumberEquations
        val maxOperations = if (allowThree) 2 else 1

        if (current.operations.size >= maxOperations) return
        // Cannot select operation if another operation was just selected
        if (current.operations.size >= current.selectedCellIds.size) return

        val newOperations = current.operations + op
        val numbers = current.selectedCellIds.mapNotNull { id -> current.cells.find { it.id == id }?.value }
        val newEquation = Equation(numbers, newOperations)

        _gameState.value = current.copy(
            operations = newOperations,
            currentEquation = newEquation,
            feedbackMessage = null
        )

        SoundHelper.playOperationSelect(_settings.value.soundEnabled)
    }

    fun clearEquation() {
        if (isProcessing) return
        val current = _gameState.value
        if (current.phase != GamePhase.BUILDING) return

        val updatedCells = current.cells.map {
            if (it.state == CellState.SELECTED) it.copy(state = CellState.ACTIVE) else it
        }

        _gameState.value = current.copy(
            cells = updatedCells,
            selectedCellIds = emptyList(),
            operations = emptyList(),
            currentEquation = null,
            feedbackMessage = null,
            lastResult = null,
            lastEquationCorrect = null
        )
    }

    fun checkEquation() {
        if (isProcessing) return
        val current = _gameState.value
        if (current.phase != GamePhase.BUILDING) return

        val eq = current.currentEquation ?: return
        if (!eq.isComplete) return

        val result = EquationEngine.evaluateEquation(eq)
        val isTargetReached = result != null && result == current.target

        if (isTargetReached) {
            handleSuccess(eq, result)
        } else {
            handleFailure(eq, result)
        }
    }

    private fun handleSuccess(equation: Equation, result: Int) {
        isProcessing = true
        val current = _gameState.value

        // Scoring
        val isThreeNumbers = equation.numbers.size == 3
        val baseScore = if (isThreeNumbers) 125 else 100
        val cleanBonus = if (!current.failedOnCurrentTarget && !current.hintUsedForCurrentTarget) 25 else 0
        val pointsEarned = baseScore + cleanBonus
        val newScore = current.score + pointsEarned

        val newSolvedTargets = current.solvedTargets + 1
        val newTwoSolves = if (isThreeNumbers) current.twoNumberSolves else current.twoNumberSolves + 1
        val newThreeSolves = if (isThreeNumbers) current.threeNumberSolves + 1 else current.threeNumberSolves

        // Highlight consumed cells
        val consumingCells = current.cells.map {
            if (current.selectedCellIds.contains(it.id)) it.copy(state = CellState.CONSUMING) else it
        }

        _gameState.value = current.copy(
            cells = consumingCells,
            score = newScore,
            solvedTargets = newSolvedTargets,
            twoNumberSolves = newTwoSolves,
            threeNumberSolves = newThreeSolves,
            lastResult = result,
            lastEquationCorrect = true,
            phase = GamePhase.SUCCESS,
            feedbackMessage = "TARGET REACHED! +$pointsEarned"
        )

        SoundHelper.playCorrect(_settings.value.soundEnabled)

        viewModelScope.launch {
            delay(550)

            // Turn consuming cells into EMPTY
            val emptyCells = _gameState.value.cells.map {
                if (it.state == CellState.CONSUMING) it.copy(state = CellState.EMPTY) else it
            }

            val nextTargetIndex = current.targetIndex + 1

            if (nextTargetIndex > current.totalTargets) {
                // Game Finished!
                _gameState.value = _gameState.value.copy(
                    cells = emptyCells,
                    phase = GamePhase.GAME_COMPLETE,
                    feedbackMessage = "GAME COMPLETE!"
                )
                saveGameStatistics(_gameState.value)
                SoundHelper.playGameComplete(_settings.value.soundEnabled)
                isProcessing = false
            } else {
                // Next Target
                val allowThree = _settings.value.threeNumberEquations
                val nextTargetPlan = EquationTargetGenerator.generateTarget(
                    round = nextTargetIndex,
                    allowThreeNumbers = allowThree
                )

                // Refill empty cells ensuring target is solvable
                val refilledCells = EquationBoardGenerator.refillEmptyCells(
                    cells = emptyCells,
                    target = nextTargetPlan.target,
                    targetPlan = nextTargetPlan,
                    allowThreeNumbers = allowThree
                )

                _gameState.value = _gameState.value.copy(
                    cells = refilledCells,
                    target = nextTargetPlan.target,
                    targetIndex = nextTargetIndex,
                    targetNumber = nextTargetIndex,
                    selectedCellIds = emptyList(),
                    operations = emptyList(),
                    currentEquation = null,
                    lastResult = null,
                    lastEquationCorrect = null,
                    currentHint = null,
                    hintUsedForCurrentTarget = false,
                    failedOnCurrentTarget = false,
                    feedbackMessage = null,
                    phase = GamePhase.BUILDING
                )

                SoundHelper.playTileRemoval(_settings.value.soundEnabled)
                delay(200)
                SoundHelper.playNewTarget(_settings.value.soundEnabled)
                isProcessing = false
            }
        }
    }

    private fun handleFailure(equation: Equation, result: Int?) {
        isProcessing = true
        val current = _gameState.value
        val newFailed = current.failedAttempts + 1

        val feedback = if (result != null) {
            "$result ≠ ${current.target}\nNOT THE TARGET"
        } else {
            "INVALID EQUATION"
        }

        _gameState.value = current.copy(
            failedAttempts = newFailed,
            failedOnCurrentTarget = true,
            lastResult = result,
            lastEquationCorrect = false,
            phase = GamePhase.FAILED,
            feedbackMessage = feedback
        )

        SoundHelper.playIncorrect(_settings.value.soundEnabled)

        viewModelScope.launch {
            delay(500)
            // Return selected tiles to ACTIVE, do not remove any tiles
            val restoredCells = _gameState.value.cells.map {
                if (it.state == CellState.SELECTED) it.copy(state = CellState.ACTIVE) else it
            }

            _gameState.value = _gameState.value.copy(
                cells = restoredCells,
                selectedCellIds = emptyList(),
                operations = emptyList(),
                currentEquation = null,
                feedbackMessage = null,
                lastResult = null,
                lastEquationCorrect = null,
                phase = GamePhase.BUILDING
            )
            isProcessing = false
        }
    }

    fun skipTarget() {
        if (isProcessing) return
        val current = _gameState.value
        if (current.phase != GamePhase.BUILDING) return
        if (current.skipsUsed >= current.maxSkips) return

        isProcessing = true
        val newSkipsUsed = current.skipsUsed + 1
        val newSkipped = current.skippedTargets + 1

        val restoredCells = current.cells.map {
            if (it.state == CellState.SELECTED) it.copy(state = CellState.ACTIVE) else it
        }

        val nextTargetIndex = current.targetIndex + 1

        SoundHelper.playSkip(_settings.value.soundEnabled)

        if (nextTargetIndex > current.totalTargets) {
            _gameState.value = current.copy(
                cells = restoredCells,
                skipsUsed = newSkipsUsed,
                skippedTargets = newSkipped,
                selectedCellIds = emptyList(),
                operations = emptyList(),
                currentEquation = null,
                phase = GamePhase.GAME_COMPLETE,
                feedbackMessage = "Target skipped. Game complete!"
            )
            saveGameStatistics(_gameState.value)
            SoundHelper.playGameComplete(_settings.value.soundEnabled)
            isProcessing = false
        } else {
            val allowThree = _settings.value.threeNumberEquations
            val nextTargetPlan = EquationTargetGenerator.generateTarget(
                round = nextTargetIndex,
                allowThreeNumbers = allowThree
            )

            // Ensure current active cells can reach next target; if not, guarantee solution
            var activeBoard = restoredCells
            if (!EquationSolutionFinder.hasSolution(activeBoard, nextTargetPlan.target, allowThree)) {
                // Ensure required numbers are on board
                val nonEmpties = activeBoard.filter { it.isActive }.shuffled()
                val updated = activeBoard.toMutableList()
                for (i in nextTargetPlan.requiredNumbers.indices) {
                    if (i < nonEmpties.size) {
                        val cellToReplace = nonEmpties[i]
                        val idx = updated.indexOfFirst { it.id == cellToReplace.id }
                        if (idx != -1) {
                            updated[idx] = updated[idx].copy(value = nextTargetPlan.requiredNumbers[i])
                        }
                    }
                }
                activeBoard = updated
            }

            _gameState.value = current.copy(
                cells = activeBoard,
                target = nextTargetPlan.target,
                targetIndex = nextTargetIndex,
                targetNumber = nextTargetIndex,
                skipsUsed = newSkipsUsed,
                skippedTargets = newSkipped,
                selectedCellIds = emptyList(),
                operations = emptyList(),
                currentEquation = null,
                currentHint = null,
                hintUsedForCurrentTarget = false,
                failedOnCurrentTarget = false,
                feedbackMessage = "Target skipped.",
                phase = GamePhase.BUILDING
            )
            isProcessing = false
        }
    }

    fun reshuffleBoard() {
        if (isProcessing) return
        val current = _gameState.value
        if (current.phase != GamePhase.BUILDING) return
        if (current.reshufflesUsed >= current.maxReshuffles) return

        isProcessing = true
        val newReshufflesUsed = current.reshufflesUsed + 1
        val reshuffled = EquationBoardGenerator.reshuffleActiveCells(current.cells)

        val allowThree = _settings.value.threeNumberEquations
        val hasSolution = EquationSolutionFinder.hasSolution(reshuffled, current.target, allowThree)

        var finalCells = reshuffled
        var finalTarget = current.target
        var feedback: String? = "Board reshuffled."

        if (!hasSolution) {
            // If mathematically active numbers cannot reach target, generate a solvable target
            val newPlan = EquationTargetGenerator.generateTarget(current.targetIndex, allowThree)
            finalTarget = newPlan.target
            finalCells = EquationBoardGenerator.refillEmptyCells(reshuffled, finalTarget, newPlan, allowThree)
            feedback = "NO SOLUTION FOUND. New target generated!"
        }

        _gameState.value = current.copy(
            cells = finalCells,
            target = finalTarget,
            reshufflesUsed = newReshufflesUsed,
            selectedCellIds = emptyList(),
            operations = emptyList(),
            currentEquation = null,
            feedbackMessage = feedback,
            noSolutionAvailable = !hasSolution
        )

        SoundHelper.playOperationSelect(_settings.value.soundEnabled)
        isProcessing = false
    }

    fun useHint() {
        if (isProcessing) return
        val current = _gameState.value
        if (!_settings.value.showHints) return
        if (current.hintUsedForCurrentTarget) return
        if (current.phase != GamePhase.BUILDING) return

        val allowThree = _settings.value.threeNumberEquations
        val solution = EquationSolutionFinder.findFirstSolution(current.cells, current.target, allowThree)

        if (solution != null) {
            val hintString = solution.equation.toDisplayString(showEquals = true, result = current.target)
            _gameState.value = current.copy(
                hintsUsed = current.hintsUsed + 1,
                hintUsedForCurrentTarget = true,
                currentHint = hintString,
                feedbackMessage = "Hint: $hintString"
            )
            SoundHelper.playHint(_settings.value.soundEnabled)
        }
    }

    private fun saveGameStatistics(state: EquationBoardGameState) {
        val updated = preferencesManager.recordCompletedGame(
            score = state.score,
            accuracy = state.accuracy,
            isPerfect = state.isPerfectRun,
            solves = state.solvedTargets,
            skips = state.skippedTargets,
            failed = state.failedAttempts,
            hints = state.hintsUsed,
            reshuffles = state.reshufflesUsed,
            twoNumSolves = state.twoNumberSolves,
            threeNumSolves = state.threeNumberSolves
        )
        _statistics.value = updated
    }

    fun updateSettings(newSettings: SettingsState) {
        preferencesManager.saveSettings(newSettings)
        _settings.value = newSettings
    }

    fun resetSettings() {
        val defaults = preferencesManager.resetSettings()
        _settings.value = defaults
    }

    fun resetStatistics() {
        val empty = preferencesManager.resetStatistics()
        _statistics.value = empty
    }
}
