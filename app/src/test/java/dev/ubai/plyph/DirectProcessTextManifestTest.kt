package dev.ubai.plyph

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class DirectProcessTextManifestTest {
    @Test
    fun processTextActivitiesRemainRegisteredWithDistinctLabels() {
        val manifest = sequenceOf(
            File("app/src/main/AndroidManifest.xml"),
            File("src/main/AndroidManifest.xml"),
        ).firstOrNull(File::isFile)
        assertNotNull("Could not find the app manifest", manifest)

        val document = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(manifest)
        val activities = document.getElementsByTagName("activity")
        val expected = mapOf(
            ".ProcessTextActivity" to "@string/process_text_open_label",
            ".CorrectProcessTextActivity" to "@string/process_text_correct_label",
            ".RewriteProcessTextActivity" to "@string/process_text_rewrite_label",
            ".RunPromptProcessTextActivity" to "@string/process_text_run_label",
        )
        val actual = buildMap {
            for (index in 0 until activities.length) {
                val activity = activities.item(index) as Element
                if (!activity.hasProcessTextFilter()) continue
                assertEquals("true", activity.androidAttribute("exported"))
                assertEquals("true", activity.androidAttribute("noHistory"))
                assertEquals("true", activity.androidAttribute("excludeFromRecents"))
                put(activity.androidAttribute("name"), activity.androidAttribute("label"))
            }
        }

        assertEquals(expected, actual)
        assertEquals(expected.size, actual.values.toSet().size)
    }

    private fun Element.hasProcessTextFilter(): Boolean {
        val actions = getElementsByTagName("action")
        for (index in 0 until actions.length) {
            val action = actions.item(index) as Element
            if (action.androidAttribute("name") == "android.intent.action.PROCESS_TEXT") return true
        }
        return false
    }

    private fun Element.androidAttribute(name: String): String =
        getAttributeNS(ANDROID_NAMESPACE, name)

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
