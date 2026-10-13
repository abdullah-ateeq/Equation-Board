package com.example.equationboard.game.logic

import com.example.equationboard.game.model.Equation
import com.example.equationboard.game.model.Operation
import kotlin.random.Random

data class TargetPlan(
    val target: Int,
    val equation: Equation,
    val requiredNumbers: List<Int>
)

object EquationTargetGenerator {

    /**
     * Generates a target and a guaranteed equation for a given round.
     * Round 1-3: Addition / Subtraction
     * Round 4-7: Multiplication / Mixed
     * Round 8-10: Three-number equations (if allowed) or higher combinations
     */
    fun generateTarget(
        round: Int,
        allowThreeNumbers: Boolean = true,
        random: Random = Random.Default
    ): TargetPlan {
        val clampedRound = round.coerceIn(1, 10)

        return when {
            clampedRound in 1..3 -> generateSimpleAdditionOrSubtraction(random)
            clampedRound in 4..7 -> {
                // 70% multiplication, 30% addition/subtraction
                if (random.nextFloat() < 0.7f) {
                    generateMultiplication(random)
                } else {
                    generateSimpleAdditionOrSubtraction(random)
                }
            }
            else -> {
                // Round 8-10
                if (allowThreeNumbers && random.nextFloat() < 0.75f) {
                    generateThreeNumberEquation(random)
                } else {
                    generateMultiplication(random)
                }
            }
        }
    }

    private fun generateSimpleAdditionOrSubtraction(random: Random): TargetPlan {
        val isAddition = random.nextBoolean()
        return if (isAddition) {
            // Target 5-24
            val a = random.nextInt(1, 13)
            val b = random.nextInt(1, 13)
            val sum = a + b
            // Ensure target is at least 5
            if (sum < 5) {
                val adjustedB = random.nextInt(4, 13)
                val newSum = a + adjustedB
                TargetPlan(
                    target = newSum,
                    equation = Equation(listOf(a, adjustedB), listOf(Operation.ADD)),
                    requiredNumbers = listOf(a, adjustedB)
                )
            } else {
                TargetPlan(
                    target = sum,
                    equation = Equation(listOf(a, b), listOf(Operation.ADD)),
                    requiredNumbers = listOf(a, b)
                )
            }
        } else {
            // Target 1-11
            val a = random.nextInt(3, 13)
            val b = random.nextInt(1, a)
            val diff = a - b
            TargetPlan(
                target = diff,
                equation = Equation(listOf(a, b), listOf(Operation.SUBTRACT)),
                requiredNumbers = listOf(a, b)
            )
        }
    }

    private fun generateMultiplication(random: Random): TargetPlan {
        // Target 4-60 with a, b in 1..12
        val candidates = mutableListOf<Pair<Int, Int>>()
        for (a in 2..12) {
            for (b in 2..12) {
                val prod = a * b
                if (prod in 4..60) {
                    candidates.add(Pair(a, b))
                }
            }
        }
        val (a, b) = candidates[random.nextInt(candidates.size)]
        return TargetPlan(
            target = a * b,
            equation = Equation(listOf(a, b), listOf(Operation.MULTIPLY)),
            requiredNumbers = listOf(a, b)
        )
    }

    private fun generateThreeNumberEquation(random: Random): TargetPlan {
        // Preferred clean forms:
        // 1: a * b - c (target in 5..60)
        // 2: a * b + c (target in 5..60)
        // 3: a + b + c (target in 5..36)
        // 4: a - b + c (target in 5..24)
        val form = random.nextInt(4)
        return when (form) {
            0 -> {
                // a * b - c
                var a = random.nextInt(2, 9)
                var b = random.nextInt(2, 9)
                var c = random.nextInt(1, 13)
                var prod = a * b
                while (prod <= c || (prod - c) !in 5..60) {
                    a = random.nextInt(2, 9)
                    b = random.nextInt(2, 9)
                    c = random.nextInt(1, 13)
                    prod = a * b
                }
                val res = prod - c
                TargetPlan(
                    target = res,
                    equation = Equation(listOf(a, b, c), listOf(Operation.MULTIPLY, Operation.SUBTRACT)),
                    requiredNumbers = listOf(a, b, c)
                )
            }
            1 -> {
                // a * b + c
                var a = random.nextInt(2, 8)
                var b = random.nextInt(2, 8)
                var c = random.nextInt(1, 13)
                while ((a * b + c) !in 5..60) {
                    a = random.nextInt(2, 8)
                    b = random.nextInt(2, 8)
                    c = random.nextInt(1, 13)
                }
                val res = a * b + c
                TargetPlan(
                    target = res,
                    equation = Equation(listOf(a, b, c), listOf(Operation.MULTIPLY, Operation.ADD)),
                    requiredNumbers = listOf(a, b, c)
                )
            }
            2 -> {
                // a + b + c
                val a = random.nextInt(1, 13)
                val b = random.nextInt(1, 13)
                val c = random.nextInt(1, 13)
                val sum = a + b + c
                TargetPlan(
                    target = sum,
                    equation = Equation(listOf(a, b, c), listOf(Operation.ADD, Operation.ADD)),
                    requiredNumbers = listOf(a, b, c)
                )
            }
            else -> {
                // a - b + c
                val a = random.nextInt(4, 13)
                val b = random.nextInt(1, a)
                val c = random.nextInt(1, 13)
                val res = (a - b) + c
                TargetPlan(
                    target = res,
                    equation = Equation(listOf(a, b, c), listOf(Operation.SUBTRACT, Operation.ADD)),
                    requiredNumbers = listOf(a, b, c)
                )
            }
        }
    }
}
