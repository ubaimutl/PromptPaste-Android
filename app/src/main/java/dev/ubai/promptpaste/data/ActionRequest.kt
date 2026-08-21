package dev.ubai.promptpaste.data

data class ActionRequest(
    val label: String,
    val prompt: String,
    val inputMode: InputMode,
    val providerId: String = "",
    val model: String = "",
    val inputLimit: Int = 0,
    val outputLimit: Int = 0,
)

fun BuiltInAction.toRequest(settings: AppSettings): ActionRequest = when (this) {
    BuiltInAction.CORRECT -> ActionRequest(
        label = label,
        prompt = settings.promptCorrect,
        inputMode = InputMode.TRANSFORM,
    )
    BuiltInAction.REWRITE -> ActionRequest(
        label = label,
        prompt = settings.promptRewrite,
        inputMode = InputMode.TRANSFORM,
    )
    BuiltInAction.RUN_PROMPT -> ActionRequest(
        label = label,
        prompt = settings.promptRun,
        inputMode = InputMode.PROMPT,
        providerId = settings.runProviderId,
        model = settings.runModel,
        inputLimit = settings.runInputLimit,
        outputLimit = settings.runOutputLimit,
    )
}

fun CustomAction.toRequest(): ActionRequest = ActionRequest(
    label = name,
    prompt = prompt,
    inputMode = inputMode,
    providerId = providerId,
    model = model,
    inputLimit = inputLimit,
    outputLimit = outputLimit,
)
