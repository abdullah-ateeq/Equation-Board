package com.example.equationboard.game.model

enum class Operation(
    val symbol: String,
    val displayName: String
) {
    ADD("+", "Addition"),
    SUBTRACT("−", "Subtraction"),
    MULTIPLY("×", "Multiplication")
}
