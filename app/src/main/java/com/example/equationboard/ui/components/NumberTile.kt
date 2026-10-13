package com.example.equationboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.equationboard.game.model.CellState
import com.example.equationboard.game.model.NumberCell

@Composable
fun NumberTile(
    cell: NumberCell,
    showCoordinates: Boolean,
    animationsEnabled: Boolean,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = cell.isSelected
    val isConsuming = cell.isConsuming
    val isEmpty = cell.isEmpty

    val scale by animateFloatAsState(
        targetValue = when {
            !animationsEnabled -> 1f
            isSelected -> 1.06f
            isConsuming -> 0.7f
            isEmpty -> 0.95f
            else -> 1f
        },
        animationSpec = tween(durationMillis = if (animationsEnabled) 200 else 0),
        label = "tileScale"
    )

    val alpha by animateFloatAsState(
        targetValue = when {
            isConsuming -> 0.2f
            isEmpty -> 0.25f
            else -> 1f
        },
        animationSpec = tween(durationMillis = if (animationsEnabled) 250 else 0),
        label = "tileAlpha"
    )

    val containerColor = when (cell.state) {
        CellState.SELECTED -> MaterialTheme.colorScheme.primaryContainer
        CellState.ACTIVE -> MaterialTheme.colorScheme.surface
        CellState.CONSUMING -> MaterialTheme.colorScheme.surfaceVariant
        CellState.EMPTY -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val contentColor = when (cell.state) {
        CellState.SELECTED -> MaterialTheme.colorScheme.onPrimaryContainer
        CellState.ACTIVE -> MaterialTheme.colorScheme.onSurface
        CellState.CONSUMING -> MaterialTheme.colorScheme.onSurfaceVariant
        CellState.EMPTY -> Color.Transparent
    }

    val border = when (cell.state) {
        CellState.SELECTED -> BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
        CellState.ACTIVE -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        CellState.EMPTY -> BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        CellState.CONSUMING -> null
    }

    val elevation = if (isSelected) 6.dp else if (cell.isActive) 2.dp else 0.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .alpha(alpha)
            .semantics {
                this.contentDescription = cell.contentDescription
            }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    enabled = cell.isActive,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCellClick(cell.id) }
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (showCoordinates && !isEmpty) {
                    Text(
                        text = cell.coordinate.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = contentColor.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 4.dp, top = 2.dp)
                    )
                }

                if (!isEmpty) {
                    Text(
                        text = cell.value.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}
