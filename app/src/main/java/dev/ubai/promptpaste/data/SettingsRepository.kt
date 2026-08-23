package dev.ubai.promptpaste.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("promptpaste_settings", Context.MODE_PRIVATE)
    private val secureStore = SecureStore(context)

    fun loadSettings(): AppSettings {
        val defaults = AppSettings()
        val models = Provider.entries.associate { provider ->
            provider.id to preferences.getString("model_${provider.id}", provider.defaultModel)
                .orEmpty().ifBlank { provider.defaultModel }
        }
        return AppSettings(
            provider = Provider.fromId(preferences.getString("provider", defaults.provider.id)),
            models = models,
            ollamaUrl = string("ollama_url", defaults.ollamaUrl),
            promptCorrect = string("prompt_correct", defaults.promptCorrect),
            promptRewrite = string("prompt_rewrite", defaults.promptRewrite),
            promptRun = string("prompt_run", defaults.promptRun),
            runProviderId = string("run_provider", defaults.runProviderId),
            runModel = string("run_model", defaults.runModel),
            runInputLimit = preferences.getInt("run_input_limit", defaults.runInputLimit),
            runOutputLimit = preferences.getInt("run_output_limit", defaults.runOutputLimit),
            reviewBeforeKeyboardReplacement = preferences.getBoolean(
                "review_before_keyboard_replacement",
                defaults.reviewBeforeKeyboardReplacement,
            ),
            reviewInsideKeyboard = preferences.getBoolean(
                "review_inside_keyboard",
                defaults.reviewInsideKeyboard,
            ),
            showSelectAllInKeyboard = preferences.getBoolean(
                "show_select_all_in_keyboard",
                defaults.showSelectAllInKeyboard,
            ),
            language = string("variable_language", defaults.language),
            tone = string("variable_tone", defaults.tone),
            style = string("variable_style", defaults.style),
        )
    }

    fun saveSettings(settings: AppSettings) {
        preferences.edit().apply {
            putString("provider", settings.provider.id)
            Provider.entries.forEach { putString("model_${it.id}", settings.modelFor(it)) }
            putString("ollama_url", settings.ollamaUrl)
            putString("prompt_correct", settings.promptCorrect)
            putString("prompt_rewrite", settings.promptRewrite)
            putString("prompt_run", settings.promptRun)
            putString("run_provider", settings.runProviderId)
            putString("run_model", settings.runModel)
            putInt("run_input_limit", settings.runInputLimit)
            putInt("run_output_limit", settings.runOutputLimit)
            putBoolean("review_before_keyboard_replacement", settings.reviewBeforeKeyboardReplacement)
            putBoolean("review_inside_keyboard", settings.reviewInsideKeyboard)
            putBoolean("show_select_all_in_keyboard", settings.showSelectAllInKeyboard)
            putString("variable_language", settings.language)
            putString("variable_tone", settings.tone)
            putString("variable_style", settings.style)
            apply()
        }
    }

    fun getApiKey(provider: Provider): String = if (provider.requiresApiKey) secureStore.get(provider) else ""

    fun setApiKey(provider: Provider, value: String) {
        if (provider.requiresApiKey) secureStore.set(provider, value)
    }

    fun loadActions(): List<CustomAction> = runCatching {
        val array = JSONArray(preferences.getString("custom_actions", "[]"))
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optString("id")
                val name = item.optString("name").trim()
                val prompt = item.optString("prompt")
                val mode = if (item.optString("inputMode") == "prompt") InputMode.PROMPT else InputMode.TRANSFORM
                if (id.isBlank() || name.isBlank() || (mode == InputMode.TRANSFORM && prompt.isBlank())) continue
                add(
                    CustomAction(
                        id = id,
                        name = name,
                        prompt = prompt,
                        enabled = item.optBoolean("enabled", true),
                        providerId = item.optString("provider"),
                        model = item.optString("model"),
                        inputMode = mode,
                        inputLimit = item.optInt("inputLimit").coerceAtLeast(0),
                        outputLimit = item.optInt("outputLimit").coerceAtLeast(0),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())

    fun saveActions(actions: List<CustomAction>) {
        val array = JSONArray()
        actions.forEach { action ->
            array.put(
                JSONObject()
                    .put("id", action.id)
                    .put("name", action.name)
                    .put("prompt", action.prompt)
                    .put("enabled", action.enabled)
                    .put("provider", action.providerId)
                    .put("model", action.model)
                    .put("inputMode", action.inputMode.name.lowercase())
                    .put("inputLimit", action.inputLimit)
                    .put("outputLimit", action.outputLimit),
            )
        }
        preferences.edit { putString("custom_actions", array.toString()) }
    }

    private fun string(key: String, defaultValue: String): String =
        preferences.getString(key, defaultValue) ?: defaultValue
}
