package com.nikzigpt.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nikzigpt.data.ChatMessage
import com.nikzigpt.ui.components.CommonComponents
import com.nikzigpt.ui.components.ErrorDisplay
import com.nikzigpt.ui.components.LoadingIndicator
import com.nikzigpt.ui.components.MessageBubble
import com.nikzigpt.ui.components.NikziTextField
import com.nikzigpt.ui.theme.NikziGPTTheme
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToModelSelection: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val currentStreamingMessage by viewModel.currentStreamingMessage.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    
    val scrollState = rememberScrollState()
    val textFieldValue = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    // Auto-scroll to bottom when new messages arrive
    launchedEffect(messages.size) {
        scrollState.animateScrollTo(0)
    }
    
    // Show error snackbar
    launchedEffect(error) {
        if (error != null) {
            // Could show a snackbar here
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NikziGPT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Model selector
                    IconButton(onClick = onNavigateToModelSelection) {
                        Row(
                            modifier = Modifier.padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.SmartToy,
                                contentDescription = "Model",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            selectedModel?.let { model ->
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    text = model.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    // New chat button
                    IconButton(onClick = { viewModel.newChat() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Add,
                            contentDescription = "New Chat",
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Messages list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    reverseLayout = true,
                    state = scrollState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages.reversed()) { message ->
                        MessageBubble(
                            message = message.content,
                            isUser = message.role == "user",
                            isStreaming = message.isStreaming,
                            onCopy = {
                                copyToClipboard(context, message.content)
                            },
                            onRegenerate = {
                                if (message.role == "assistant") {
                                    viewModel.regenerateLastResponse()
                                }
                            }
                        )
                    }
                    
                    // Show streaming indicator at bottom
                    if (isStreaming && currentStreamingMessage.isNotEmpty()) {
                        MessageBubble(
                            message = currentStreamingMessage,
                            isUser = false,
                            isStreaming = true
                        )
                    }
                }
                
                // Error display
                error?.let { errorMsg ->
                    ErrorDisplay(
                        message = errorMsg,
                        onDismiss = { viewModel.clearError() }
                    )
                }
                
                // Input area
                InputArea(
                    textFieldValue = textFieldValue,
                    focusRequester = focusRequester,
                    onSend = { text ->
                        viewModel.sendMessage(text)
                        textFieldValue.value = ""
                    },
                    isLoading = isLoading || isStreaming,
                    selectedModel = selectedModel,
                    onModelClick = onNavigateToModelSelection
                )
            }
            
            // Loading overlay
            if (isLoading && messages.isEmpty()) {
                LoadingIndicator("Loading models...")
            }
        }
    }
}

@Composable
fun InputArea(
    textFieldValue: androidx.compose.runtime.MutableState<String>,
    focusRequester: androidx.compose.ui.focus.FocusRequester,
    onSend: (String) -> Unit,
    isLoading: Boolean,
    selectedModel: com.nikzigpt.data.AIModel?,
    onModelClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = colors.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Model indicator
            selectedModel?.let { model ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.15f)
                        .clickable(onModelClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .background(
                            color = colors.primaryContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = colors.onPrimaryContainer,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                }
            }
            
            // Text field
            NikziTextField(
                value = textFieldValue.value,
                onValueChange = { textFieldValue.value = it },
                placeholder = "Message NikziGPT...",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                ),
                keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                    onDone = { onSend(textFieldValue.value) }
                ),
                trailingIcon = {
                    if (textFieldValue.value.isNotEmpty() && !isLoading) {
                        IconButton(onClick = { onSend(textFieldValue.value) }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.Send,
                                contentDescription = "Send",
                                tint = colors.primary
                            )
                        }
                    } else if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                            color = colors.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("NikziGPT Message", text)
    clipboard.primaryClip = clip
    
    // Show toast
    android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
}

@Composable
fun ChatAppNavHost() {
    val navController = rememberNavController()
    val chatViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ChatViewModel>()
    val settingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<SettingsViewModel>()
    
    NavHost(navController, "chat") {
        composable("chat") {
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToModelSelection = { navController.navigate("model_selection") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("model_selection") {
            ModelSelectionScreen(
                viewModel = chatViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}