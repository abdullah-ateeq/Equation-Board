package com.example.equationboard.game.model

data class Coordinate(
    val row: Int,
    val column: Int
) {
    val label: String
        get() = "${('A' + column)}${row + 1}"
}
