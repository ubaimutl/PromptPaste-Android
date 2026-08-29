package dev.ubai.plyph.data

enum class Provider(
    val id: String,
    val displayName: String,
    val requiresApiKey: Boolean,
    val defaultModel: String,
) {
    OLLAMA("ollama", "Ollama (local)", false, "qwen3:4b"),
    GROQ("groq", "Groq", true, "openai/gpt-oss-20b"),
    GEMINI("gemini", "Gemini", true, "gemini-3.5-flash-lite"),
    OPENROUTER("openrouter", "OpenRouter", true, "openrouter/free"),
    CEREBRAS("cerebras", "Cerebras", true, "gpt-oss-120b"),
    OPENAI("openai", "OpenAI", true, "gpt-4.1-mini"),
    VERCEL("vercel", "Vercel AI Gateway", true, "openai/gpt-5.4-mini");

    companion object {
        fun fromId(id: String?): Provider = entries.firstOrNull { it.id == id } ?: GROQ
    }
}

enum class InputMode { TRANSFORM, PROMPT }

data class CustomAction(
    val id: String,
    val name: String,
    val prompt: String,
    val enabled: Boolean = true,
    val providerId: String = "",
    val model: String = "",
    val inputMode: InputMode = InputMode.TRANSFORM,
    val inputLimit: Int = 0,
    val outputLimit: Int = 0,
)

data class AppSettings(
    val provider: Provider = Provider.GROQ,
    val models: Map<String, String> = Provider.entries.associate { it.id to it.defaultModel },
    val ollamaUrl: String = "http://127.0.0.1:11434",
    val promptCorrect: String = "Correct grammar, spelling, punctuation, clarity, and style. Preserve the language, meaning, and tone. Return only the corrected text, unchanged if already correct.",
    val promptRewrite: String = "Rewrite for clarity and natural flow. Preserve the language, meaning, and tone. Add no ideas or commentary. Return only the improved text.",
    val promptRun: String = "Follow the provided instruction precisely. Produce the requested result directly. Do not add introductory commentary unless requested.",
    val runProviderId: String = "",
    val runModel: String = "",
    val runInputLimit: Int = 0,
    val runOutputLimit: Int = 0,
    val reviewBeforeKeyboardReplacement: Boolean = true,
    val reviewInsideKeyboard: Boolean = false,
    val showSelectAllInKeyboard: Boolean = true,
    val language: String = "English",
    val tone: String = "professional",
    val style: String = "clear and concise",
) {
    fun modelFor(provider: Provider): String = models[provider.id].orEmpty().ifBlank { provider.defaultModel }
}

enum class BuiltInAction(val label: String) {
    CORRECT("Correct"),
    REWRITE("Rewrite"),
    RUN_PROMPT("Run prompt"),
}

data class ModelOption(val id: String, val name: String = id) {
    val label: String get() = if (name == id) id else "$name — $id"
}
