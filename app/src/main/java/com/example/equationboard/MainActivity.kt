package com.example.equationboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.equationboard.game.EquationBoardViewModel
import com.example.equationboard.navigation.EquationBoardNavHost
import com.example.equationboard.ui.theme.EquationBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: EquationBoardViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            EquationBoardTheme(
                themePreference = settings.theme,
                dynamicColor = settings.dynamicColor
            ) {
                EquationBoardNavHost(viewModel = viewModel)
            }
        }
    }
}