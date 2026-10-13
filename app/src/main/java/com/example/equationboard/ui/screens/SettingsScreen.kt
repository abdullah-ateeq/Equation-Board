package com.example.equationboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.equationboard.data.SettingsState

@Composable
fun SettingsScreen(
    settings: SettingsState,
    onSettingsChanged: (SettingsState) -> Unit,
    onResetSettings: () -> Unit,
    onResetStatistics: () -> Unit
) {
    var showResetStatsDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    if (showResetStatsDialog) {
        AlertDialog(
            onDismissRequest = { showResetStatsDialog = false },
            title = { Text("Reset Statistics") },
            text = { Text("Are you sure you want to reset all game statistics? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetStatsDialog = false
                        onResetStatistics()
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetStatsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Are you sure you want to restore all settings to default?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetSettingsDialog = false
                        onResetSettings()
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Theme Selection Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "THEME",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        SettingsState.THEME_SYSTEM to "System",
                        SettingsState.THEME_LIGHT to "Light",
                        SettingsState.THEME_DARK to "Dark"
                    )
                    for ((themeKey, label) in themes) {
                        FilterChip(
                            selected = settings.theme == themeKey,
                            onClick = { onSettingsChanged(settings.copy(theme = themeKey)) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gameplay & Display Preferences Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SettingSwitchRow(
                    title = "Dynamic Color",
                    subtitle = "Use Android system accent colors (Android 12+)",
                    checked = settings.dynamicColor,
                    onCheckedChange = { onSettingsChanged(settings.copy(dynamicColor = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingSwitchRow(
                    title = "Sound Effects",
                    subtitle = "Play tactile audio feedback during actions",
                    checked = settings.soundEnabled,
                    onCheckedChange = { onSettingsChanged(settings.copy(soundEnabled = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on button taps and completions",
                    checked = settings.hapticFeedback,
                    onCheckedChange = { onSettingsChanged(settings.copy(hapticFeedback = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingSwitchRow(
                    title = "Animations",
                    subtitle = "Tile scaling, shake, and transition animations",
                    checked = settings.animationsEnabled,
                    onCheckedChange = { onSettingsChanged(settings.copy(animationsEnabled = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingSwitchRow(
                    title = "Show Hints",
                    subtitle = "Allow one optional equation hint per target",
                    checked = settings.showHints,
                    onCheckedChange = { onSettingsChanged(settings.copy(showHints = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingSwitchRow(
                    title = "Show Coordinates",
                    subtitle = "Display A1 through E5 badges on tiles",
                    checked = settings.showCoordinates,
                    onCheckedChange = { onSettingsChanged(settings.copy(showCoordinates = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingSwitchRow(
                    title = "Confirm Restart",
                    subtitle = "Show confirmation dialog before restarting game",
                    checked = settings.confirmRestart,
                    onCheckedChange = { onSettingsChanged(settings.copy(confirmRestart = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingSwitchRow(
                    title = "Three-Number Equations",
                    subtitle = "Enable equations with up to 3 numbers (e.g. 6 × 3 − 2)",
                    checked = settings.threeNumberEquations,
                    onCheckedChange = { onSettingsChanged(settings.copy(threeNumberEquations = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showResetStatsDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset Statistics", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = { showResetSettingsDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset Settings", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
