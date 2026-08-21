package dev.ubai.promptpaste.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.ubai.promptpaste.data.AppSettings
import dev.ubai.promptpaste.data.ActionRequest
import dev.ubai.promptpaste.data.BuiltInAction
import dev.ubai.promptpaste.data.CustomAction
import dev.ubai.promptpaste.data.InputMode
import dev.ubai.promptpaste.data.ModelOption
import dev.ubai.promptpaste.data.Provider
import dev.ubai.promptpaste.data.SettingsRepository
import dev.ubai.promptpaste.data.toRequest
import dev.ubai.promptpaste.network.AiClient
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class PromptPasteUiState(
    val settings: AppSettings,
    val actions: List<CustomAction>,
    val input: String = "",
    val output: String = "",
    val isRunning: Boolean = false,
    val error: String = "",
    val availableModels: List<ModelOption> = emptyList(),
    val isLoadingModels: Boolean = false,
    val modelStatus: String = "",
    val apiKeyDraft: String = "",
    val apiKeyStatus: String = "",
)

class PromptPasteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val aiClient = AiClient(repository)
    private var requestJob: Job? = null
    private var modelsJob: Job? = null
    private var requestGeneration = 0

    var state by mutableStateOf(
        PromptPasteUiState(
            settings = repository.loadSettings(),
            actions = repository.loadActions(),
        ),
    )
        private set

    init {
        loadApiKeyDraft()
    }

    fun setInitialInput(text: String?) {
        if (state.input.isBlank() && !text.isNullOrBlank()) state = state.copy(input = text)
    }

    fun updateInput(value: String) {
        state = state.copy(input = value, error = "")
    }

    fun updateOutput(value: String) {
        state = state.copy(output = value)
    }

    fun clearInput() {
        requestGeneration++
        requestJob?.cancel()
        state = state.copy(input = "", output = "", error = "", isRunning = false)
    }

    fun useOutputAsInput() {
        if (state.output.isNotBlank()) state = state.copy(input = state.output, output = "", error = "")
    }

    fun dismissError() {
        state = state.copy(error = "")
    }

    fun cancelRequest() {
        requestGeneration++
        requestJob?.cancel()
        requestJob = null
        state = state.copy(isRunning = false)
    }

    fun run(action: BuiltInAction) {
        runTransform(action.toRequest(state.settings))
    }

    fun run(action: CustomAction) {
        runTransform(action.toRequest())
    }

    private fun runTransform(request: ActionRequest) {
        val text = state.input
        if (text.isBlank()) {
            state = state.copy(error = "Add or select some text first.")
            return
        }
        requestJob?.cancel()
        val generation = ++requestGeneration
        requestJob = viewModelScope.launch {
            state = state.copy(isRunning = true, output = "", error = "")
            try {
                val output = aiClient.transform(
                    text = text,
                    promptTemplate = request.prompt,
                    inputMode = request.inputMode,
                    settings = state.settings,
                    providerOverride = request.providerId,
                    modelOverride = request.model,
                    inputLimit = request.inputLimit,
                    outputLimit = request.outputLimit,
                )
                if (generation == requestGeneration) {
                    state = state.copy(output = output, isRunning = false)
                }
            } catch (error: CancellationException) {
                if (generation == requestGeneration) state = state.copy(isRunning = false)
            } catch (error: Exception) {
                if (generation == requestGeneration) {
                    state = state.copy(
                        isRunning = false,
                        error = error.message ?: "The request failed.",
                    )
                }
            }
        }
    }

    fun reloadPersistentState() {
        val settings = repository.loadSettings()
        state = state.copy(settings = settings, actions = repository.loadActions())
        loadApiKeyDraft()
    }

    fun updateSettings(value: AppSettings) {
        repository.saveSettings(value)
        state = state.copy(settings = value, modelStatus = "")
    }

    fun selectProvider(provider: Provider) {
        if (provider == state.settings.provider) return
        updateSettings(state.settings.copy(provider = provider))
        state = state.copy(availableModels = emptyList(), apiKeyStatus = "")
        loadApiKeyDraft()
    }

    fun setModel(provider: Provider, model: String) {
        val updated = state.settings.copy(models = state.settings.models + (provider.id to model))
        updateSettings(updated)
    }

    fun updateApiKeyDraft(value: String) {
        state = state.copy(apiKeyDraft = value, apiKeyStatus = "")
    }

    fun saveApiKey() {
        val provider = state.settings.provider
        if (!provider.requiresApiKey) return
        runCatching { repository.setApiKey(provider, state.apiKeyDraft) }
            .onSuccess {
                state = state.copy(
                    apiKeyDraft = state.apiKeyDraft.trim(),
                    apiKeyStatus = if (state.apiKeyDraft.isBlank()) "API key removed" else "API key stored securely",
                )
            }
            .onFailure {
                state = state.copy(apiKeyStatus = it.message ?: "Could not store the API key.")
            }
    }

    private fun loadApiKeyDraft() {
        val provider = state.settings.provider
        val value = runCatching { repository.getApiKey(provider) }.getOrDefault("")
        state = state.copy(apiKeyDraft = value, apiKeyStatus = "")
    }

    fun refreshModels() {
        modelsJob?.cancel()
        val provider = state.settings.provider
        modelsJob = viewModelScope.launch {
            state = state.copy(isLoadingModels = true, modelStatus = "", availableModels = emptyList())
            try {
                val models = aiClient.fetchModels(state.settings, provider)
                if (state.settings.provider == provider) {
                    state = state.copy(
                        isLoadingModels = false,
                        availableModels = models,
                        modelStatus = "${models.size} models available",
                    )
                }
            } catch (error: CancellationException) {
                if (state.settings.provider == provider) state = state.copy(isLoadingModels = false)
            } catch (error: Exception) {
                if (state.settings.provider == provider) {
                    state = state.copy(
                        isLoadingModels = false,
                        modelStatus = error.message ?: "Could not load models.",
                    )
                }
            }
        }
    }

    fun saveAction(action: CustomAction) {
        val normalized = action.copy(
            id = action.id.ifBlank { UUID.randomUUID().toString() },
            name = action.name.trim(),
            prompt = action.prompt.trim(),
            model = action.model.trim(),
            inputLimit = action.inputLimit.coerceAtLeast(0),
            outputLimit = action.outputLimit.coerceAtLeast(0),
        )
        if (normalized.name.isBlank() ||
            (normalized.inputMode == InputMode.TRANSFORM && normalized.prompt.isBlank())
        ) {
            state = state.copy(error = "An action needs a name and transformation prompt.")
            return
        }
        val actions = state.actions.toMutableList()
        val index = actions.indexOfFirst { it.id == normalized.id }
        if (index >= 0) actions[index] = normalized else actions += normalized
        persistActions(actions)
    }

    fun deleteAction(action: CustomAction) {
        persistActions(state.actions.filterNot { it.id == action.id })
    }

    fun setActionEnabled(action: CustomAction, enabled: Boolean) {
        persistActions(state.actions.map { if (it.id == action.id) it.copy(enabled = enabled) else it })
    }

    fun moveAction(action: CustomAction, offset: Int) {
        val actions = state.actions.toMutableList()
        val index = actions.indexOfFirst { it.id == action.id }
        val target = index + offset
        if (index !in actions.indices || target !in actions.indices) return
        val moved = actions.removeAt(index)
        actions.add(target, moved)
        persistActions(actions)
    }

    private fun persistActions(actions: List<CustomAction>) {
        repository.saveActions(actions)
        state = state.copy(actions = actions, error = "")
    }
}
