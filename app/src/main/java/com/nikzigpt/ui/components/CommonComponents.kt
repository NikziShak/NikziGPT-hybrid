package com.nikzigpt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Custom Card with elevation
@Composable
fun NikziCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick != null, onClick),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        ),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content()
    }
}

// Message bubble
@Composable
fun MessageBubble(
    message: String,
    isUser: Boolean,
    isStreaming: Boolean = false,
    onCopy: (() -> Unit)? = null,
    onRegenerate: (() -> Unit)? = null
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isUser) colors.primaryContainer else colors.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = if (isUser) 4.dp else 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = 20.dp
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = message,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = if (isUser) colors.onPrimaryContainer else colors.onSurfaceVariant
                )
                
                if (isStreaming) {
                    TypingIndicator()
                }
                
                // Action buttons
                if (!isUser && !isStreaming) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (onCopy != null) {
                            IconButton(onClick = onCopy) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = colors.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                        if (onRegenerate != null) {
                            IconButton(onClick = onRegenerate) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.filled.Refresh,
                                    contentDescription = "Regenerate",
                                    tint = colors.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Typing indicator animation
@Composable
fun TypingIndicator() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val scale1 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(600, delayMillis = 0),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )
    val scale2 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(600, delayMillis = 200),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )
    val scale3 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(600, delayMillis = 400),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )
    
    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Dot(scale1.value)
        Dot(scale2.value)
        Dot(scale3.value)
    }
}

@Composable
private fun Dot(scale: Float) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}

// Model selection chip
@Composable
fun ModelChip(
    modelName: String,
    provider: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isFree: Boolean = true
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.primaryContainer else colors.surfaceVariant,
            contentColor = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant
        ),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = modelName,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = provider,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            if (isFree) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .background(
                            color = colors.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FREE",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.secondary
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = colors.primary
                )
            }
        }
    }
}

// Settings item
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick != null, onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        trailing?.invoke()
        
        if (onClick != null && trailing == null) {
            Icon(
                imageVector = androidx.compose.material.icons.filled.ChevronRight,
                contentDescription = "Navigate",
                tint = colors.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// Text input field
@Composable
fun NikziTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: androidx.compose.ui.text.input.KeyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default,
    keyboardActions: androidx.compose.ui.text.input.KeyboardActions = androidx.compose.ui.text.input.KeyboardActions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    androidx.compose.material3.TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
            containerColor = colors.surfaceVariant,
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            textColor = colors.onSurface,
            placeholderColor = colors.onSurfaceVariant.copy(alpha = 0.6f),
            leadingIconColor = colors.onSurfaceVariant.copy(alpha = 0.6f),
            trailingIconColor = colors.onSurfaceVariant.copy(alpha = 0.6f),
            focusedIndicatorColor = colors.primary,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorContainerColor = colors.errorContainer,
            errorTextColor = colors.error
        ),
        label = { Text(text = placeholder, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = androidx.compose.material3.MaterialTheme.shapes.small
    )
}

// Slider for temperature/max tokens
@Composable
fun NikziSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: androidx.compose.ui.unit.FloatRange = 0f..1f,
    steps: Int = 100,
    label: String = "",
    valueFormat: (Float) -> String = { "%.2f".format(it) }
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text(text = valueFormat(value), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.primary)
        }
        
        androidx.compose.material3.Slider(
            value = value,
            onValueChange = onValueChange,
            range = range,
            steps = steps,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = colors.primary,
                trackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant,
                activeTickColor = colors.primary,
                inactiveTickColor = colors.surfaceVariant
            )
        )
    }
}

// Integer slider
@Composable
fun NikziIntSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: androidx.compose.ui.unit.IntRange = 1..4096,
    steps: Int = 10,
    label: String = "",
    valueFormat: (Int) -> String = { it.toString() }
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text(text = valueFormat(value), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.primary)
        }
        
        androidx.compose.material3.RangeSlider(
            value = androidx.compose.ui.unit.IntRange(value, value),
            onValueChange = { range -> onValueChange(range.start) },
            valueRange = range,
            steps = steps,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = colors.primary,
                trackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant,
                activeTickColor = colors.primary,
                inactiveTickColor = colors.surfaceVariant
            )
        )
    }
}

// Loading indicator
@Composable
fun LoadingIndicator(message: String = "Loading...") {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            color = colors.primary,
            strokeWidth = 4.dp
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = message,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant
        )
    }
}

// Error display
@Composable
fun ErrorDisplay(
    message: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.errorContainer,
            contentColor = colors.onErrorContainer
        ),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
                if (onDismiss != null) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Close,
                            contentDescription = "Dismiss",
                            tint = colors.onErrorContainer
                        )
                    }
                }
            }
            
            if (onRetry != null) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

// Empty state
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = subtitle,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        actionText?.let { text ->
            onAction?.let { action ->
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
                Button(onClick = action) {
                    Text(text)
                }
            }
        }
    }
}