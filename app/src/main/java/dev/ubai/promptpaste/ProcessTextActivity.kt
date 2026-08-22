package dev.ubai.promptpaste

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.ubai.promptpaste.ui.ProcessTextApp
import dev.ubai.promptpaste.ui.PromptPasteViewModel
import dev.ubai.promptpaste.ui.theme.PromptPasteTheme

class ProcessTextActivity : ComponentActivity() {
    private val viewModel by viewModels<PromptPasteViewModel>()
    private var readOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        readOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
        if (savedInstanceState == null) {
            viewModel.setInitialInput(intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString())
        }
        setContent {
            PromptPasteTheme {
                ProcessTextApp(
                    viewModel = viewModel,
                    readOnly = readOnly,
                    onCopy = ::copy,
                    onFinish = ::finishWithResult,
                    onClose = ::closeTransientScreen,
                    onOpenSettings = ::openSettings,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.reloadPersistentState()
    }

    private fun copy(text: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText(getString(R.string.clipboard_result_label), text))
    }

    private fun finishWithResult(text: String) {
        if (readOnly) {
            copy(text)
            closeTransientScreen()
            return
        }
        setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, text))
        closeTransientScreen()
    }

    private fun openSettings() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_SETTINGS, true),
        )
        finish()
    }

    private fun closeTransientScreen() {
        if (isTaskRoot) finishAndRemoveTask() else finish()
    }
}
