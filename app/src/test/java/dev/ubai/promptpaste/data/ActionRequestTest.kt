package dev.ubai.promptpaste.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionRequestTest {
    @Test
    fun builtInRequestsUseTheSameSettingsAsTheMainApp() {
        val settings = AppSettings(
            promptCorrect = "correct prompt",
            promptRewrite = "rewrite prompt",
            promptRun = "run guidance",
            runProviderId = Provider.OPENAI.id,
            runModel = "test-model",
            runInputLimit = 120,
            runOutputLimit = 240,
        )

        assertEquals(
            ActionRequest("Correct", "correct prompt", InputMode.TRANSFORM),
            BuiltInAction.CORRECT.toRequest(settings),
        )
        assertEquals(
            ActionRequest("Rewrite", "rewrite prompt", InputMode.TRANSFORM),
            BuiltInAction.REWRITE.toRequest(settings),
        )
        assertEquals(
            ActionRequest(
                label = "Run prompt",
                prompt = "run guidance",
                inputMode = InputMode.PROMPT,
                providerId = Provider.OPENAI.id,
                model = "test-model",
                inputLimit = 120,
                outputLimit = 240,
            ),
            BuiltInAction.RUN_PROMPT.toRequest(settings),
        )
    }

    @Test
    fun customRequestKeepsOverridesAndLimits() {
        val action = CustomAction(
            id = "custom-id",
            name = "Shorten",
            prompt = "Make this shorter",
            providerId = Provider.GEMINI.id,
            model = "gemini-test",
            inputMode = InputMode.TRANSFORM,
            inputLimit = 80,
            outputLimit = 40,
        )

        assertEquals(
            ActionRequest(
                label = "Shorten",
                prompt = "Make this shorter",
                inputMode = InputMode.TRANSFORM,
                providerId = Provider.GEMINI.id,
                model = "gemini-test",
                inputLimit = 80,
                outputLimit = 40,
            ),
            action.toRequest(),
        )
    }
}
