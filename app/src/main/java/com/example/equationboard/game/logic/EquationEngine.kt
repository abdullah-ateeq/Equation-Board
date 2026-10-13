package com.example.equationboard.game.logic

import com.example.equationboard.game.model.Equation
import com.example.equationboard.game.model.Operation

object EquationEngine {

    /**
     * Evaluates an equation with standard mathematical precedence.
     * Prevents negative intermediate and final results.
     * Returns null if equation is incomplete, malformed, or produces negative values.
     */
    fun evaluateEquation(
        numbers: List<Int>,
        operations: List<Operation>
    ): Int? {
        if (numbers.isEmpty()) return null
        if (operations.size != numbers.size - 1) return null

        when (numbers.size) {
            1 -> return numbers[0]
            2 -> {
                val a = numbers[0]
                val b = numbers[1]
                return when (operations[0]) {
                    Operation.ADD -> a + b
                    Operation.SUBTRACT -> {
                        val result = a - b
                        if (result < 0) null else result
                    }
                    Operation.MULTIPLY -> a * b
                }
            }
            3 -> {
                val a = numbers[0]
                val b = numbers[1]
                val c = numbers[2]
                val op1 = operations[0]
                val op2 = operations[1]

                // Standard operator precedence: MULTIPLY precedes ADD and SUBTRACT
                return if (op1 == Operation.MULTIPLY && op2 == Operation.MULTIPLY) {
                    a * b * c
                } else if (op1 == Operation.MULTIPLY) {
                    val first = a * b
                    when (op2) {
                        Operation.ADD -> first + c
                        Operation.SUBTRACT -> {
                            val res = first - c
                            if (res < 0) null else res
                        }
                        Operation.MULTIPLY -> first * c
                    }
                } else if (op2 == Operation.MULTIPLY) {
                    val mult = b * c
                    when (op1) {
                        Operation.ADD -> a + mult
                        Operation.SUBTRACT -> {
                            val res = a - mult
                            if (res < 0) null else res
                        }
                        Operation.MULTIPLY -> mult * a
                    }
                } else {
                    // Both are ADD or SUBTRACT: evaluate strictly left-to-right
                    val first = when (op1) {
                        Operation.ADD -> a + b
                        Operation.SUBTRACT -> {
                            val res = a - b
                            if (res < 0) return null
                            res
                        }
                        Operation.MULTIPLY -> a * b
                    }

                    when (op2) {
                        Operation.ADD -> first + c
                        Operation.SUBTRACT -> {
                            val res = first - c
                            if (res < 0) null else res
                        }
                        Operation.MULTIPLY -> first * c
                    }
                }
            }
            else -> return null
        }
    }

    fun evaluateEquation(equation: Equation): Int? {
        return evaluateEquation(equation.numbers, equation.operations)
    }

    fun isTargetReached(equation: Equation, target: Int): Boolean {
        if (!equation.isComplete) return false
        val result = evaluateEquation(equation) ?: return false
        return result == target
    }

    /**
     * Helper to validate if an operation can be legally appended.
     * Prevents consecutive operations or starting with an operation.
     */
    fun canAppendOperation(
        numbersCount: Int,
        operationsCount: Int,
        allowThreeNumbers: Boolean
    ): Boolean {
        if (numbersCount == 0) return false
        if (operationsCount >= numbersCount) return false
        val maxOperations = if (allowThreeNumbers) 2 else 1
        return operationsCount < maxOperations
    }

    /**
     * Helper to validate if a number can be appended.
     */
    fun canAppendNumber(
        numbersCount: Int,
        operationsCount: Int,
        allowThreeNumbers: Boolean
    ): Boolean {
        val maxNumbers = if (allowThreeNumbers) 3 else 2
        if (numbersCount >= maxNumbers) return false
        return numbersCount == operationsCount
    }
}
