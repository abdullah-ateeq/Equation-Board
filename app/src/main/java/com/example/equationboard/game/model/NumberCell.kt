package com.example.equationboard.game.model

data class NumberCell(
    val id: Int,
    val coordinate: Coordinate,
    val value: Int,
    val state: CellState
) {
    val isActive: Boolean
        get() = state == CellState.ACTIVE

    val isSelected: Boolean
        get() = state == CellState.SELECTED

    val isEmpty: Boolean
        get() = state == CellState.EMPTY

    val isConsuming: Boolean
        get() = state == CellState.CONSUMING

    val contentDescription: String
        get() = when (state) {
            CellState.ACTIVE -> "Number $value at ${coordinate.label}"
            CellState.SELECTED -> "Selected number $value at ${coordinate.label}"
            CellState.CONSUMING -> "Consumed number $value at ${coordinate.label}"
            CellState.EMPTY -> "Empty cell at ${coordinate.label}"
        }
}
