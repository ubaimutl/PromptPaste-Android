package dev.ubai.promptpaste.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.ubai.promptpaste.R
import dev.ubai.promptpaste.data.AppSettings
import dev.ubai.promptpaste.data.BuiltInAction
import dev.ubai.promptpaste.data.CustomAction
import dev.ubai.promptpaste.data.InputMode
import dev.ubai.promptpaste.data.ModelOption
import dev.ubai.promptpaste.data.Provider

private enum class AppSection(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    EDITOR("Editor", Icons.Filled.Edit, Icons.Outlined.Edit),
    ACTIONS("Actions", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
}

internal fun shouldReviewDirectReplacement(
    reviewBeforeReplacement: Boolean,
    readOnly: Boolean,
): Boolean = reviewBeforeReplacement && !readOnly

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptPasteApp(
    viewModel: PromptPasteViewModel,
    onPaste: () -> String,
    onCopy: (String) -> Unit,
    onOpenKeyboardSettings: () -> Unit,
    onShowKeyboardPicker: () -> Unit,
    openSettingsInitially: Boolean = false,
) {
    var sectionName by rememberSaveable {
        mutableStateOf(if (openSettingsInitially) AppSection.SETTINGS.name else AppSection.EDITOR.name)
    }
    val section = AppSection.valueOf(sectionName)
    val state = viewModel.state

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_promptpaste_topbar),
                            contentDescription = "PromptPaste logo",
                            modifier = Modifier.size(36.dp),
                        )
                        Column {
                            Text("PromptPaste", fontWeight = FontWeight.Bold)
                            Text(
                                state.settings.provider.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                AppSection.entries.forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = { sectionName = item.name },
                        icon = {
                            Icon(
                                imageVector = if (section == item) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                fontWeight = if (section == item) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }
        },
    ) { padding ->
        when (section) {
            AppSection.EDITOR -> EditorScreen(
                state = state,
                onInputChange = viewModel::updateInput,
                onOutputChange = viewModel::updateOutput,
                onPaste = { viewModel.updateInput(onPaste()) },
                onClear = viewModel::clearInput,
                onBuiltIn = viewModel::run,
                onCustom = viewModel::run,
                onCancel = viewModel::cancelRequest,
                onCopy = onCopy,
                onUseResult = viewModel::useOutputAsInput,
                modifier = Modifier.padding(padding),
            )
            AppSection.ACTIONS -> ActionsScreen(
                state = state,
                onSave = viewModel::saveAction,
                onDelete = viewModel::deleteAction,
                onEnabledChange = viewModel::setActionEnabled,
                onMove = viewModel::moveAction,
                modifier = Modifier.padding(padding),
            )
            AppSection.SETTINGS -> SettingsScreen(
                state = state,
                onProviderChange = viewModel::selectProvider,
                onSettingsChange = viewModel::updateSettings,
                onModelChange = viewModel::setModel,
                onApiKeyChange = viewModel::updateApiKeyDraft,
                onSaveApiKey = viewModel::saveApiKey,
                onRefreshModels = viewModel::refreshModels,
                onOpenKeyboardSettings = onOpenKeyboardSettings,
                onShowKeyboardPicker = onShowKeyboardPicker,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectProcessTextApp(
    viewModel: PromptPasteViewModel,
    action: BuiltInAction,
    readOnly: Boolean,
    onFinish: (String) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state = viewModel.state
    val reviewBeforeReplacing = shouldReviewDirectReplacement(
        reviewBeforeReplacement = state.settings.reviewBeforeKeyboardReplacement,
        readOnly = readOnly,
    )

    LaunchedEffect(state.output, reviewBeforeReplacing) {
        if (state.output.isNotBlank() && !reviewBeforeReplacing) onFinish(state.output)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PromptPaste: ${action.label}", fontWeight = FontWeight.Bold)
                        Text(
                            state.settings.provider.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancel") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.output.isNotBlank() && reviewBeforeReplacing -> {
                    Text(
                        "Review result",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Check or edit the generated text before replacing the selection.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.output,
                        onValueChange = viewModel::updateOutput,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                        label = { Text("Result") },
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                        ) { Text("Cancel") }
                        Button(
                            onClick = { onFinish(state.output) },
                            modifier = Modifier.weight(1f),
                        ) { Text("Replace selection") }
                    }
                }
                state.error.isNotBlank() -> {
                    Text(
                        when (action) {
                            BuiltInAction.CORRECT -> "Could not correct the selection"
                            BuiltInAction.REWRITE -> "Could not rewrite the selection"
                            BuiltInAction.RUN_PROMPT -> "Could not run the selected prompt"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(16.dp))
                    ErrorCard(state.error)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Open Settings") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Try again") }
                }
                state.isRunning -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        when (action) {
                            BuiltInAction.CORRECT -> "Correcting selected text…"
                            BuiltInAction.REWRITE -> "Rewriting selected text…"
                            BuiltInAction.RUN_PROMPT -> "Running selected prompt…"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (reviewBeforeReplacing) {
                            "You can review the result before replacing the selection."
                        } else {
                            "The result will replace the selection automatically."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessTextApp(
    viewModel: PromptPasteViewModel,
    readOnly: Boolean,
    onCopy: (String) -> Unit,
    onFinish: (String) -> Unit,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state = viewModel.state
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PromptPaste", fontWeight = FontWeight.Bold)
                        Text(
                            state.settings.provider.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = { TextButton(onClick = onClose) { Text("Close") } },
                actions = { TextButton(onClick = onOpenSettings) { Text("Settings") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
    ) { padding ->
        EditorScreen(
            state = state,
            onInputChange = viewModel::updateInput,
            onOutputChange = viewModel::updateOutput,
            onPaste = null,
            onClear = null,
            onBuiltIn = viewModel::run,
            onCustom = viewModel::run,
            onCancel = viewModel::cancelRequest,
            onCopy = onCopy,
            onUseResult = { onFinish(state.output) },
            resultActionLabel = if (readOnly) "Copy and close" else "Replace selection",
            supportingText = if (readOnly) {
                "This app marked the selection read-only. PromptPaste will copy the result instead."
            } else {
                "Choose an action, review the result, then return it to the original app."
            },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun EditorScreen(
    state: PromptPasteUiState,
    onInputChange: (String) -> Unit,
    onOutputChange: (String) -> Unit,
    onPaste: (() -> Unit)?,
    onClear: (() -> Unit)?,
    onBuiltIn: (BuiltInAction) -> Unit,
    onCustom: (CustomAction) -> Unit,
    onCancel: () -> Unit,
    onCopy: (String) -> Unit,
    onUseResult: () -> Unit,
    modifier: Modifier = Modifier,
    resultActionLabel: String = "Use as input",
    supportingText: String = "Paste text or share it from another app, then choose an action.",
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                supportingText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = onInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Text") },
                    minLines = 6,
                    maxLines = 14,
                    shape = RoundedCornerShape(12.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${state.input.length} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (onPaste != null || onClear != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (onPaste != null) {
                                OutlinedButton(
                                    onClick = onPaste,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Icon(
                                        Icons.Default.ContentPaste,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Paste")
                                }
                            }
                            if (onClear != null && state.input.isNotEmpty()) {
                                TextButton(
                                    onClick = onClear,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clear")
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Choose an action",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onBuiltIn(BuiltInAction.CORRECT) },
                    enabled = !state.isRunning,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(BuiltInAction.CORRECT.label)
                }
                FilledTonalButton(
                    onClick = { onBuiltIn(BuiltInAction.REWRITE) },
                    enabled = !state.isRunning,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(BuiltInAction.REWRITE.label)
                }
                FilledTonalButton(
                    onClick = { onBuiltIn(BuiltInAction.RUN_PROMPT) },
                    enabled = !state.isRunning,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(BuiltInAction.RUN_PROMPT.label)
                }
                state.actions.filter { it.enabled }.forEach { action ->
                    OutlinedButton(
                        onClick = { onCustom(action) },
                        enabled = !state.isRunning,
                        shape = RoundedCornerShape(20.dp),
                    ) { Text(action.name) }
                }
            }
        }
        if (state.isRunning) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LinearProgressIndicator(
                            Modifier.fillMaxWidth(),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Generating response with ${state.settings.provider.displayName}…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = onCancel) { Text("Cancel") }
                        }
                    }
                }
            }
        }
        if (state.error.isNotBlank()) {
            item { ErrorCard(state.error) }
        }
        if (state.output.isNotBlank()) {
            item {
                ElevatedCard(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Result",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ) {
                                Text(
                                    "${state.output.length} chars",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                        OutlinedTextField(
                            value = state.output,
                            onValueChange = onOutputChange,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 6,
                            maxLines = 16,
                            label = { Text("Review and edit") },
                            shape = RoundedCornerShape(12.dp),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        ) {
                            OutlinedButton(
                                onClick = { onCopy(state.output) },
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Copy")
                            }
                            Button(
                                onClick = onUseResult,
                                shape = RoundedCornerShape(20.dp),
                            ) { Text(resultActionLabel) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp),
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActionsScreen(
    state: PromptPasteUiState,
    onSave: (CustomAction) -> Unit,
    onDelete: (CustomAction) -> Unit,
    onEnabledChange: (CustomAction, Boolean) -> Unit,
    onMove: (CustomAction, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEditor by remember { mutableStateOf(false) }
    var editingAction by remember { mutableStateOf<CustomAction?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Custom actions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Create reusable transformations or prompts.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onClick = {
                        editingAction = null
                        showEditor = true
                    },
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Add")
                }
            }
        }
        if (state.actions.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "No custom actions yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Built-in Correct, Rewrite, and Run prompt are always available. Tap 'Add' to create your own.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        items(state.actions, key = { it.id }) { action ->
            val index = state.actions.indexOfFirst { it.id == action.id }
            ElevatedCard(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(action.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Text(
                                    actionSummary(action),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Switch(
                            checked = action.enabled,
                            onCheckedChange = { onEnabledChange(action, it) },
                        )
                    }
                    Text(
                        action.prompt.ifBlank { "The selected text is used directly as the prompt." },
                        maxLines = 3,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { onMove(action, -1) },
                            enabled = index > 0,
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move up",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(
                            onClick = { onMove(action, 1) },
                            enabled = index < state.actions.lastIndex,
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move down",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(onClick = {
                            editingAction = action
                            showEditor = true
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit action",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        IconButton(onClick = { onDelete(action) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete action",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        ActionEditorDialog(
            action = editingAction,
            onDismiss = { showEditor = false },
            onSave = {
                onSave(it)
                showEditor = false
            },
        )
    }
}

private fun actionSummary(action: CustomAction): String {
    val mode = if (action.inputMode == InputMode.PROMPT) "Prompt" else "Transform"
    val provider = Provider.entries.firstOrNull { it.id == action.providerId }?.displayName ?: "Active provider"
    val limits = buildList {
        if (action.inputLimit > 0) add("in: ${action.inputLimit}")
        if (action.outputLimit > 0) add("out: ${action.outputLimit}")
    }
    return listOfNotNull(mode, provider, limits.takeIf { it.isNotEmpty() }?.joinToString(" / ")).joinToString(" · ")
}

@Composable
private fun ActionEditorDialog(
    action: CustomAction?,
    onDismiss: () -> Unit,
    onSave: (CustomAction) -> Unit,
) {
    var name by remember(action?.id) { mutableStateOf(action?.name.orEmpty()) }
    var prompt by remember(action?.id) { mutableStateOf(action?.prompt.orEmpty()) }
    var inputMode by remember(action?.id) { mutableStateOf(action?.inputMode ?: InputMode.TRANSFORM) }
    var providerId by remember(action?.id) { mutableStateOf(action?.providerId.orEmpty()) }
    var model by remember(action?.id) { mutableStateOf(action?.model.orEmpty()) }
    var inputLimit by remember(action?.id) {
        mutableStateOf(action?.inputLimit?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var outputLimit by remember(action?.id) {
        mutableStateOf(action?.outputLimit?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var enabled by remember(action?.id) { mutableStateOf(action?.enabled != false) }
    val valid = name.isNotBlank() && (inputMode == InputMode.PROMPT || prompt.isNotBlank())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(16.dp)
                .imePadding(),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    if (action == null) "New action" else "Edit action",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    EnumPicker(
                        label = "Input mode",
                        value = if (inputMode == InputMode.TRANSFORM) "Transform selected text" else "Use text as prompt",
                        options = listOf(
                            "Transform selected text" to InputMode.TRANSFORM,
                            "Use text as prompt" to InputMode.PROMPT,
                        ),
                        onSelected = { inputMode = it },
                    )
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(if (inputMode == InputMode.PROMPT) "System guidance (optional)" else "Prompt")
                        },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                    )
                    ProviderOverridePicker(providerId) {
                        providerId = it
                        if (it.isBlank()) model = ""
                    }
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Model override (optional)") },
                        enabled = providerId.isNotBlank(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    NumberField("Input token limit (0 = auto)", inputLimit) { inputLimit = it }
                    NumberField("Output token limit (0 = auto)", outputLimit) { outputLimit = it }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Show in action list", fontWeight = FontWeight.Medium)
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            onSave(
                                CustomAction(
                                    id = action?.id.orEmpty(),
                                    name = name,
                                    prompt = prompt,
                                    enabled = enabled,
                                    providerId = providerId,
                                    model = model,
                                    inputMode = inputMode,
                                    inputLimit = inputLimit.toIntOrNull() ?: 0,
                                    outputLimit = outputLimit.toIntOrNull() ?: 0,
                                ),
                            )
                        },
                        enabled = valid,
                    ) { Text("Save") }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: PromptPasteUiState,
    onProviderChange: (Provider) -> Unit,
    onSettingsChange: (AppSettings) -> Unit,
    onModelChange: (Provider, String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onRefreshModels: () -> Unit,
    onOpenKeyboardSettings: () -> Unit,
    onShowKeyboardPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    var showKey by rememberSaveable { mutableStateOf(false) }
    var showModelChooser by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Use inside other apps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "For apps that hide Android text actions, enable the optional PromptPaste Actions " +
                    "keyboard. Select text, switch to it, and tap an action. It replaces the selection " +
                    "and returns to your previous keyboard.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            ElevatedCard(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("PromptPaste Actions keyboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Android shows a keyboard-access warning when you enable it. PromptPaste " +
                            "reads only the selected text after you tap an action, and its actions are " +
                            "disabled in password fields.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onOpenKeyboardSettings,
                            shape = RoundedCornerShape(20.dp),
                        ) { Text("Enable keyboard") }
                        OutlinedButton(
                            onClick = onShowKeyboardPicker,
                            shape = RoundedCornerShape(20.dp),
                        ) { Text("Choose keyboard") }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Review before replacing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Show the result for keyboard and direct selection actions, then wait for confirmation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = settings.reviewBeforeKeyboardReplacement,
                            onCheckedChange = {
                                onSettingsChange(settings.copy(reviewBeforeKeyboardReplacement = it))
                            },
                        )
                    }
                }
            }
        }
        item { SectionDivider("AI Provider & Credentials") }
        item {
            Text(
                "Text is sent only when you run an action. API keys are encrypted with Android Keystore.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        item {
            ProviderPicker(settings.provider, onProviderChange)
        }
        if (settings.provider == Provider.OLLAMA) {
            item {
                OutlinedTextField(
                    value = settings.ollamaUrl,
                    onValueChange = { onSettingsChange(settings.copy(ollamaUrl = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ollama address") },
                    supportingText = { Text("For the Android emulator, a server on your computer is usually 10.0.2.2.") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
            }
        } else {
            item {
                OutlinedTextField(
                    value = state.apiKeyDraft,
                    onValueChange = onApiKeyChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API key") },
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { showKey = !showKey }) {
                            Text(if (showKey) "Hide" else "Show")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        state.apiKeyStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                    Button(
                        onClick = onSaveApiKey,
                        shape = RoundedCornerShape(20.dp),
                    ) { Text("Save key") }
                }
            }
        }
        item {
            OutlinedTextField(
                value = settings.modelFor(settings.provider),
                onValueChange = { onModelChange(settings.provider, it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Model ID") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onRefreshModels,
                    enabled = !state.isLoadingModels,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    if (state.isLoadingModels) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Refresh models")
                }
                if (state.availableModels.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { showModelChooser = true },
                        shape = RoundedCornerShape(20.dp),
                    ) { Text("Choose model (${state.availableModels.size})") }
                }
            }
            if (state.modelStatus.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    state.modelStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { SectionDivider("Prompt variables") }
        item {
            OutlinedTextField(
                value = settings.language,
                onValueChange = { onSettingsChange(settings.copy(language = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("${'$'}{language}") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )
        }
        item {
            OutlinedTextField(
                value = settings.tone,
                onValueChange = { onSettingsChange(settings.copy(tone = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("${'$'}{tone}") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )
        }
        item {
            OutlinedTextField(
                value = settings.style,
                onValueChange = { onSettingsChange(settings.copy(style = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("${'$'}{style}") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )
        }
        item { SectionDivider("Built-in prompts") }
        item {
            PromptField("Correct", settings.promptCorrect) {
                onSettingsChange(settings.copy(promptCorrect = it))
            }
        }
        item {
            PromptField("Rewrite", settings.promptRewrite) {
                onSettingsChange(settings.copy(promptRewrite = it))
            }
        }
        item {
            PromptField("Run selected prompt", settings.promptRun) {
                onSettingsChange(settings.copy(promptRun = it))
            }
        }
        item { SectionDivider("Run prompt overrides") }
        item {
            ProviderOverridePicker(settings.runProviderId) {
                onSettingsChange(
                    settings.copy(
                        runProviderId = it,
                        runModel = if (it.isBlank()) "" else settings.runModel,
                    ),
                )
            }
        }
        item {
            OutlinedTextField(
                value = settings.runModel,
                onValueChange = { onSettingsChange(settings.copy(runModel = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Model override (optional)") },
                enabled = settings.runProviderId.isNotBlank(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )
        }
        item {
            NumberField(
                "Input token limit (0 = auto)",
                settings.runInputLimit.takeIf { it > 0 }?.toString().orEmpty(),
            ) { onSettingsChange(settings.copy(runInputLimit = it.toIntOrNull() ?: 0)) }
        }
        item {
            NumberField(
                "Output token limit (0 = auto)",
                settings.runOutputLimit.takeIf { it > 0 }?.toString().orEmpty(),
            ) { onSettingsChange(settings.copy(runOutputLimit = it.toIntOrNull() ?: 0)) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showModelChooser) {
        ModelChooserDialog(
            models = state.availableModels,
            onDismiss = { showModelChooser = false },
            onSelect = {
                onModelChange(settings.provider, it.id)
                showModelChooser = false
            },
        )
    }
}

@Composable
private fun ModelChooserDialog(
    models: List<ModelOption>,
    onDismiss: () -> Unit,
    onSelect: (ModelOption) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(models, query) {
        if (query.isBlank()) models else models.filter { it.label.contains(query, ignoreCase = true) }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Choose model", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search ${models.size} models") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(Modifier.weight(1f)) {
                    items(filtered, key = { it.id }) { model ->
                        TextButton(
                            onClick = { onSelect(model) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                model.label,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderPicker(selected: Provider, onSelected: (Provider) -> Unit) {
    EnumPicker(
        label = "Provider",
        value = selected.displayName,
        options = Provider.entries.map { it.displayName to it },
        onSelected = onSelected,
    )
}

@Composable
private fun ProviderOverridePicker(selectedId: String, onSelected: (String) -> Unit) {
    EnumPicker(
        label = "Provider override",
        value = Provider.entries.firstOrNull { it.id == selectedId }?.displayName ?: "Use active provider",
        options = listOf("Use active provider" to "") + Provider.entries.map { it.displayName to it.id },
        onSelected = onSelected,
    )
}

@Composable
private fun <T> EnumPicker(
    label: String,
    value: String,
    options: List<Pair<String, T>>,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(2.dp))
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (title, option) ->
                DropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PromptField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        minLines = 3,
        maxLines = 7,
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { next -> if (next.all(Char::isDigit)) onValueChange(next) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
private fun SectionDivider(title: String) {
    Column(Modifier.padding(top = 10.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
