package com.nikzigpt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikzigpt.data.AIModel
import com.nikzigpt.ui.components.CommonComponents
import com.nikzigpt.ui.components.LoadingIndicator
import com.nikzigpt.ui.components.ModelChip
import com.nikzigpt.ui.theme.NikziGPTTheme

@Composable
fun ModelSelectionScreen(
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit
) {
    val availableModels by viewModel.availableModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val searchText = remember { mutableStateOf("") }
    val showOnlyFree = remember { mutableStateOf(true) }
    
    // Filter models based on search and free filter
    val filteredModels = remember(availableModels, searchText.value, showOnlyFree.value) {
        availableModels.filter { model ->
            val matchesSearch = searchText.value.isBlank() || 
                model.name.lowercase().contains(searchText.value.lowercase()) ||
                model.provider.lowercase().contains(searchText.value.lowercase()) ||
                model.description.lowercase().contains(searchText.value.lowercase())
            val matchesFree = !showOnlyFree.value || model.isFree
            matchesSearch && matchesFree
        }.sortedBy { it.provider }.thenBy { it.name }
    }
    
    // Group models by provider
    val modelsByProvider = remember(filteredModels) {
        filteredModels.groupBy { it.provider }
            .toList()
            .sortedBy { it.first }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Model", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Refresh button
                    IconButton(onClick = { viewModel.loadModels() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Refresh,
                            contentDescription = "Refresh models",
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
            // Search bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CommonComponents.NikziTextField(
                        value = searchText.value,
                        onValueChange = { searchText.value = it },
                        placeholder = "Search models...",
                        leadingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${filteredModels.size} models",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Free only toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Switch(
                                    checked = showOnlyFree.value,
                                    onCheckedChange = { showOnlyFree.value = it },
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                )
                                Text(
                                    text = "Free only",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // Error display
            error?.let { errorMsg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    CommonComponents.ErrorDisplay(
                        message = errorMsg,
                        onRetry = { viewModel.retryLoadModels() },
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
            
            // Models list
            if (isLoading && availableModels.isEmpty()) {
                LoadingIndicator("Loading models...")
            } else if (filteredModels.isEmpty()) {
                CommonComponents.EmptyState(
                    icon = androidx.compose.material.icons.filled.SearchOff,
                    title = "No models found",
                    subtitle = if (searchText.value.isNotBlank()) {
                        "Try adjusting your search or filters"
                    } else {
                        "No free models available"
                    },
                    actionText = "Refresh",
                    onAction = { viewModel.loadModels() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(modelsByProvider) { (provider, models) ->
                        item {
                            // Provider header
                            Text(
                                text = provider,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, bottom = 8.dp)
                            )
                        }
                        
                        items(models) { model ->
                            ModelChip(
                                modelName = model.name,
                                provider = model.provider,
                                isSelected = selectedModel?.id == model.id,
                                onClick = { viewModel.selectModel(model) },
                                isFree = model.isFree
                            )
                        }
                    }
                }
            }
        }
    }
}