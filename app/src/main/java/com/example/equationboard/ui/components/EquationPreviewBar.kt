package com.example.equationboard.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.equationboard.game.model.Equation
import com.example.equationboard.game.model.GamePhase
import com.example.equationboard.game.model.Operation
import com.example.equationboard.ui.theme.ErrorRed
import com.example.equationboard.ui.theme.SuccessGreen
import kotlin.math.roundToInt

@Composable
fun EquationPreviewBar(
    equation: Equation?,
    target: Int,
    phase: GamePhase,
    feedbackMessage: String?,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(phase) {
        if (animationsEnabled && phase == GamePhase.FAILED) {
            // Shake effect
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(12f, tween(50))
            shakeOffset.animateTo(-12f, tween(50))
            shakeOffset.animateTo(8f, tween(50))
            shakeOffset.animateTo(-8f, tween(50))
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    val isSuccess = phase == GamePhase.SUCCESS
    val isFailed = phase == GamePhase.FAILED

    val borderColor = when {
        isSuccess -> SuccessGreen
        isFailed -> ErrorRed
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val containerColor = when {
        isSuccess -> SuccessGreen.copy(alpha = 0.12f)
        isFailed -> ErrorRed.copy(alpha = 0.10f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val accessibilityDesc = equation?.toAccessibilityString(
        result = if (isSuccess) target else null
    ) ?: "Equation empty"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .semantics {
                this.contentDescription = accessibilityDesc
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tokens row: e.g. [ 6 ] [ × ] [ 4 ] [ = ] [ 24 ]
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (equation == null || equation.numbers.isEmpty()) {
                    Text(
                        text = "Select numbers and an operation",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else {
                    for (i in equation.numbers.indices) {
                        EquationToken(
                            text = equation.numbers[i].toString(),
                            isNumber = true
                        )

                        if (i < equation.operations.size) {
                            EquationToken(
                                text = equation.operations[i].symbol,
                                isNumber = false
                            )
                        }
                    }

                    // If waiting for next number after an operation
                    if (equation.operations.size == equation.numbers.size) {
                        EquationToken(
                            text = "?",
                            isNumber = true,
                            isPlaceholder = true
                        )
                    }

                    // Equals sign token
                    EquationToken(text = "=", isNumber = false)

                    // Target / Question mark token
                    EquationToken(
                        text = target.toString(),
                        isNumber = true,
                        isTarget = true
                    )
                }
            }

            // Status / Feedback text if any
            if (!feedbackMessage.isNullOrEmpty()) {
                val feedbackColor = when {
                    isSuccess -> SuccessGreen
                    isFailed -> ErrorRed
                    else -> MaterialTheme.colorScheme.primary
                }
                Text(
                    text = feedbackMessage,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = feedbackColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EquationToken(
    text: String,
    isNumber: Boolean,
    isPlaceholder: Boolean = false,
    isTarget: Boolean = false
) {
    val bg = when {
        isTarget -> MaterialTheme.colorScheme.primaryContainer
        isPlaceholder -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        isNumber -> MaterialTheme.colorScheme.surface
        else -> Color.Transparent
    }

    val fg = when {
        isTarget -> MaterialTheme.colorScheme.onPrimaryContainer
        isPlaceholder -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        isNumber -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isTarget || isNumber) FontWeight.Bold else FontWeight.Medium,
                fontSize = if (isNumber || isTarget) 18.sp else 16.sp
            ),
            color = fg
        )
    }
}
