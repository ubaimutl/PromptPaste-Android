package dev.ubai.promptpaste

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.ubai.promptpaste.ui.PromptPasteApp
import dev.ubai.promptpaste.ui.PromptPasteViewModel
import dev.ubai.promptpaste.ui.theme.PromptPasteTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<PromptPasteViewModel>()

    companion object {
        const val EXTRA_OPEN_SETTINGS = "dev.ubai.promptpaste.extra.OPEN_SETTINGS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) viewModel.setInitialInput(sharedText(intent))
        setContent {
            PromptPasteTheme {
                PromptPasteApp(
                    viewModel = viewModel,
                    onPaste = { readClipboard() },
                    onCopy = { writeClipboard(it) },
                    onOpenKeyboardSettings = {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                    onShowKeyboardPicker = {
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .showInputMethodPicker()
                    },
                    openSettingsInitially = intent.getBooleanExtra(EXTRA_OPEN_SETTINGS, false),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.updateInput(sharedText(intent).orEmpty())
    }

    private fun sharedText(intent: Intent): String? =
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        } else null

    private fun readClipboard(): String =
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .primaryClip?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()

    private fun writeClipboard(text: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("PromptPaste result", text))
    }
}
