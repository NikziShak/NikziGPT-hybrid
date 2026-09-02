package com.nikzigpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikzigpt.ui.SettingsScreen
import com.nikzigpt.ui.theme.NikziGPTTheme

class SettingsActivity : ComponentActivity() {
    
    private val settingsViewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NikziGPTTheme {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}