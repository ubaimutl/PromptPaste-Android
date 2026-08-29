package dev.ubai.plyph.network

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.StringRes
import dev.ubai.plyph.R
import dev.ubai.plyph.data.AppSettings
import dev.ubai.plyph.data.InputMode
import dev.ubai.plyph.data.ModelOption
import dev.ubai.plyph.data.Provider
import dev.ubai.plyph.data.SettingsRepository
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AiClient(
    private val repository: SettingsRepository,
    context: Context,
) {
    private val context = context.applicationContext

    suspend fun transform(
        text: String,
        promptTemplate: String,
        inputMode: InputMode,
        settings: AppSettings,
        providerOverride: String = "",
        modelOverride: String = "",
        inputLimit: Int = 0,
        outputLimit: Int = 0,
    ): String {
        val estimatedTokens = estimateTokens(text)
        if (inputLimit > 0 && estimatedTokens > inputLimit) {
            throw AiException(message(R.string.error_input_limit, estimatedTokens, inputLimit))
        }

        val provider = Provider.fromId(providerOverride.ifBlank { settings.provider.id })
        val model = modelOverride.ifBlank { settings.modelFor(provider) }
        if (model.isBlank()) throw AiException(message(R.string.error_choose_model))
        val prompt = expandPromptTemplate(promptTemplate, text, settings)
        val requestedOutput = if (inputMode == InputMode.PROMPT && outputLimit <= 0) 2000 else outputLimit

        return try {
            when (provider) {
                Provider.OLLAMA -> ollama(text, prompt, model, inputMode, requestedOutput, settings)
                Provider.GEMINI -> gemini(text, prompt, model, inputMode, requestedOutput)
                Provider.GROQ -> openAiCompatible(
                    provider,
                    "https://api.groq.com/openai/v1/chat/completions",
                    text,
                    prompt,
                    model,
                    inputMode,
                    requestedOutput,
                )
                Provider.OPENROUTER -> openAiCompatible(
                    provider,
                    "https://openrouter.ai/api/v1/chat/completions",
                    text,
                    prompt,
                    model,
                    inputMode,
                    requestedOutput,
                    mapOf("X-Title" to "Plyph"),
                )
                Provider.CEREBRAS -> openAiCompatible(
                    provider,
                    "https://api.cerebras.ai/v1/chat/completions",
                    text,
                    prompt,
                    model,
                    inputMode,
                    requestedOutput,
                    cerebras = true,
                )
                Provider.OPENAI -> openAiCompatible(
                    provider,
                    "https://api.openai.com/v1/chat/completions",
                    text,
                    prompt,
                    model,
                    inputMode,
                    requestedOutput,
                )
                Provider.VERCEL -> openAiCompatible(
                    provider,
                    "https://ai-gateway.vercel.sh/v1/chat/completions",
                    text,
                    prompt,
                    model,
                    inputMode,
                    requestedOutput,
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: SocketTimeoutException) {
            throw AiException(message(R.string.error_request_timeout))
        } catch (error: UnknownHostException) {
            throw AiException(message(R.string.error_connection))
        } catch (error: IOException) {
            throw AiException(message(R.string.error_connection), error)
        }
    }

    suspend fun fetchModels(settings: AppSettings, provider: Provider): List<ModelOption> {
        val response = when (provider) {
            Provider.OLLAMA -> requestJson(
                "GET",
                "${settings.ollamaUrl.trimEnd('/')}/api/tags",
            )
            Provider.GEMINI -> {
                val key = requiredKey(provider)
                requestJson(
                    "GET",
                    "https://generativelanguage.googleapis.com/v1beta/models?pageSize=1000&key=${encode(key)}",
                )
            }
            else -> {
                val endpoints = mapOf(
                    Provider.GROQ to "https://api.groq.com/openai/v1/models",
                    Provider.OPENROUTER to "https://openrouter.ai/api/v1/models?output_modalities=text",
                    Provider.CEREBRAS to "https://api.cerebras.ai/v1/models",
                    Provider.OPENAI to "https://api.openai.com/v1/models",
                    Provider.VERCEL to "https://ai-gateway.vercel.sh/v1/models",
                )
                requestJson(
                    "GET",
                    endpoints.getValue(provider),
                    mapOf("Authorization" to "Bearer ${requiredKey(provider)}"),
                )
            }
        }
        if (response.status !in 200..299) throw providerError(response, provider, settings.modelFor(provider))

        val models = when (provider) {
            Provider.OLLAMA -> response.data.optJSONArray("models").objects().mapNotNull { item ->
                val id = item.optString("model", item.optString("name"))
                id.takeIf(String::isNotBlank)?.let { ModelOption(it, item.optString("name", it)) }
            }
            Provider.GEMINI -> response.data.optJSONArray("models").objects().mapNotNull { item ->
                val methods = item.optJSONArray("supportedGenerationMethods").strings()
                if ("generateContent" !in methods) return@mapNotNull null
                val id = item.optString("name").removePrefix("models/")
                id.takeIf(String::isNotBlank)?.let { ModelOption(it, item.optString("displayName", it)) }
            }
            else -> response.data.optJSONArray("data").objects().mapNotNull { item ->
                if (item.has("type") && item.optString("type") != "language") return@mapNotNull null
                val id = item.optString("id")
                id.takeIf(String::isNotBlank)?.let { ModelOption(it, item.optString("name", it)) }
            }
        }.filter { model ->
            when (provider) {
                Provider.OPENAI -> Regex("^(gpt-|o\\d)").containsMatchIn(model.id) &&
                    !Regex("audio|image|realtime|search|transcribe|tts", RegexOption.IGNORE_CASE)
                        .containsMatchIn(model.id)
                Provider.GROQ -> !Regex("guard|whisper|tts", RegexOption.IGNORE_CASE).containsMatchIn(model.id)
                else -> true
            }
        }

        return models.distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }

    private suspend fun openAiCompatible(
        provider: Provider,
        endpoint: String,
        text: String,
        prompt: String,
        model: String,
        inputMode: InputMode,
        outputLimit: Int,
        extraHeaders: Map<String, String> = emptyMap(),
        cerebras: Boolean = false,
    ): String {
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages(prompt, text, inputMode))
            .put(if (cerebras) "max_completion_tokens" else "max_tokens", maxTokens(text, outputLimit))
        if (provider == Provider.GROQ && model.startsWith("openai/gpt-oss-")) {
            body.put("reasoning_effort", "low")
            body.put("include_reasoning", false)
        }
        val response = requestJson(
            "POST",
            endpoint,
            extraHeaders + mapOf("Authorization" to "Bearer ${requiredKey(provider)}"),
            body,
        )
        if (response.data.optJSONArray("choices")?.optJSONObject(0)?.optString("finish_reason") == "length") {
            throw outputLimitError()
        }
        if (response.status in 200..299) {
            val output = response.data.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim()
            if (!output.isNullOrEmpty()) return cleanOutput(output, inputMode)
        }
        throw providerError(response, provider, model)
    }

    private suspend fun ollama(
        text: String,
        prompt: String,
        model: String,
        inputMode: InputMode,
        outputLimit: Int,
        settings: AppSettings,
    ): String {
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages(prompt, text, inputMode))
            .put("stream", false)
        if (outputLimit > 0) body.put("options", JSONObject().put("num_predict", outputLimit))
        val response = requestJson("POST", "${settings.ollamaUrl.trimEnd('/')}/api/chat", body = body)
        if (response.data.optString("done_reason") == "length") throw outputLimitError()
        if (response.status in 200..299) {
            val output = response.data.optJSONObject("message")?.optString("content")?.trim()
            if (!output.isNullOrEmpty()) return cleanOutput(output, inputMode)
        }
        throw providerError(response, Provider.OLLAMA, model)
    }

    private suspend fun gemini(
        text: String,
        prompt: String,
        model: String,
        inputMode: InputMode,
        outputLimit: Int,
    ): String {
        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "parts",
                            JSONArray().put(
                                JSONObject().put(
                                    "text",
                                    if (inputMode == InputMode.PROMPT) text else payload(text),
                                ),
                            ),
                        ),
                ),
            )
            .put("generationConfig", JSONObject().put("maxOutputTokens", maxTokens(text, outputLimit)))
        if (prompt.isNotBlank()) {
            body.put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt))),
            )
        }
        val response = requestJson(
            "POST",
            "https://generativelanguage.googleapis.com/v1beta/models/${encode(model)}:generateContent" +
                "?key=${encode(requiredKey(Provider.GEMINI))}",
            body = body,
        )
        val candidate = response.data.optJSONArray("candidates")?.optJSONObject(0)
        if (candidate?.optString("finishReason") == "MAX_TOKENS") throw outputLimitError()
        if (response.status in 200..299) {
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val output = buildString {
                if (parts != null) for (index in 0 until parts.length()) {
                    append(parts.optJSONObject(index)?.optString("text").orEmpty())
                }
            }.trim()
            if (output.isNotEmpty()) return cleanOutput(output, inputMode)
        }
        throw providerError(response, Provider.GEMINI, model)
    }

    private fun messages(prompt: String, text: String, inputMode: InputMode): JSONArray = JSONArray().apply {
        if (prompt.isNotBlank()) put(JSONObject().put("role", "system").put("content", prompt))
        put(
            JSONObject()
                .put("role", "user")
                .put("content", if (inputMode == InputMode.PROMPT) text else payload(text)),
        )
    }

    private fun payload(text: String): String =
        "Transform only the text inside the tags.\nReturn only the transformed text.\n<text>\n$text\n</text>"

    private fun cleanOutput(value: String, inputMode: InputMode): String {
        var output = value.trim()
        if (inputMode == InputMode.PROMPT) return output
        Regex("^<text>\\s*([\\s\\S]*?)\\s*</text>$", RegexOption.IGNORE_CASE)
            .matchEntire(output)?.let { output = it.groupValues[1].trim() }
        Regex("^```(?:text)?\\s*\\n?([\\s\\S]*?)\\n?```$", RegexOption.IGNORE_CASE)
            .matchEntire(output)?.let { output = it.groupValues[1].trim() }
        return output
    }

    private fun estimateTokens(text: String): Int = (text.length + 3) / 4

    private fun maxTokens(text: String, outputLimit: Int): Int =
        if (outputLimit > 0) outputLimit else estimateTokens(text).plus(180).coerceIn(220, 2000)

    private fun requiredKey(provider: Provider): String = repository.getApiKey(provider).ifBlank {
        throw AiException(message(R.string.error_api_key_required, provider.displayName))
    }

    private fun providerError(response: HttpResult, provider: Provider, model: String): AiException {
        val message = when (response.status) {
            401, 403 -> message(R.string.error_api_key_rejected, provider.displayName)
            404 -> message(R.string.error_model_not_found, provider.displayName, model)
            408 -> message(R.string.error_provider_timeout, provider.displayName)
            429 -> message(R.string.error_rate_limit, provider.displayName)
            in 500..599 -> message(R.string.error_provider_unavailable, provider.displayName, response.status)
            else -> message(R.string.error_provider_rejected, provider.displayName, response.status)
        }
        return AiException(message)
    }

    private fun outputLimitError() = AiException(
        message(R.string.error_output_limit),
    )

    private suspend fun requestJson(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: JSONObject? = null,
    ): HttpResult = withContext(Dispatchers.IO) {
        coroutineContext.ensureActive()
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 20_000
            readTimeout = 45_000
            setRequestProperty("Accept", "application/json")
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        val cancellation = coroutineContext.job.invokeOnCompletion { cause ->
            if (cause is CancellationException) connection.disconnect()
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val status = connection.responseCode
            val stream = if (status in 200..399) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val data = if (responseText.isBlank()) JSONObject() else runCatching { JSONObject(responseText) }
                .getOrElse {
                    if (status in 200..299) {
                        throw AiException(
                            message(R.string.error_invalid_response, providerNameFromUrl(url)),
                        )
                    }
                    JSONObject()
                }
            HttpResult(status, data)
        } finally {
            cancellation.dispose()
            connection.disconnect()
        }
    }

    private fun providerNameFromUrl(url: String): String = when {
        "ollama" in url || "127.0.0.1" in url || "10.0.2.2" in url -> "Ollama"
        "groq.com" in url -> "Groq"
        "googleapis.com" in url -> "Gemini"
        "openrouter.ai" in url -> "OpenRouter"
        "cerebras.ai" in url -> "Cerebras"
        "openai.com" in url -> "OpenAI"
        "vercel.sh" in url -> "Vercel AI Gateway"
        else -> message(R.string.error_provider_generic)
    }

    private fun message(@StringRes resourceId: Int, vararg formatArgs: Any): String {
        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val appLocales = context.getSystemService(LocaleManager::class.java).applicationLocales
            if (!appLocales.isEmpty) configuration.setLocales(appLocales)
        }
        return context.createConfigurationContext(configuration).resources
            .getString(resourceId, *formatArgs)
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun JSONArray?.objects(): List<JSONObject> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) optJSONObject(index)?.let(::add)
        }
    }

    private fun JSONArray?.strings(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) optString(index).takeIf(String::isNotBlank)?.let(::add)
        }
    }
}

private data class HttpResult(val status: Int, val data: JSONObject)

class AiException(message: String, cause: Throwable? = null) : Exception(message, cause)

internal fun expandPromptTemplate(prompt: String, text: String, settings: AppSettings): String {
    val variables = mapOf(
        "selection" to text,
        "language" to settings.language,
        "tone" to settings.tone,
        "style" to settings.style,
    )
    return Regex("\\$\\{(selection|language|tone|style)\\}").replace(prompt) { match ->
        variables.getValue(match.groupValues[1])
    }
}
