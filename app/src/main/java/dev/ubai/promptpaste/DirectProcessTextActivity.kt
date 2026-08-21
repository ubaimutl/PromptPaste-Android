package dev.ubai.promptpaste

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.ubai.promptpaste.data.BuiltInAction
import dev.ubai.promptpaste.ui.DirectProcessTextApp
import dev.ubai.promptpaste.ui.PromptPasteViewModel
import dev.ubai.promptpaste.ui.theme.PromptPasteTheme

abstract class DirectProcessTextActivity(
    private val action: BuiltInAction,
) : ComponentActivity() {
    private val viewModel by viewModels<PromptPasteViewModel>()
    private var readOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        if (selectedText.isNullOrBlank()) {
            closeTransientScreen()
            return
        }

        readOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
        if (viewModel.state.input.isBlank()) viewModel.setInitialInput(selectedText)
        if (!viewModel.state.isRunning &&
            viewModel.state.output.isBlank() &&
            viewModel.state.error.isBlank()
        ) {
            viewModel.run(action)
        }

        setContent {
            PromptPasteTheme {
                DirectProcessTextApp(
                    viewModel = viewModel,
                    action = action,
                    readOnly = readOnly,
                    onFinish = ::finishWithResult,
                    onRetry = { viewModel.run(action) },
                    onCancel = ::cancel,
                    onOpenSettings = ::openSettings,
                )
            }
        }
    }

    private fun finishWithResult(text: String) {
        if (readOnly) {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("PromptPaste result", text))
            Toast.makeText(this, "Result copied", Toast.LENGTH_SHORT).show()
        } else {
            setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, text))
        }
        closeTransientScreen()
    }

    private fun cancel() {
        viewModel.cancelRequest()
        setResult(RESULT_CANCELED)
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

class CorrectProcessTextActivity : DirectProcessTextActivity(BuiltInAction.CORRECT)

class RewriteProcessTextActivity : DirectProcessTextActivity(BuiltInAction.REWRITE)

class RunPromptProcessTextActivity : DirectProcessTextActivity(BuiltInAction.RUN_PROMPT)
