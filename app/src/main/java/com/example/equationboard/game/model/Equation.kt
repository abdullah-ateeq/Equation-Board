package com.example.equationboard.game.model

data class Equation(
    val numbers: List<Int>,
    val operations: List<Operation>
) {
    val isComplete: Boolean
        get() = numbers.size >= 2 && numbers.size == operations.size + 1

    fun toDisplayString(showEquals: Boolean = true, result: Int? = null): String {
        val sb = StringBuilder()
        for (i in numbers.indices) {
            sb.append(numbers[i])
            if (i < operations.size) {
                sb.append(" ").append(operations[i].symbol).append(" ")
            }
        }
        if (showEquals) {
            sb.append(" = ")
            if (result != null) {
                sb.append(result)
            } else {
                sb.append("?")
            }
        }
        return sb.toString()
    }

    fun toAccessibilityString(result: Int? = null): String {
        val sb = StringBuilder()
        for (i in numbers.indices) {
            sb.append(numbers[i])
            if (i < operations.size) {
                val opName = when (operations[i]) {
                    Operation.ADD -> " plus "
                    Operation.SUBTRACT -> " minus "
                    Operation.MULTIPLY -> " times "
                }
                sb.append(opName)
            }
        }
        if (result != null) {
            sb.append(" equals ").append(result)
        }
        return sb.toString()
    }
}
