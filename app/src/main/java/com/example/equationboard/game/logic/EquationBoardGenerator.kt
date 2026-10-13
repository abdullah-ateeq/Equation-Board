package com.example.equationboard.game.logic

import com.example.equationboard.game.model.CellState
import com.example.equationboard.game.model.Coordinate
import com.example.equationboard.game.model.NumberCell
import com.example.equationboard.game.model.Operation
import kotlin.random.Random

data class InitialBoardResult(
    val cells: List<NumberCell>,
    val targetPlan: TargetPlan
)

object EquationBoardGenerator {

    const val BOARD_ROWS = 5
    const val BOARD_COLS = 5
    const val TOTAL_CELLS = BOARD_ROWS * BOARD_COLS

    /**
     * Generates a fresh 5x5 board with 25 cells and a guaranteed solvable target.
     */
    fun generateNewBoard(
        round: Int = 1,
        allowThreeNumbers: Boolean = true,
        random: Random = Random.Default
    ): InitialBoardResult {
        val targetPlan = EquationTargetGenerator.generateTarget(round, allowThreeNumbers, random)
        val values = mutableListOf<Int>()
        values.addAll(targetPlan.requiredNumbers)

        while (values.size < TOTAL_CELLS) {
            values.add(random.nextInt(1, 13))
        }

        // Shuffle so required numbers are dispersed across the board
        values.shuffle(random)

        val cells = mutableListOf<NumberCell>()
        var id = 0
        for (r in 0 until BOARD_ROWS) {
            for (c in 0 until BOARD_COLS) {
                cells.add(
                    NumberCell(
                        id = id,
                        coordinate = Coordinate(row = r, column = c),
                        value = values[id],
                        state = CellState.ACTIVE
                    )
                )
                id++
            }
        }

        // Double check reachability
        if (!EquationSolutionFinder.hasSolution(cells, targetPlan.target, allowThreeNumbers)) {
            // Guarantee by inserting required numbers into random positions
            val positions = (0 until TOTAL_CELLS).shuffled(random).take(targetPlan.requiredNumbers.size)
            val updatedCells = cells.toMutableList()
            for (i in targetPlan.requiredNumbers.indices) {
                val pos = positions[i]
                updatedCells[pos] = updatedCells[pos].copy(value = targetPlan.requiredNumbers[i])
            }
            return InitialBoardResult(updatedCells, targetPlan)
        }

        return InitialBoardResult(cells, targetPlan)
    }

    /**
     * Reshuffles the positions of all non-empty cells while strictly preserving their values.
     */
    fun reshuffleActiveCells(
        cells: List<NumberCell>,
        random: Random = Random.Default
    ): List<NumberCell> {
        val nonEmptyCells = cells.filter { it.state != CellState.EMPTY }
        val values = nonEmptyCells.map { it.value }.shuffled(random)

        val result = cells.toMutableList()
        var valIndex = 0
        for (i in result.indices) {
            if (result[i].state != CellState.EMPTY) {
                result[i] = result[i].copy(
                    value = values[valIndex++],
                    state = CellState.ACTIVE // Reset any selection to ACTIVE
                )
            }
        }
        return result
    }

    /**
     * Refills empty (or consuming) cells with new values (1..12),
     * ensuring that the given target is solvable on the board.
     */
    fun refillEmptyCells(
        cells: List<NumberCell>,
        target: Int,
        targetPlan: TargetPlan? = null,
        allowThreeNumbers: Boolean = true,
        random: Random = Random.Default
    ): List<NumberCell> {
        val emptyIndices = cells.indices.filter {
            cells[it].state == CellState.EMPTY || cells[it].state == CellState.CONSUMING
        }

        if (emptyIndices.isEmpty()) {
            return cells
        }

        val updated = cells.toMutableList()

        // First, fill empty cells with random values in 1..12
        for (idx in emptyIndices) {
            updated[idx] = updated[idx].copy(
                value = random.nextInt(1, 13),
                state = CellState.ACTIVE
            )
        }

        // Check if board already has a solution for target
        if (EquationSolutionFinder.hasSolution(updated, target, allowThreeNumbers)) {
            return updated
        }

        // If not solvable, insert required numbers into the empty slots
        val required = targetPlan?.requiredNumbers ?: listOf(
            target.coerceIn(1, 12),
            0 // will be adjusted
        )

        if (targetPlan != null && emptyIndices.size >= targetPlan.requiredNumbers.size) {
            for (i in targetPlan.requiredNumbers.indices) {
                val slot = emptyIndices[i]
                updated[slot] = updated[slot].copy(
                    value = targetPlan.requiredNumbers[i],
                    state = CellState.ACTIVE
                )
            }
        } else {
            // Find any way to place a 2-number solution in the empty slots
            val numSlots = emptyIndices.size
            if (numSlots >= 2) {
                val pair = findPairForTarget(target)
                if (pair != null) {
                    updated[emptyIndices[0]] = updated[emptyIndices[0]].copy(value = pair.first, state = CellState.ACTIVE)
                    updated[emptyIndices[1]] = updated[emptyIndices[1]].copy(value = pair.second, state = CellState.ACTIVE)
                }
            } else if (numSlots == 1) {
                // Try combining with an existing active cell
                val slot = emptyIndices[0]
                val existingActive = updated.filterIndexed { index, _ -> index != slot && updated[index].isActive }
                var solved = false
                for (active in existingActive) {
                    val needed = target - active.value
                    if (needed in 1..12) {
                        updated[slot] = updated[slot].copy(value = needed, state = CellState.ACTIVE)
                        solved = true
                        break
                    }
                    val diff = active.value - target
                    if (diff in 1..12) {
                        updated[slot] = updated[slot].copy(value = diff, state = CellState.ACTIVE)
                        solved = true
                        break
                    }
                    if (target % active.value == 0 && (target / active.value) in 1..12) {
                        updated[slot] = updated[slot].copy(value = target / active.value, state = CellState.ACTIVE)
                        solved = true
                        break
                    }
                }
                if (!solved) {
                    // Default fallback
                    updated[slot] = updated[slot].copy(value = random.nextInt(1, 13), state = CellState.ACTIVE)
                }
            }
        }

        return updated
    }

    private fun findPairForTarget(target: Int): Pair<Int, Int>? {
        // Try multiplication
        for (a in 1..12) {
            if (target % a == 0) {
                val b = target / a
                if (b in 1..12) return Pair(a, b)
            }
        }
        // Try addition
        for (a in 1..12) {
            val b = target - a
            if (b in 1..12) return Pair(a, b)
        }
        // Try subtraction
        for (a in (target + 1)..12) {
            val b = a - target
            if (b in 1..12) return Pair(a, b)
        }
        return null
    }
}
