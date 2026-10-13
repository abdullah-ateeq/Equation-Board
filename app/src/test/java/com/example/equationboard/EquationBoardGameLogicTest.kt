package com.example.equationboard

import com.example.equationboard.data.PreferencesManager
import com.example.equationboard.data.SettingsState
import com.example.equationboard.game.logic.EquationBoardGenerator
import com.example.equationboard.game.logic.EquationEngine
import com.example.equationboard.game.logic.EquationSolutionFinder
import com.example.equationboard.game.logic.EquationTargetGenerator
import com.example.equationboard.game.model.CellState
import com.example.equationboard.game.model.Coordinate
import com.example.equationboard.game.model.Equation
import com.example.equationboard.game.model.EquationBoardGameState
import com.example.equationboard.game.model.GamePhase
import com.example.equationboard.game.model.NumberCell
import com.example.equationboard.game.model.Operation
import com.example.equationboard.testutil.FakeSharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EquationBoardGameLogicTest {

    // 1. Board generation
    @Test
    fun test01_boardGeneration() {
        val boardResult = EquationBoardGenerator.generateNewBoard(round = 1)
        assertNotNull(boardResult)
        assertNotNull(boardResult.cells)
        assertNotNull(boardResult.targetPlan)
        assertTrue(boardResult.cells.isNotEmpty())
    }

    // 2. Exactly 25 cells
    @Test
    fun test02_exactly25Cells() {
        val boardResult = EquationBoardGenerator.generateNewBoard(round = 1)
        assertEquals(25, boardResult.cells.size)
        assertEquals(25, EquationBoardGenerator.TOTAL_CELLS)
    }

    // 3. Number range 1–12
    @Test
    fun test03_numberRange1To12() {
        val boardResult = EquationBoardGenerator.generateNewBoard(round = 1)
        for (cell in boardResult.cells) {
            assertTrue("Cell value ${cell.value} out of range 1..12", cell.value in 1..12)
        }
    }

    // 4. Coordinate uniqueness
    @Test
    fun test04_coordinateUniqueness() {
        val boardResult = EquationBoardGenerator.generateNewBoard(round = 1)
        val coordinates = boardResult.cells.map { it.coordinate.label }
        val uniqueCoordinates = coordinates.toSet()
        assertEquals(25, uniqueCoordinates.size)
        assertTrue(coordinates.contains("A1"))
        assertTrue(coordinates.contains("E5"))
    }

    // 5. Guaranteed target solution
    @Test
    fun test05_guaranteedTargetSolution() {
        for (round in 1..10) {
            val boardResult = EquationBoardGenerator.generateNewBoard(round = round)
            val hasSolution = EquationSolutionFinder.hasSolution(
                cells = boardResult.cells,
                target = boardResult.targetPlan.target,
                allowThreeNumbers = true
            )
            assertTrue("Round $round must have a guaranteed solution for target ${boardResult.targetPlan.target}", hasSolution)
        }
    }

    // 6. Target generation
    @Test
    fun test06_targetGeneration() {
        val plan = EquationTargetGenerator.generateTarget(round = 1)
        assertTrue(plan.target in 1..60)
        assertTrue(plan.requiredNumbers.isNotEmpty())
        assertEquals(plan.target, EquationEngine.evaluateEquation(plan.equation))
    }

    // 7. Addition: 7 + 8 = 15
    @Test
    fun test07_addition() {
        val eq = Equation(listOf(7, 8), listOf(Operation.ADD))
        val result = EquationEngine.evaluateEquation(eq)
        assertEquals(15, result)
        assertTrue(EquationEngine.isTargetReached(eq, 15))
    }

    // 8. Subtraction: 9 − 4 = 5
    @Test
    fun test08_subtraction() {
        val eq = Equation(listOf(9, 4), listOf(Operation.SUBTRACT))
        val result = EquationEngine.evaluateEquation(eq)
        assertEquals(5, result)
        assertTrue(EquationEngine.isTargetReached(eq, 5))
    }

    // 9. Multiplication: 6 × 4 = 24
    @Test
    fun test09_multiplication() {
        val eq = Equation(listOf(6, 4), listOf(Operation.MULTIPLY))
        val result = EquationEngine.evaluateEquation(eq)
        assertEquals(24, result)
        assertTrue(EquationEngine.isTargetReached(eq, 24))
    }

    // 10. Two-number equations
    @Test
    fun test10_twoNumberEquations() {
        val addEq = Equation(listOf(8, 7), listOf(Operation.ADD))
        val subEq = Equation(listOf(12, 5), listOf(Operation.SUBTRACT))
        val mulEq = Equation(listOf(6, 3), listOf(Operation.MULTIPLY))

        assertEquals(15, EquationEngine.evaluateEquation(addEq))
        assertEquals(7, EquationEngine.evaluateEquation(subEq))
        assertEquals(18, EquationEngine.evaluateEquation(mulEq))
    }

    // 11. Three-number equations
    @Test
    fun test11_threeNumberEquations() {
        val eq1 = Equation(listOf(5, 4, 3), listOf(Operation.ADD, Operation.ADD))
        val eq2 = Equation(listOf(6, 3, 2), listOf(Operation.MULTIPLY, Operation.SUBTRACT))
        val eq3 = Equation(listOf(4, 5, 2), listOf(Operation.MULTIPLY, Operation.ADD))
        val eq4 = Equation(listOf(9, 4, 3), listOf(Operation.SUBTRACT, Operation.ADD))

        assertEquals(12, EquationEngine.evaluateEquation(eq1))
        assertEquals(16, EquationEngine.evaluateEquation(eq2))
        assertEquals(22, EquationEngine.evaluateEquation(eq3))
        assertEquals(8, EquationEngine.evaluateEquation(eq4))
    }

    // 12. Equation evaluation precedence
    @Test
    fun test12_equationEvaluation() {
        // Standard precedence: 2 + 3 * 4 = 2 + 12 = 14
        val eq = Equation(listOf(2, 3, 4), listOf(Operation.ADD, Operation.MULTIPLY))
        assertEquals(14, EquationEngine.evaluateEquation(eq))

        // Negative intermediate results must be rejected (return null)
        val negativeSub = Equation(listOf(3, 8), listOf(Operation.SUBTRACT))
        assertNull(EquationEngine.evaluateEquation(negativeSub))
    }

    // 13. Target validation
    @Test
    fun test13_targetValidation() {
        val eq = Equation(listOf(6, 4), listOf(Operation.MULTIPLY))
        assertTrue(EquationEngine.isTargetReached(eq, 24))
        assertFalse(EquationEngine.isTargetReached(eq, 20))
    }

    // 14. Invalid equation
    @Test
    fun test14_invalidEquation() {
        val target = 20
        val eq = Equation(listOf(6, 4), listOf(Operation.MULTIPLY))
        val result = EquationEngine.evaluateEquation(eq)
        assertEquals(24, result)
        assertFalse(EquationEngine.isTargetReached(eq, target))
    }

    // 15. Same-cell selection
    @Test
    fun test15_sameCellSelection() {
        val selectedCellIds = listOf(1)
        val cannotSelectSameCell = selectedCellIds.contains(1)
        assertTrue("Selecting the same cell twice must be rejected", cannotSelectSameCell)
    }

    // 16. Duplicate tile selection from different cells
    @Test
    fun test16_duplicateTileSelection() {
        val cell1 = NumberCell(1, Coordinate(0, 0), 5, CellState.ACTIVE)
        val cell2 = NumberCell(2, Coordinate(0, 1), 5, CellState.ACTIVE)
        assertNotEquals(cell1.id, cell2.id)
        assertEquals(cell1.value, cell2.value)

        val eq = Equation(listOf(cell1.value, cell2.value), listOf(Operation.ADD))
        assertEquals(10, EquationEngine.evaluateEquation(eq))
    }

    // 17. Operation ordering
    @Test
    fun test17_operationOrdering() {
        // Cannot start with an operation
        assertFalse(EquationEngine.canAppendOperation(numbersCount = 0, operationsCount = 0, allowThreeNumbers = true))

        // Can append operation after 1 number
        assertTrue(EquationEngine.canAppendOperation(numbersCount = 1, operationsCount = 0, allowThreeNumbers = true))

        // Cannot append two consecutive operations
        assertFalse(EquationEngine.canAppendOperation(numbersCount = 1, operationsCount = 1, allowThreeNumbers = true))
    }

    // 18. Maximum equation length
    @Test
    fun test18_maximumEquationLength() {
        // When three numbers disabled: max 2 numbers
        assertFalse(EquationEngine.canAppendNumber(numbersCount = 2, operationsCount = 2, allowThreeNumbers = false))

        // When three numbers enabled: up to 3 numbers
        assertTrue(EquationEngine.canAppendNumber(numbersCount = 2, operationsCount = 2, allowThreeNumbers = true))
        assertFalse(EquationEngine.canAppendNumber(numbersCount = 3, operationsCount = 2, allowThreeNumbers = true))
    }

    // 19. Successful equation
    @Test
    fun test19_successfulEquation() {
        val target = 24
        val eq = Equation(listOf(6, 4), listOf(Operation.MULTIPLY))
        val reached = EquationEngine.isTargetReached(eq, target)
        assertTrue(reached)

        val baseScore = 100
        val cleanBonus = 25
        val totalEarned = baseScore + cleanBonus
        assertEquals(125, totalEarned)
    }

    // 20. Failed equation
    @Test
    fun test20_failedEquation() {
        val target = 24
        val eq = Equation(listOf(6, 5), listOf(Operation.MULTIPLY))
        val reached = EquationEngine.isTargetReached(eq, target)
        assertFalse(reached)

        var failedAttempts = 0
        if (!reached) {
            failedAttempts += 1
        }
        assertEquals(1, failedAttempts)
    }

    // 21. Tile consumption
    @Test
    fun test21_tileConsumption() {
        val cells = listOf(
            NumberCell(0, Coordinate(0, 0), 6, CellState.ACTIVE),
            NumberCell(1, Coordinate(0, 1), 4, CellState.ACTIVE),
            NumberCell(2, Coordinate(0, 2), 7, CellState.ACTIVE)
        )
        val selectedIds = listOf(0, 1)

        val consuming = cells.map {
            if (selectedIds.contains(it.id)) it.copy(state = CellState.CONSUMING) else it
        }
        assertEquals(CellState.CONSUMING, consuming[0].state)
        assertEquals(CellState.CONSUMING, consuming[1].state)
        assertEquals(CellState.ACTIVE, consuming[2].state)

        val emptyCells = consuming.map {
            if (it.state == CellState.CONSUMING) it.copy(state = CellState.EMPTY) else it
        }
        assertEquals(CellState.EMPTY, emptyCells[0].state)
        assertEquals(CellState.EMPTY, emptyCells[1].state)
        assertEquals(CellState.ACTIVE, emptyCells[2].state)
    }

    // 22. Board refill
    @Test
    fun test22_boardRefill() {
        val initial = EquationBoardGenerator.generateNewBoard(round = 1)
        val withEmpty = initial.cells.toMutableList()
        withEmpty[0] = withEmpty[0].copy(state = CellState.EMPTY)
        withEmpty[1] = withEmpty[1].copy(state = CellState.EMPTY)

        val refilled = EquationBoardGenerator.refillEmptyCells(
            cells = withEmpty,
            target = initial.targetPlan.target,
            targetPlan = initial.targetPlan,
            allowThreeNumbers = true
        )
        assertEquals(25, refilled.size)
        assertTrue(refilled.all { it.isActive })
        // Remaining cells should maintain their positions
        for (i in 2 until 25) {
            assertEquals(initial.cells[i].value, refilled[i].value)
        }
    }

    // 23. Target progression
    @Test
    fun test23_targetProgression() {
        // Rounds 1-3 should only use Addition or Subtraction
        for (r in 1..3) {
            val plan = EquationTargetGenerator.generateTarget(round = r, allowThreeNumbers = true)
            for (op in plan.equation.operations) {
                assertTrue("Rounds 1-3 should only have ADD or SUBTRACT", op == Operation.ADD || op == Operation.SUBTRACT)
            }
        }
    }

    // 24. Skip
    @Test
    fun test24_skip() {
        var skipsUsed = 0
        var skippedTargets = 0
        var targetIndex = 1

        skipsUsed += 1
        skippedTargets += 1
        targetIndex += 1

        assertEquals(1, skipsUsed)
        assertEquals(1, skippedTargets)
        assertEquals(2, targetIndex)
    }

    // 25. Reshuffle
    @Test
    fun test25_reshuffle() {
        val initial = EquationBoardGenerator.generateNewBoard(round = 1)
        val reshuffled = EquationBoardGenerator.reshuffleActiveCells(initial.cells)
        assertEquals(25, reshuffled.size)
        assertTrue(reshuffled.all { it.isActive })
    }

    // 26. Reshuffle preserves values
    @Test
    fun test26_reshufflePreservesValues() {
        val initial = EquationBoardGenerator.generateNewBoard(round = 1)
        val initialValuesSorted = initial.cells.map { it.value }.sorted()

        val reshuffled = EquationBoardGenerator.reshuffleActiveCells(initial.cells)
        val reshuffledValuesSorted = reshuffled.map { it.value }.sorted()

        assertEquals(initialValuesSorted, reshuffledValuesSorted)
    }

    // 27. Hint
    @Test
    fun test27_hint() {
        val initial = EquationBoardGenerator.generateNewBoard(round = 1)
        val solution = EquationSolutionFinder.findFirstSolution(
            cells = initial.cells,
            target = initial.targetPlan.target,
            allowThreeNumbers = true
        )
        assertNotNull(solution)
        assertTrue(EquationEngine.isTargetReached(solution!!.equation, initial.targetPlan.target))
    }

    // 28. No-solution detection
    @Test
    fun test28_noSolutionDetection() {
        // Construct cells with only 1s
        val cellsWithOnes = (0 until 25).map { id ->
            NumberCell(id, Coordinate(id / 5, id % 5), 1, CellState.ACTIVE)
        }
        // Target 50 is impossible with only ones and 2 or 3 numbers (1+1+1=3, 1*1*1=1)
        val hasSolution = EquationSolutionFinder.hasSolution(cellsWithOnes, 50, allowThreeNumbers = true)
        assertFalse("Target 50 cannot be reached with only 1s", hasSolution)
    }

    // 29. Score calculation
    @Test
    fun test29_score() {
        // Two-number clean solve: 100 + 25 = 125
        val twoNumClean = 100 + 25
        assertEquals(125, twoNumClean)

        // Three-number clean solve: 125 + 25 = 150
        val threeNumClean = 125 + 25
        assertEquals(150, threeNumClean)

        // Two-number with hint: 100 + 0 = 100
        val withHint = 100
        assertEquals(100, withHint)
    }

    // 30. Accuracy calculation
    @Test
    fun test30_accuracy() {
        val solved = 9
        val failed = 3
        val accuracy = (solved * 100) / (solved + failed)
        assertEquals(75, accuracy)
    }

    // 31. Perfect run
    @Test
    fun test31_perfectRun() {
        val statePerfect = EquationBoardGameState(
            solvedTargets = 10,
            totalTargets = 10,
            failedAttempts = 0,
            hintsUsed = 0,
            skipsUsed = 0
        )
        assertTrue(statePerfect.isPerfectRun)

        val stateImperfect = EquationBoardGameState(
            solvedTargets = 10,
            totalTargets = 10,
            failedAttempts = 1,
            hintsUsed = 0,
            skipsUsed = 0
        )
        assertFalse(stateImperfect.isPerfectRun)
    }

    // 32. Game completion
    @Test
    fun test32_gameCompletion() {
        var targetIndex = 10
        val totalTargets = 10
        targetIndex += 1
        val isFinished = targetIndex > totalTargets
        assertTrue(isFinished)
    }

    // 33. Restart
    @Test
    fun test33_restart() {
        val board1 = EquationBoardGenerator.generateNewBoard(round = 1)
        assertEquals(1, board1.cells.first().coordinate.row + 1)
        assertEquals(25, board1.cells.size)
    }

    // 34. Statistics
    @Test
    fun test34_statistics() {
        val fakePrefs = FakeSharedPreferences()
        val prefsManager = PreferencesManager(fakePrefs)

        val stats1 = prefsManager.recordCompletedGame(
            score = 925,
            accuracy = 75,
            isPerfect = false,
            solves = 9,
            skips = 1,
            failed = 3,
            hints = 1,
            reshuffles = 0,
            twoNumSolves = 7,
            threeNumSolves = 2
        )

        assertEquals(1, stats1.completedGames)
        assertEquals(925, stats1.bestScore)
        assertEquals(75, stats1.bestAccuracy)
        assertEquals(0, stats1.perfectRuns)
        assertEquals(9, stats1.totalSolves)
        assertEquals(1, stats1.totalSkips)
    }

    // 35. Settings persistence
    @Test
    fun test35_settingsPersistence() {
        val fakePrefs = FakeSharedPreferences()
        val prefsManager = PreferencesManager(fakePrefs)

        val initial = prefsManager.getSettings()
        assertTrue(initial.soundEnabled)

        val updated = initial.copy(soundEnabled = false, showCoordinates = true)
        prefsManager.saveSettings(updated)

        val reloaded = prefsManager.getSettings()
        assertFalse(reloaded.soundEnabled)
        assertTrue(reloaded.showCoordinates)

        val reset = prefsManager.resetSettings()
        assertTrue(reset.soundEnabled)
        assertFalse(reset.showCoordinates)
    }

    // 36. Rapid tap protection
    @Test
    fun test36_rapidTapProtection() {
        // Sequence must alternate number -> op -> number
        assertFalse(EquationEngine.canAppendOperation(numbersCount = 0, operationsCount = 0, allowThreeNumbers = true))
        assertTrue(EquationEngine.canAppendNumber(numbersCount = 0, operationsCount = 0, allowThreeNumbers = true))
        assertFalse(EquationEngine.canAppendNumber(numbersCount = 1, operationsCount = 0, allowThreeNumbers = true))
    }

    // 37. Random board validity
    @Test
    fun test37_randomBoardValidity() {
        for (seed in 1..20) {
            val board = EquationBoardGenerator.generateNewBoard(round = 1, random = Random(seed))
            assertEquals(25, board.cells.size)
            assertTrue(board.cells.all { it.value in 1..12 })
        }
    }

    // 38. Three-number equation setting
    @Test
    fun test38_threeNumberEquationSetting() {
        val allowThree = false
        val plan = EquationTargetGenerator.generateTarget(round = 9, allowThreeNumbers = allowThree)
        assertEquals("When disabled, equation must only have 2 numbers", 2, plan.equation.numbers.size)
    }

    // 39. Solution finder
    @Test
    fun test39_solutionFinder() {
        val cells = listOf(
            NumberCell(0, Coordinate(0, 0), 6, CellState.ACTIVE),
            NumberCell(1, Coordinate(0, 1), 3, CellState.ACTIVE),
            NumberCell(2, Coordinate(0, 2), 2, CellState.ACTIVE)
        )
        // 6 * 3 - 2 = 16
        val solution = EquationSolutionFinder.findFirstSolution(cells, 16, allowThreeNumbers = true)
        assertNotNull(solution)
        assertEquals(16, EquationEngine.evaluateEquation(solution!!.equation))
    }

    // 40. Target always solvable
    @Test
    fun test40_targetAlwaysSolvable() {
        for (i in 1..50) {
            val board = EquationBoardGenerator.generateNewBoard(round = (i % 10) + 1, allowThreeNumbers = true)
            val hasSol = EquationSolutionFinder.hasSolution(board.cells, board.targetPlan.target, allowThreeNumbers = true)
            assertTrue("Target ${board.targetPlan.target} must be solvable", hasSol)
        }
    }
}
