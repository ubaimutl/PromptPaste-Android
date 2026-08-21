package dev.ubai.promptpaste

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class InputMethodManifestTest {
    @Test
    fun inputMethodRequiresAndroidBindingPermission() {
        val manifest = sequenceOf(
            File("app/src/main/AndroidManifest.xml"),
            File("src/main/AndroidManifest.xml"),
        ).firstOrNull(File::isFile)
        assertNotNull("Could not find the app manifest", manifest)

        val document = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(manifest)
        val services = document.getElementsByTagName("service")
        val service = (0 until services.length)
            .map { services.item(it) as Element }
            .firstOrNull { it.androidAttribute("name") == ".PromptPasteInputMethodService" }
        assertNotNull("PromptPaste input method service is missing", service)
        service ?: return

        assertEquals("true", service.androidAttribute("exported"))
        assertEquals("android.permission.BIND_INPUT_METHOD", service.androidAttribute("permission"))
        assertTrue(service.hasNamedChild("action", "android.view.InputMethod"))
        assertTrue(service.hasNamedChild("meta-data", "android.view.im"))
    }

    private fun Element.hasNamedChild(tag: String, name: String): Boolean {
        val nodes = getElementsByTagName(tag)
        return (0 until nodes.length)
            .map { nodes.item(it) as Element }
            .any { it.androidAttribute("name") == name }
    }

    private fun Element.androidAttribute(name: String): String =
        getAttributeNS(ANDROID_NAMESPACE, name)

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
