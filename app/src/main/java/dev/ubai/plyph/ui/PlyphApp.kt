package dev.ubai.plyph.ui

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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.ubai.plyph.R
import dev.ubai.plyph.data.AppSettings
import dev.ubai.plyph.data.BuiltInAction
import dev.ubai.plyph.data.CustomAction
import dev.ubai.plyph.data.InputMode
import dev.ubai.plyph.data.ModelOption
import dev.ubai.plyph.data.Provider


private enum class AppSection(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    EDITOR(R.string.nav_editor, Icons.Filled.Edit, Icons.Outlined.Edit),
    ACTIONS(R.string.nav_actions, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    SETTINGS(R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
}

internal fun shouldReviewDirectReplacement(
    reviewBeforeReplacement: Boolean,
    readOnly: Boolean,
): Boolean = reviewBeforeReplacement && !readOnly

@Composable
private fun builtInActionLabel(action: BuiltInAction): String = stringResource(
    when (action) {
        BuiltInAction.CORRECT -> R.string.process_text_correct_label
        BuiltInAction.REWRITE -> R.string.process_text_rewrite_label
        BuiltInAction.RUN_PROMPT -> R.string.process_text_run_label
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlyphApp(
    viewModel: PlyphViewModel,
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
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_plyph_topbar),
                                contentDescription = stringResource(R.string.plyph_logo),
                                modifier = Modifier.size(28.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    "Plyph",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    state.settings.provider.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    AppSection.entries.forEach { item ->
                        val itemLabel = stringResource(item.labelRes)
                        NavigationBarItem(
                            selected = section == item,
                            onClick = { sectionName = item.name },
                            icon = {
                                Icon(
                                    imageVector = if (section == item) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = itemLabel,
                                )
                            },
                            label = {
                                Text(
                                    itemLabel,
                                    fontWeight = if (section == item) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
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
                onRefreshModels = viewModel::refreshActionModels,
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
    viewModel: PlyphViewModel,
    action: BuiltInAction,
    readOnly: Boolean,
    onFinish: (String) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state = viewModel.state
    val actionLabel = builtInActionLabel(action)
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
                        Text(stringResource(R.string.plyph_action_title, actionLabel), fontWeight = FontWeight.Bold)
                        Text(
                            state.settings.provider.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = { TextButton(onClick = onCancel) { Text(stringResource(R.string.common_cancel)) } },
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
                        stringResource(R.string.review_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.review_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.output,
                        onValueChange = viewModel::updateOutput,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                        label = { Text(stringResource(R.string.common_result)) },
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(R.string.common_cancel)) }
                        Button(
                            onClick = { onFinish(state.output) },
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(R.string.common_replace_selection)) }
                    }
                }
                state.error.isNotBlank() -> {
                    Text(
                        when (action) {
                            BuiltInAction.CORRECT -> stringResource(R.string.error_correct_selection)
                            BuiltInAction.REWRITE -> stringResource(R.string.error_rewrite_selection)
                            BuiltInAction.RUN_PROMPT -> stringResource(R.string.error_run_prompt)
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
                    ) { Text(stringResource(R.string.common_open_settings)) }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.common_try_again)) }
                }
                state.isRunning -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        when (action) {
                            BuiltInAction.CORRECT -> stringResource(R.string.running_correct)
                            BuiltInAction.REWRITE -> stringResource(R.string.running_rewrite)
                            BuiltInAction.RUN_PROMPT -> stringResource(R.string.running_prompt)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (reviewBeforeReplacing) {
                            stringResource(R.string.review_available_message)
                        } else {
                            stringResource(R.string.replacement_automatic_message)
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
    viewModel: PlyphViewModel,
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
                        Text("Plyph", fontWeight = FontWeight.Bold)
                        Text(
                            state.settings.provider.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = { TextButton(onClick = onClose) { Text(stringResource(R.string.common_close)) } },
                actions = { TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.common_settings)) } },
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
            resultActionLabel = if (readOnly) {
                stringResource(R.string.editor_copy_close)
            } else {
                stringResource(R.string.common_replace_selection)
            },
            supportingText = if (readOnly) {
                stringResource(R.string.editor_read_only_help)
            } else {
                stringResource(R.string.editor_process_help)
            },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun EditorScreen(
    state: PlyphUiState,
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
    resultActionLabel: String? = null,
    supportingText: String? = null,
) {
    val resolvedResultActionLabel = resultActionLabel ?: stringResource(R.string.editor_use_as_input)
    val resolvedSupportingText = supportingText ?: stringResource(R.string.editor_default_help)
    val actionScrollState = rememberScrollState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                resolvedSupportingText,
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
                    label = { Text(stringResource(R.string.editor_text)) },
                    minLines = 6,
                    maxLines = 14,
                    shape = MaterialTheme.shapes.large,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        pluralStringResource(
                            R.plurals.editor_characters,
                            state.input.length,
                            state.input.length,
                        ),
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
                                    Text(stringResource(R.string.editor_paste))
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
                                    Text(stringResource(R.string.editor_clear))
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(
                stringResource(R.string.editor_choose_action),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(actionScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onBuiltIn(BuiltInAction.CORRECT) },
                    enabled = !state.isRunning,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(builtInActionLabel(BuiltInAction.CORRECT))
                }
                FilledTonalButton(
                    onClick = { onBuiltIn(BuiltInAction.REWRITE) },
                    enabled = !state.isRunning,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(builtInActionLabel(BuiltInAction.REWRITE))
                }
                FilledTonalButton(
                    onClick = { onBuiltIn(BuiltInAction.RUN_PROMPT) },
                    enabled = !state.isRunning,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(builtInActionLabel(BuiltInAction.RUN_PROMPT))
                }
                state.actions.filter { it.enabled }.forEach { action ->
                    OutlinedButton(
                        onClick = { onCustom(action) },
                        enabled = !state.isRunning,
                        shape = MaterialTheme.shapes.medium,
                    ) { Text(action.name) }
                }
            }
        }
        if (state.isRunning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                stringResource(R.string.editor_generating, state.settings.provider.displayName),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = onCancel) { Text(stringResource(R.string.common_cancel)) }
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
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                stringResource(R.string.common_result),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ) {
                                Text(
                                    pluralStringResource(
                                        R.plurals.editor_chars,
                                        state.output.length,
                                        state.output.length,
                                    ),
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
                            label = { Text(stringResource(R.string.editor_review_edit)) },
                            shape = MaterialTheme.shapes.large,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        ) {
                            OutlinedButton(
                                onClick = { onCopy(state.output) },
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.common_copy))
                            }
                            Button(
                                onClick = onUseResult,
                                shape = MaterialTheme.shapes.medium,
                            ) { Text(resolvedResultActionLabel) }
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
                contentDescription = stringResource(R.string.common_error),
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
    state: PlyphUiState,
    onSave: (CustomAction) -> Unit,
    onDelete: (CustomAction) -> Unit,
    onEnabledChange: (CustomAction, Boolean) -> Unit,
    onMove: (CustomAction, Int) -> Unit,
    onRefreshModels: (Provider) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEditor by remember { mutableStateOf(false) }
    var editingAction by remember { mutableStateOf<CustomAction?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.actions_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.actions_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onClick = {
                        editingAction = null
                        showEditor = true
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.common_add))
                }
            }
        }
        if (state.actions.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(R.string.actions_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(R.string.actions_empty_description),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        items(state.actions, key = { it.id }) { action ->
            val index = state.actions.indexOfFirst { it.id == action.id }
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Text(
                                    actionSummary(action),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        action.prompt.ifBlank { stringResource(R.string.actions_direct_prompt) },
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
                                contentDescription = stringResource(R.string.actions_move_up),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(
                            onClick = { onMove(action, 1) },
                            enabled = index < state.actions.lastIndex,
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.actions_move_down),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(onClick = {
                            editingAction = action
                            showEditor = true
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.actions_edit),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        IconButton(onClick = { onDelete(action) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.actions_delete),
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
            activeProvider = state.settings.provider,
            availableModels = state.actionAvailableModels,
            availableModelsProviderId = state.actionModelsProviderId,
            isLoadingModels = state.isLoadingActionModels,
            onRefreshModels = onRefreshModels,
            onDismiss = { showEditor = false },
            onSave = {
                onSave(it)
                showEditor = false
            },
        )
    }
}

@Composable
private fun actionSummary(action: CustomAction): String {
    val mode = if (action.inputMode == InputMode.PROMPT) {
        stringResource(R.string.actions_mode_prompt)
    } else {
        stringResource(R.string.actions_mode_transform)
    }
    val provider = Provider.entries.firstOrNull { it.id == action.providerId }?.displayName
        ?: stringResource(R.string.actions_active_provider)
    val limits = buildList {
        if (action.inputLimit > 0) add("in: ${action.inputLimit}")
        if (action.outputLimit > 0) add("out: ${action.outputLimit}")
    }
    return listOfNotNull(mode, provider, limits.takeIf { it.isNotEmpty() }?.joinToString(" / ")).joinToString(" · ")
}

@Composable
private fun ActionEditorDialog(
    action: CustomAction?,
    activeProvider: Provider,
    availableModels: List<ModelOption>,
    availableModelsProviderId: String,
    isLoadingModels: Boolean,
    onRefreshModels: (Provider) -> Unit,
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
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    stringResource(if (action == null) R.string.action_new else R.string.action_edit),
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
                        label = { Text(stringResource(R.string.action_name)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                    )
                    EnumPicker(
                        label = stringResource(R.string.action_input_mode),
                        value = if (inputMode == InputMode.TRANSFORM) {
                            stringResource(R.string.action_transform_selected)
                        } else {
                            stringResource(R.string.action_use_as_prompt)
                        },
                        options = listOf(
                            stringResource(R.string.action_transform_selected) to InputMode.TRANSFORM,
                            stringResource(R.string.action_use_as_prompt) to InputMode.PROMPT,
                        ),
                        onSelected = { inputMode = it },
                    )
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                stringResource(
                                    if (inputMode == InputMode.PROMPT) {
                                        R.string.action_system_guidance
                                    } else {
                                        R.string.action_prompt
                                    },
                                ),
                            )
                        },
                        minLines = 4,
                        maxLines = 8,
                        shape = MaterialTheme.shapes.medium,
                    )
                    ProviderOverridePicker(providerId) {
                        providerId = it
                    }
                    val selectedProvider = Provider.entries.firstOrNull { it.id == providerId }
                        ?: activeProvider
                    val modelSuggestions = buildList {
                        add(ModelOption(selectedProvider.defaultModel))
                        if (availableModelsProviderId == selectedProvider.id) addAll(availableModels)
                    }.distinctBy { it.id }
                    EditableModelPicker(
                        value = model,
                        onValueChange = { model = it },
                        label = stringResource(R.string.action_model_override),
                        models = modelSuggestions,
                        isLoading = isLoadingModels && availableModelsProviderId == selectedProvider.id,
                        onRefresh = { onRefreshModels(selectedProvider) },
                    )
                    NumberField(stringResource(R.string.action_input_limit), inputLimit) { inputLimit = it }
                    NumberField(stringResource(R.string.action_output_limit), outputLimit) { outputLimit = it }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.action_show_in_list), fontWeight = FontWeight.Medium)
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
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
                    ) { Text(stringResource(R.string.common_save)) }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: PlyphUiState,
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
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        item {
            SettingsSectionHeader(
                title = stringResource(R.string.settings_keyboard_title),
                description = stringResource(R.string.settings_keyboard_description),
            )
            Spacer(Modifier.height(10.dp))
            SettingsCard {
                Text(
                    stringResource(R.string.settings_keyboard_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.settings_keyboard_optional),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onOpenKeyboardSettings,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                    ) { Text(stringResource(R.string.settings_enable_keyboard)) }
                    OutlinedButton(
                        onClick = onShowKeyboardPicker,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                    ) { Text(stringResource(R.string.settings_choose_keyboard)) }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.settings_keyboard_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                SettingSwitchRow(
                    title = stringResource(R.string.settings_review_title),
                    description = stringResource(R.string.settings_review_description),
                    checked = settings.reviewBeforeKeyboardReplacement,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(reviewBeforeKeyboardReplacement = it))
                    },
                )
                if (settings.reviewBeforeKeyboardReplacement) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingSwitchRow(
                        title = stringResource(R.string.settings_inline_review_title),
                        description = stringResource(R.string.settings_inline_review_description),
                        checked = settings.reviewInsideKeyboard,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(reviewInsideKeyboard = it))
                        },
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                SettingSwitchRow(
                    title = stringResource(R.string.settings_select_all_title),
                    description = stringResource(R.string.settings_select_all_description),
                    checked = settings.showSelectAllInKeyboard,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(showSelectAllInKeyboard = it))
                    },
                )
            }
        }

        item {
            SettingsSectionHeader(
                title = stringResource(R.string.settings_provider_title),
                description = stringResource(R.string.settings_provider_description),
            )
            Spacer(Modifier.height(10.dp))
            SettingsCard {
                ProviderPicker(settings.provider, onProviderChange)
                Spacer(Modifier.height(14.dp))

                if (settings.provider == Provider.OLLAMA) {
                    OutlinedTextField(
                        value = settings.ollamaUrl,
                        onValueChange = { onSettingsChange(settings.copy(ollamaUrl = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_ollama_address)) },
                        supportingText = { Text(stringResource(R.string.settings_emulator_address)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                    )
                } else {
                    OutlinedTextField(
                        value = state.apiKeyDraft,
                        onValueChange = onApiKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_api_key)) },
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { showKey = !showKey }) {
                                Text(
                                    stringResource(
                                        if (showKey) R.string.common_hide else R.string.common_show,
                                    ),
                                )
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                    )
                    Spacer(Modifier.height(8.dp))
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
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = onSaveApiKey,
                            shape = MaterialTheme.shapes.medium,
                        ) { Text(stringResource(R.string.settings_save_key)) }
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = settings.modelFor(settings.provider),
                    onValueChange = { onModelChange(settings.provider, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings_model_id)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onRefreshModels,
                        enabled = !state.isLoadingModels,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        if (state.isLoadingModels) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.common_refresh))
                    }
                    if (state.availableModels.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { showModelChooser = true },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(stringResource(R.string.settings_choose_count, state.availableModels.size))
                        }
                    }
                }
                if (state.modelStatus.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.modelStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.settings_privacy_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SettingsSectionHeader(
                title = stringResource(R.string.settings_variables_title),
                description = stringResource(R.string.settings_variables_description),
            )
            Spacer(Modifier.height(10.dp))
            SettingsCard {
                OutlinedTextField(
                    value = settings.language,
                    onValueChange = { onSettingsChange(settings.copy(language = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("${'$'}{language}") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = settings.tone,
                    onValueChange = { onSettingsChange(settings.copy(tone = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("${'$'}{tone}") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = settings.style,
                    onValueChange = { onSettingsChange(settings.copy(style = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("${'$'}{style}") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
            }
        }

        item {
            SettingsSectionHeader(
                title = stringResource(R.string.settings_prompts_title),
                description = stringResource(R.string.settings_prompts_description),
            )
            Spacer(Modifier.height(10.dp))
            SettingsCard {
                PromptField(stringResource(R.string.process_text_correct_label), settings.promptCorrect) {
                    onSettingsChange(settings.copy(promptCorrect = it))
                }
                Spacer(Modifier.height(12.dp))
                PromptField(stringResource(R.string.process_text_rewrite_label), settings.promptRewrite) {
                    onSettingsChange(settings.copy(promptRewrite = it))
                }
                Spacer(Modifier.height(12.dp))
                PromptField(stringResource(R.string.settings_run_selected_prompt), settings.promptRun) {
                    onSettingsChange(settings.copy(promptRun = it))
                }
            }
        }

        item {
            SettingsSectionHeader(
                title = stringResource(R.string.settings_overrides_title),
                description = stringResource(R.string.settings_overrides_description),
            )
            Spacer(Modifier.height(10.dp))
            SettingsCard {
                ProviderOverridePicker(settings.runProviderId) {
                    onSettingsChange(
                        settings.copy(
                            runProviderId = it,
                            runModel = if (it.isBlank()) "" else settings.runModel,
                        ),
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = settings.runModel,
                    onValueChange = { onSettingsChange(settings.copy(runModel = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.action_model_override)) },
                    enabled = settings.runProviderId.isNotBlank(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Spacer(Modifier.height(12.dp))
                NumberField(
                    stringResource(R.string.action_input_limit),
                    settings.runInputLimit.takeIf { it > 0 }?.toString().orEmpty(),
                ) { onSettingsChange(settings.copy(runInputLimit = it.toIntOrNull() ?: 0)) }
                Spacer(Modifier.height(12.dp))
                NumberField(
                    stringResource(R.string.action_output_limit),
                    settings.runOutputLimit.takeIf { it > 0 }?.toString().orEmpty(),
                ) { onSettingsChange(settings.copy(runOutputLimit = it.toIntOrNull() ?: 0)) }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
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
                    Text(stringResource(R.string.models_choose), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.models_search, models.size)) },
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
        label = stringResource(R.string.settings_provider),
        value = selected.displayName,
        options = Provider.entries.map { it.displayName to it },
        onSelected = onSelected,
    )
}

@Composable
private fun ProviderOverridePicker(selectedId: String, onSelected: (String) -> Unit) {
    val activeProviderLabel = stringResource(R.string.settings_use_active_provider)
    EnumPicker(
        label = stringResource(R.string.settings_provider_override),
        value = Provider.entries.firstOrNull { it.id == selectedId }?.displayName ?: activeProviderLabel,
        options = listOf(activeProviderLabel to "") + Provider.entries.map { it.displayName to it.id },
        onSelected = onSelected,
    )
}

@Composable
private fun EditableModelPicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    models: List<ModelOption>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(models.firstOrNull()?.id.orEmpty()) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRefresh, enabled = !isLoading) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.common_refresh),
                            )
                        }
                    }
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.models_choose),
                        )
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model.label) },
                    onClick = {
                        onValueChange(model.id)
                        expanded = false
                    },
                )
            }
        }
    }
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
            shape = MaterialTheme.shapes.medium,
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
        shape = MaterialTheme.shapes.medium,
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
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    description: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!description.isNullOrBlank()) {
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
