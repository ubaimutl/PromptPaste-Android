package dev.ubai.plyph.network

import dev.ubai.plyph.data.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class AiClientTest {
    @Test
    fun expandsEverySupportedPromptVariable() {
        val settings = AppSettings(
            language = "German",
            tone = "friendly",
            style = "concise",
        )

        val result = expandPromptTemplate(
            "Translate ${'$'}{selection} to ${'$'}{language} in a ${'$'}{tone}, ${'$'}{style} style.",
            "Hello",
            settings,
        )

        assertEquals("Translate Hello to German in a friendly, concise style.", result)
    }

    @Test
    fun leavesUnknownVariablesUnchanged() {
        val result = expandPromptTemplate("Keep ${'$'}{unknown}.", "Text", AppSettings())

        assertEquals("Keep ${'$'}{unknown}.", result)
    }
}
