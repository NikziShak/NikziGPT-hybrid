package com.nikzigpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikzigpt.ui.ChatAppNavHost
import com.nikzigpt.ui.theme.NikziGPTTheme

class MainActivity : ComponentActivity() {
    
    private val chatViewModel: ChatViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NikziGPTTheme {
                ChatAppNavHost()
            }
        }
    }
}