package com.example.equationboard.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.equationboard.data.SettingsState
import com.example.equationboard.game.EquationBoardViewModel
import com.example.equationboard.game.model.EquationBoardGameState
import com.example.equationboard.game.model.GamePhase
import com.example.equationboard.game.model.Operation
import com.example.equationboard.ui.components.EquationPreviewBar
import com.example.equationboard.ui.components.NumberTile
import com.example.equationboard.ui.components.OperationControls
import com.example.equationboard.ui.components.TargetCard
import com.example.equationboard.util.HapticHelper

@Composable
fun GameScreen(
    viewModel: EquationBoardViewModel,
    gameState: EquationBoardGameState,
    settings: SettingsState,
    onNavigateToResult: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    var showRestartDialog by remember { mutableStateOf(false) }

    // Navigate to Result when game is complete
    LaunchedEffect(gameState.phase) {
        if (gameState.phase == GamePhase.GAME_COMPLETE) {
            onNavigateToResult()
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Restart Game") },
            text = { Text("Are you sure you want to restart this game? Current progress will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        viewModel.startNewGame()
                    }
                ) {
                    Text("Restart", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLandscape) {
            // Landscape layout: Board on left, Controls on right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Left side: Board
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardGrid(
                        gameState = gameState,
                        settings = settings,
                        onCellClick = { cellId ->
                            viewModel.selectCell(cellId)
                            HapticHelper.performLight(hapticFeedback, settings.hapticFeedback)
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Right side: Controls & Targets
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameHeader(
                        gameState = gameState,
                        onRestartClick = {
                            if (settings.confirmRestart) {
                                showRestartDialog = true
                            } else {
                                viewModel.startNewGame()
                            }
                        }
                    )

                    TargetCard(
                        target = gameState.target,
                        phase = gameState.phase,
                        animationsEnabled = settings.animationsEnabled
                    )

                    EquationPreviewBar(
                        equation = gameState.currentEquation,
                        target = gameState.target,
                        phase = gameState.phase,
                        feedbackMessage = gameState.feedbackMessage,
                        animationsEnabled = settings.animationsEnabled
                    )

                    val canSelectOp = gameState.selectedCellIds.isNotEmpty() &&
                            gameState.operations.size < gameState.selectedCellIds.size &&
                            gameState.operations.size < if (settings.threeNumberEquations) 2 else 1

                    OperationControls(
                        canSelectOperation = canSelectOp,
                        onOperationSelect = { op ->
                            viewModel.selectOperation(op)
                            HapticHelper.performLight(hapticFeedback, settings.hapticFeedback)
                        }
                    )

                    GameActionButtons(
                        viewModel = viewModel,
                        gameState = gameState,
                        settings = settings,
                        onCheckClick = {
                            viewModel.checkEquation()
                            if (gameState.currentEquation != null &&
                                gameState.currentEquation.isComplete
                            ) {
                                HapticHelper.performMedium(hapticFeedback, settings.hapticFeedback)
                            }
                        }
                    )
                }
            }
        } else {
            // Portrait phone layout: Compact vertical stack
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section: Header, Target, Equation preview, Operations
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameHeader(
                        gameState = gameState,
                        onRestartClick = {
                            if (settings.confirmRestart) {
                                showRestartDialog = true
                            } else {
                                viewModel.startNewGame()
                            }
                        }
                    )

                    TargetCard(
                        target = gameState.target,
                        phase = gameState.phase,
                        animationsEnabled = settings.animationsEnabled
                    )

                    EquationPreviewBar(
                        equation = gameState.currentEquation,
                        target = gameState.target,
                        phase = gameState.phase,
                        feedbackMessage = gameState.feedbackMessage,
                        animationsEnabled = settings.animationsEnabled
                    )

                    val canSelectOp = gameState.selectedCellIds.isNotEmpty() &&
                            gameState.operations.size < gameState.selectedCellIds.size &&
                            gameState.operations.size < if (settings.threeNumberEquations) 2 else 1

                    OperationControls(
                        canSelectOperation = canSelectOp,
                        onOperationSelect = { op ->
                            viewModel.selectOperation(op)
                            HapticHelper.performLight(hapticFeedback, settings.hapticFeedback)
                        }
                    )
                }

                // Middle section: 5x5 Board Grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardGrid(
                        gameState = gameState,
                        settings = settings,
                        onCellClick = { cellId ->
                            viewModel.selectCell(cellId)
                            HapticHelper.performLight(hapticFeedback, settings.hapticFeedback)
                        }
                    )
                }

                // Bottom section: Check button and action controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameActionButtons(
                        viewModel = viewModel,
                        gameState = gameState,
                        settings = settings,
                        onCheckClick = {
                            viewModel.checkEquation()
                            HapticHelper.performMedium(hapticFeedback, settings.hapticFeedback)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameHeader(
    gameState: EquationBoardGameState,
    onRestartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "TARGET ${gameState.targetIndex} / ${gameState.totalTargets}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "SCORE: ${gameState.score}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SOLVED: ${gameState.solvedTargets}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "SKIPS: ${gameState.skipsUsed}/${gameState.maxSkips}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onRestartClick,
                modifier = Modifier.semantics {
                    this.contentDescription = "Restart game"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GameBoardGrid(
    gameState: EquationBoardGameState,
    settings: SettingsState,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val boardSize = minOf(maxWidth, if (maxHeight > 0.dp) maxHeight else maxWidth)

        Box(
            modifier = Modifier
                .size(boardSize)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) {
                items(gameState.cells, key = { it.id }) { cell ->
                    NumberTile(
                        cell = cell,
                        showCoordinates = settings.showCoordinates,
                        animationsEnabled = settings.animationsEnabled,
                        onCellClick = onCellClick
                    )
                }
            }
        }
    }
}

@Composable
private fun GameActionButtons(
    viewModel: EquationBoardViewModel,
    gameState: EquationBoardGameState,
    settings: SettingsState,
    onCheckClick: () -> Unit
) {
    val isComplete = gameState.currentEquation?.isComplete == true

    // Check Equation primary button
    Button(
        onClick = onCheckClick,
        enabled = isComplete && gameState.phase == GamePhase.BUILDING,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .semantics {
                this.contentDescription = "Check equation"
            }
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = "CHECK EQUATION",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
    }

    // Secondary buttons row: CLEAR, HINT, SKIP, RESHUFFLE
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val buttonPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)

        // Clear
        OutlinedButton(
            onClick = { viewModel.clearEquation() },
            enabled = gameState.selectedCellIds.isNotEmpty() && gameState.phase == GamePhase.BUILDING,
            contentPadding = buttonPadding,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .semantics {
                    this.contentDescription = "Clear equation"
                }
        ) {
            Text(
                text = "CLEAR",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }

        // Hint (if enabled in settings)
        if (settings.showHints) {
            FilledTonalButton(
                onClick = { viewModel.useHint() },
                enabled = !gameState.hintUsedForCurrentTarget && gameState.phase == GamePhase.BUILDING,
                contentPadding = buttonPadding,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .semantics {
                        this.contentDescription = "Show hint"
                    }
            ) {
                Text(
                    text = if (gameState.hintUsedForCurrentTarget) "HINT USED" else "HINT",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
            }
        }

        // Skip
        OutlinedButton(
            onClick = { viewModel.skipTarget() },
            enabled = gameState.skipsUsed < gameState.maxSkips && gameState.phase == GamePhase.BUILDING,
            contentPadding = buttonPadding,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .semantics {
                    this.contentDescription = "Skip target"
                }
        ) {
            Text(
                text = "SKIP",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }

        // Reshuffle
        OutlinedButton(
            onClick = { viewModel.reshuffleBoard() },
            enabled = gameState.reshufflesUsed < gameState.maxReshuffles && gameState.phase == GamePhase.BUILDING,
            contentPadding = buttonPadding,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .semantics {
                    this.contentDescription = "Reshuffle board"
                }
        ) {
            Text(
                text = "SHUFFLE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }
    }
}
