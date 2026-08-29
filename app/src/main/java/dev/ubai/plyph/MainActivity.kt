package dev.ubai.plyph

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
import dev.ubai.plyph.ui.PlyphApp
import dev.ubai.plyph.ui.PlyphViewModel
import dev.ubai.plyph.ui.theme.PlyphTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<PlyphViewModel>()

    companion object {
        const val EXTRA_OPEN_SETTINGS = "dev.ubai.plyph.extra.OPEN_SETTINGS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) viewModel.setInitialInput(sharedText(intent))
        setContent {
            PlyphTheme {
                PlyphApp(
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
            .setPrimaryClip(ClipData.newPlainText(getString(R.string.clipboard_result_label), text))
    }
}
