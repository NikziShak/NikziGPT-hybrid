package com.nikzigpt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikzigpt.ui.components.CommonComponents
import com.nikzigpt.ui.components.NikziIntSlider
import com.nikzigpt.ui.components.NikziSlider
import com.nikzigpt.ui.components.NikziTextField
import com.nikzigpt.ui.components.SettingsItem
import com.nikzigpt.ui.theme.NikziGPTTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val maxTokens by viewModel.maxTokens.collectAsState()
    val systemPrompt by viewModel.systemPrompt.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()
    
    val apiKeyText = remember { mutableStateOf(apiKey) }
    val systemPromptText = remember { mutableStateOf(systemPrompt) }
    val isApiKeyVisible = remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // API Settings Section
                item {
                    SectionHeader("API Settings")
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("OpenRouter API Key", style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { isApiKeyVisible.value = !isApiKeyVisible.value }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible.value) 
                                            androidx.compose.material.icons.filled.VisibilityOff 
                                        else 
                                            androidx.compose.material.icons.filled.Visibility,
                                        contentDescription = if (isApiKeyVisible.value) "Hide API Key" else "Show API Key",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                            
                            NikziTextField(
                                value = apiKeyText.value,
                                onValueChange = { viewModel.updateApiKey(it); apiKeyText.value = it },
                                placeholder = "Enter your OpenRouter API key",
                                visualTransformation = if (isApiKeyVisible.value) 
                                    androidx.compose.ui.text.input.VisualTransformation.None 
                                else 
                                    androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                trailingIcon = {
                                    if (apiKeyText.value.isNotEmpty()) {
                                        IconButton(onClick = { 
                                            viewModel.updateApiKey(""); 
                                            apiKeyText.value = "" 
                                        }) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.filled.Clear,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            )
                            
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                            
                            Text(
                                "Get your free API key from openrouter.ai",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Model Settings Section
                item {
                    SectionHeader("Model Settings")
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            NikziSlider(
                                value = temperature,
                                onValueChange = viewModel.updateTemperature,
                                range = 0f..2f,
                                steps = 200,
                                label = "Temperature",
                                valueFormat = { "%.2f".format(it) }
                            )
                            
                            Text(
                                "Controls randomness. Lower = more focused, Higher = more creative",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            NikziIntSlider(
                                value = maxTokens,
                                onValueChange = viewModel.updateMaxTokens,
                                range = 1..32768,
                                steps = 50,
                                label = "Max Tokens",
                                valueFormat = { it.toString() }
                            )
                            
                            Text(
                                "Maximum response length. Higher values allow longer responses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("System Prompt (Optional)", style = MaterialTheme.typography.titleMedium)
                            
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                            
                            NikziTextField(
                                value = systemPromptText.value,
                                onValueChange = { viewModel.updateSystemPrompt(it); systemPromptText.value = it },
                                placeholder = "Enter system prompt...",
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                singleLine = false
                            )
                            
                            Text(
                                "Set a system prompt to customize the AI's behavior",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Appearance Section
                item {
                    SectionHeader("Appearance")
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SettingsItem(
                                title = "Dark Theme",
                                subtitle = "Use dark mode (light theme coming soon)",
                                trailing = {
                                    Switch(
                                        checked = theme == "dark",
                                        onCheckedChange = { viewModel.updateTheme(if (it) "dark" else "light") },
                                        colors = androidx.compose.material3.SwitchDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                
                // Save Button
                item {
                    Button(
                        onClick = { viewModel.saveSettings() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        Text(if (isSaving) "Saving..." else "Save Settings")
                    }
                }
                
                // Save message
                saveMessage?.let { msg ->
                    item {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (msg.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                }
                
                // Info section
                item {
                    SectionHeader("About")
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SettingsItem(
                                title = "Version",
                                subtitle = "1.0.0"
                            )
                            Divider()
                            SettingsItem(
                                title = "Powered by",
                                subtitle = "OpenRouter API"
                            )
                            Divider()
                            SettingsItem(
                                title = "Open Source",
                                subtitle = "GitHub: NikziGPT"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, top = 8.dp, bottom = 4.dp)
    )
}