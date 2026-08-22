package dev.ubai.promptpaste

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ubai.promptpaste.ui.theme.PromptPasteTheme

internal sealed interface KeyboardReviewOutcome {
    data class Replace(val original: String, val result: String) : KeyboardReviewOutcome
    data object Cancel : KeyboardReviewOutcome
}

internal object KeyboardReviewSession {
    private var outcome: KeyboardReviewOutcome? = null

    @Synchronized
    fun replace(original: String, result: String) {
        outcome = KeyboardReviewOutcome.Replace(original, result)
    }

    @Synchronized
    fun cancel() {
        outcome = KeyboardReviewOutcome.Cancel
    }

    @Synchronized
    fun consume(): KeyboardReviewOutcome? = outcome.also { outcome = null }

    @Synchronized
    fun clear() {
        outcome = null
    }
}

class KeyboardReviewActivity : ComponentActivity() {
    private lateinit var originalText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        originalText = intent.getStringExtra(EXTRA_ORIGINAL).orEmpty()
        val result = intent.getStringExtra(EXTRA_RESULT).orEmpty()
        val actionLabel = intent.getStringExtra(EXTRA_ACTION_LABEL).orEmpty()
        val providerLabel = intent.getStringExtra(EXTRA_PROVIDER_LABEL).orEmpty()
        if (originalText.isBlank() || result.isBlank() || actionLabel.isBlank()) {
            cancelReview()
            return
        }

        setContent {
            PromptPasteTheme {
                KeyboardReviewApp(
                    actionLabel = actionLabel,
                    providerLabel = providerLabel,
                    initialResult = result,
                    onReplace = { editedResult ->
                        KeyboardReviewSession.replace(originalText, editedResult)
                        closeTransientScreen()
                    },
                    onCancel = ::cancelReview,
                )
            }
        }
    }

    private fun cancelReview() {
        KeyboardReviewSession.cancel()
        closeTransientScreen()
    }

    private fun closeTransientScreen() {
        if (isTaskRoot) finishAndRemoveTask() else finish()
    }

    companion object {
        private const val EXTRA_ORIGINAL = "dev.ubai.promptpaste.keyboard_review.ORIGINAL"
        private const val EXTRA_RESULT = "dev.ubai.promptpaste.keyboard_review.RESULT"
        private const val EXTRA_ACTION_LABEL = "dev.ubai.promptpaste.keyboard_review.ACTION_LABEL"
        private const val EXTRA_PROVIDER_LABEL = "dev.ubai.promptpaste.keyboard_review.PROVIDER_LABEL"

        fun createIntent(
            context: Context,
            original: String,
            result: String,
            actionLabel: String,
            providerLabel: String,
        ): Intent = Intent(context, KeyboardReviewActivity::class.java)
            .putExtra(EXTRA_ORIGINAL, original)
            .putExtra(EXTRA_RESULT, result)
            .putExtra(EXTRA_ACTION_LABEL, actionLabel)
            .putExtra(EXTRA_PROVIDER_LABEL, providerLabel)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_HISTORY,
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyboardReviewApp(
    actionLabel: String,
    providerLabel: String,
    initialResult: String,
    onReplace: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var result by rememberSaveable(initialResult) { mutableStateOf(initialResult) }
    BackHandler(onBack = onCancel)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.promptpaste_action_title, actionLabel),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            providerLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text(stringResource(R.string.common_cancel)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.review_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.review_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = result,
                onValueChange = { result = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                label = { Text(stringResource(R.string.common_result)) },
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.common_cancel)) }
                Button(
                    onClick = { onReplace(result) },
                    modifier = Modifier.weight(1f),
                    enabled = result.isNotBlank(),
                ) { Text(stringResource(R.string.common_replace_selection)) }
            }
        }
    }
}
