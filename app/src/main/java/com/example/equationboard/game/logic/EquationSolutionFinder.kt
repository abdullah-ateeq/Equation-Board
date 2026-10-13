package com.example.equationboard.game.logic

import com.example.equationboard.game.model.Equation
import com.example.equationboard.game.model.NumberCell
import com.example.equationboard.game.model.Operation

data class EquationSolution(
    val equation: Equation,
    val cellIds: List<Int>
)

object EquationSolutionFinder {

    /**
     * Checks if the target is reachable using active cells.
     */
    fun hasSolution(
        cells: List<NumberCell>,
        target: Int,
        allowThreeNumbers: Boolean = true
    ): Boolean {
        return findFirstSolution(cells, target, allowThreeNumbers) != null
    }

    /**
     * Finds the first valid solution, preferring simpler 2-number equations.
     */
    fun findFirstSolution(
        cells: List<NumberCell>,
        target: Int,
        allowThreeNumbers: Boolean = true
    ): EquationSolution? {
        val activeCells = cells.filter { it.isActive }
        if (activeCells.size < 2) return null

        // 1. Try 2-number equations first
        val operations = listOf(Operation.ADD, Operation.SUBTRACT, Operation.MULTIPLY)
        for (i in activeCells.indices) {
            val cell1 = activeCells[i]
            for (j in activeCells.indices) {
                if (i == j) continue
                val cell2 = activeCells[j]

                for (op in operations) {
                    val eq = Equation(listOf(cell1.value, cell2.value), listOf(op))
                    if (EquationEngine.isTargetReached(eq, target)) {
                        return EquationSolution(eq, listOf(cell1.id, cell2.id))
                    }
                }
            }
        }

        // 2. Try 3-number equations if allowed
        if (allowThreeNumbers && activeCells.size >= 3) {
            // Check clean operation combinations first
            val cleanOpPairs = listOf(
                Pair(Operation.MULTIPLY, Operation.SUBTRACT),
                Pair(Operation.MULTIPLY, Operation.ADD),
                Pair(Operation.ADD, Operation.ADD),
                Pair(Operation.SUBTRACT, Operation.ADD),
                Pair(Operation.ADD, Operation.SUBTRACT),
                Pair(Operation.MULTIPLY, Operation.MULTIPLY)
            )

            for (i in activeCells.indices) {
                val cell1 = activeCells[i]
                for (j in activeCells.indices) {
                    if (i == j) continue
                    val cell2 = activeCells[j]
                    for (k in activeCells.indices) {
                        if (k == i || k == j) continue
                        val cell3 = activeCells[k]

                        for ((op1, op2) in cleanOpPairs) {
                            val eq = Equation(
                                listOf(cell1.value, cell2.value, cell3.value),
                                listOf(op1, op2)
                            )
                            if (EquationEngine.isTargetReached(eq, target)) {
                                return EquationSolution(
                                    eq,
                                    listOf(cell1.id, cell2.id, cell3.id)
                                )
                            }
                        }
                    }
                }
            }
        }

        return null
    }

    /**
     * Finds all valid distinct equation solutions up to a given limit.
     */
    fun findAllSolutions(
        cells: List<NumberCell>,
        target: Int,
        allowThreeNumbers: Boolean = true,
        limit: Int = 20
    ): List<EquationSolution> {
        val solutions = mutableListOf<EquationSolution>()
        val seenSignatures = mutableSetOf<String>()
        val activeCells = cells.filter { it.isActive }
        if (activeCells.size < 2) return emptyList()

        val operations = listOf(Operation.ADD, Operation.SUBTRACT, Operation.MULTIPLY)

        // 2-number solutions
        for (i in activeCells.indices) {
            val cell1 = activeCells[i]
            for (j in activeCells.indices) {
                if (i == j) continue
                val cell2 = activeCells[j]

                for (op in operations) {
                    val eq = Equation(listOf(cell1.value, cell2.value), listOf(op))
                    if (EquationEngine.isTargetReached(eq, target)) {
                        val signature = "${cell1.value}${op.symbol}${cell2.value}"
                        if (seenSignatures.add(signature)) {
                            solutions.add(EquationSolution(eq, listOf(cell1.id, cell2.id)))
                            if (solutions.size >= limit) return solutions
                        }
                    }
                }
            }
        }

        // 3-number solutions
        if (allowThreeNumbers && activeCells.size >= 3) {
            val opPairs = listOf(
                Pair(Operation.MULTIPLY, Operation.SUBTRACT),
                Pair(Operation.MULTIPLY, Operation.ADD),
                Pair(Operation.ADD, Operation.ADD),
                Pair(Operation.SUBTRACT, Operation.ADD),
                Pair(Operation.ADD, Operation.SUBTRACT),
                Pair(Operation.MULTIPLY, Operation.MULTIPLY)
            )

            for (i in activeCells.indices) {
                val cell1 = activeCells[i]
                for (j in activeCells.indices) {
                    if (i == j) continue
                    val cell2 = activeCells[j]
                    for (k in activeCells.indices) {
                        if (k == i || k == j) continue
                        val cell3 = activeCells[k]

                        for ((op1, op2) in opPairs) {
                            val eq = Equation(
                                listOf(cell1.value, cell2.value, cell3.value),
                                listOf(op1, op2)
                            )
                            if (EquationEngine.isTargetReached(eq, target)) {
                                val signature = "${cell1.value}${op1.symbol}${cell2.value}${op2.symbol}${cell3.value}"
                                if (seenSignatures.add(signature)) {
                                    solutions.add(
                                        EquationSolution(
                                            eq,
                                            listOf(cell1.id, cell2.id, cell3.id)
                                        )
                                    )
                                    if (solutions.size >= limit) return solutions
                                }
                            }
                        }
                    }
                }
            }
        }

        return solutions
    }
}
