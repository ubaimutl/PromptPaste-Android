package dev.ubai.plyph

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.ubai.plyph.data.ActionRequest
import dev.ubai.plyph.data.BuiltInAction
import dev.ubai.plyph.data.Provider
import dev.ubai.plyph.data.SettingsRepository
import dev.ubai.plyph.data.toRequest
import dev.ubai.plyph.network.AiClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlyphInputMethodService : InputMethodService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repository by lazy { SettingsRepository(this) }
    private val aiClient by lazy { AiClient(repository, this) }

    private lateinit var rootLayout: LinearLayout
    private lateinit var statusView: TextView
    private lateinit var progressView: ProgressBar
    private lateinit var actionsRow: LinearLayout
    private lateinit var actionsScroll: HorizontalScrollView
    private lateinit var errorContainer: LinearLayout
    private lateinit var errorTextView: TextView
    private lateinit var errorDismissButton: TextView
    private lateinit var reviewContainer: LinearLayout
    private lateinit var reviewHeaderView: TextView
    private lateinit var reviewTextView: TextView
    private lateinit var reviewCancelButton: Button
    private lateinit var reviewReplaceButton: Button
    private lateinit var selectAllButton: ImageButton
    private lateinit var keyboardButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private var actionButtons: List<Button> = emptyList()
    private var requestJob: Job? = null
    private var requestGeneration = 0
    private var passwordField = false
    private var pendingReviewSelection: String? = null

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean = true

    override fun onCreateInputView(): View {
        val darkTheme = isDarkTheme()
        val palette = palette()
        window?.window?.let { imeWindow ->
            imeWindow.navigationBarColor = palette.background
            WindowInsetsControllerCompat(imeWindow, imeWindow.decorView)
                .isAppearanceLightNavigationBars = !darkTheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imeWindow.isNavigationBarContrastEnforced = false
            }
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            minimumHeight = dp(80)
            setPadding(dp(12), dp(8), dp(12), dp(10))
            setBackgroundColor(palette.background)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

            clipChildren = false
            clipToPadding = false
        }
        rootLayout = root

        // Header / Status row
        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            clipChildren = false
            clipToPadding = false

            setPadding(0, dp(4), 0, dp(4))
        }
        statusView = TextView(this).apply {
            text = uiString(R.string.ime_select_text)
            textSize = 13f
            setTextColor(palette.onBackground)
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        progressView = ProgressBar(this).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(dp(20), dp(20)).apply {
                marginEnd = dp(6)
            }
        }
        val settings = repository.loadSettings()
        selectAllButton = iconButton(
            R.drawable.ic_ime_select_all,
            uiString(R.string.ime_select_all),
            palette,
        ).apply {
            visibility = if (settings.showSelectAllInKeyboard) View.VISIBLE else View.GONE
            (layoutParams as? LinearLayout.LayoutParams)?.marginEnd = dp(6)
            setOnClickListener { selectAllText() }
        }
        settingsButton = iconButton(
            R.drawable.ic_ime_settings,
            uiString(R.string.ime_settings),
            palette,
        ).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.marginEnd = dp(6)
            setOnClickListener { openPlyphSettings() }
        }
        keyboardButton = iconButton(
            R.drawable.ic_ime_keyboard,
            uiString(R.string.ime_keyboard),
            palette,
        ).apply {
            setOnClickListener { returnToPreviousKeyboard() }
        }
        statusRow.addView(statusView)
        statusRow.addView(progressView)
        statusRow.addView(selectAllButton)
        statusRow.addView(settingsButton)
        statusRow.addView(keyboardButton)
        root.addView(statusRow)

        // Dedicated Error banner (prevents long error messages from breaking layout)
        errorTextView = TextView(this).apply {
            textSize = 12f
            setTextColor(palette.onErrorContainer)
            maxLines = 4
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        errorDismissButton = TextView(this).apply {
            text = uiString(R.string.ime_dismiss)
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(palette.onErrorContainer)
            setPadding(dp(8), dp(4), dp(4), dp(4))
            setOnClickListener { hideError() }
        }
        errorContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(palette.errorContainer)
                setStroke(dp(1), palette.error)
            }
            addView(errorTextView)
            addView(errorDismissButton)
        }
        val errorParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(6)
        }
        root.addView(errorContainer, errorParams)


        actionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(0, dp(6), 0, dp(6))

            clipChildren = false
            clipToPadding = false
        }

        actionsScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

            clipChildren = false
            clipToPadding = false

            addView(actionsRow)
        }
        root.addView(
            actionsScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )

        // Review state container with vertical scroll view (prevents overflow on long text)
        reviewHeaderView = TextView(this).apply {
            text = uiString(R.string.ime_review_result)
            textSize = 12f
            setTextColor(palette.onSurfaceVariant)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(6))
        }
        reviewTextView = TextView(this).apply {
            textSize = 14f
            setTextColor(palette.onSurface)
            setTextIsSelectable(true)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(palette.surface)
                setStroke(dp(1), palette.stroke)
            }
        }
        val reviewScroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            addView(reviewTextView)
        }
        reviewCancelButton = compactButton(
            uiString(R.string.ime_cancel_review),
            palette,
            primary = false,
        ).apply {
            setOnClickListener { dismissReview() }
        }
        reviewReplaceButton = compactButton(
            uiString(R.string.ime_replace),
            palette,
            primary = true,
        ).apply {
            setOnClickListener { confirmReview() }
        }
        val reviewButtons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            addView(reviewCancelButton)
            addView(space(dp(8)))
            addView(reviewReplaceButton)
        }
        reviewContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, dp(8), 0, 0)
            addView(reviewHeaderView)
            addView(
                reviewScroll,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(130), // Bounded height with full vertical scrolling
                ),
            )
            addView(
                reviewButtons,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = dp(8) },
            )
        }
        root.addView(
            reviewContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )

        refreshLocalizedUi()

        // Keep bottom padding in sync with the navigation bar height so buttons
        // are never hidden behind the back/home/recents bar on 3-button-nav devices.
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(dp(12), dp(8), dp(12), dp(10) + navBar.bottom)
            insets
        }

        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        passwordField = info?.inputType?.let(::isPasswordInput) == true
        if (::actionsRow.isInitialized) {
            refreshLocalizedUi()
            hideReview()
            hideError()
            if (!consumeKeyboardReviewOutcome()) setIdleStatus()
            requestInputViewResize()
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        refreshLocalizedUi()
        consumeKeyboardReviewOutcome()
        if (::reviewContainer.isInitialized && reviewContainer.visibility != View.VISIBLE) {
            requestInputViewResize()
        }
    }

    override fun onFinishInput() {
        cancelRequest(resetUi = false)
        super.onFinishInput()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::rootLayout.isInitialized) {
            rootLayout.post {
                refreshLocalizedUi()
                requestInputViewResize()
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun rebuildActions() {
        val settings = repository.loadSettings()
        if (::selectAllButton.isInitialized) {
            selectAllButton.visibility = if (settings.showSelectAllInKeyboard) View.VISIBLE else View.GONE
        }
        val requests = BuiltInAction.entries.map { action ->
            action.toRequest(settings).copy(label = uiString(action.labelResource()))
        } +
            repository.loadActions().filter { it.enabled }.map { it.toRequest() }
        val palette = palette()
        actionsRow.removeAllViews()
        actionButtons = requests.mapIndexed { index, request ->
            compactButton(request.label, palette, primary = index == 0).also { button ->
                button.setOnClickListener {
                    hideError()
                    runAction(request)
                }
                actionsRow.addView(button)
                if (index != requests.lastIndex) actionsRow.addView(space(dp(8)))
            }
        }
    }

    private fun refreshLocalizedUi() {
        if (!::rootLayout.isInitialized) return
        val configuration = localizedConfiguration()
        if (!configuration.locales.isEmpty) {
            val direction = TextUtils.getLayoutDirectionFromLocale(configuration.locales[0])
            rootLayout.layoutDirection = direction
            actionsRow.layoutDirection = direction
        }
        if (::selectAllButton.isInitialized) selectAllButton.contentDescription = uiString(R.string.ime_select_all)
        if (::settingsButton.isInitialized) settingsButton.contentDescription = uiString(R.string.ime_settings)
        if (::keyboardButton.isInitialized) keyboardButton.contentDescription = uiString(R.string.ime_keyboard)
        errorDismissButton.text = uiString(R.string.ime_dismiss)
        reviewHeaderView.text = uiString(R.string.ime_review_result)
        reviewCancelButton.text = uiString(R.string.ime_cancel_review)
        reviewReplaceButton.text = uiString(R.string.ime_replace)
        rebuildActions()
    }

    private fun localizedConfiguration(): Configuration = Configuration(resources.configuration).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val appLocales = getSystemService(LocaleManager::class.java).applicationLocales
            if (!appLocales.isEmpty) setLocales(appLocales)
        }
    }

    private fun uiString(resourceId: Int, vararg formatArgs: Any): String =
        createConfigurationContext(localizedConfiguration()).resources.getString(resourceId, *formatArgs)

    private fun runAction(request: ActionRequest) {
        if (passwordField) {
            showError(uiString(R.string.ime_password_disabled))
            return
        }
        val connection = currentInputConnection
        val selectedText = runCatching { connection?.getSelectedText(0)?.toString() }.getOrNull()
        if (connection == null || selectedText.isNullOrBlank()) {
            showError(uiString(R.string.ime_no_selection))
            return
        }

        hideError()
        requestJob?.cancel()
        val generation = ++requestGeneration
        val settings = repository.loadSettings()
        setRunning(uiString(R.string.ime_running_action, request.label))
        requestJob = serviceScope.launch {
            try {
                val result = aiClient.transform(
                    text = selectedText,
                    promptTemplate = request.prompt,
                    inputMode = request.inputMode,
                    settings = settings,
                    providerOverride = request.providerId,
                    modelOverride = request.model,
                    inputLimit = request.inputLimit,
                    outputLimit = request.outputLimit,
                )
                if (generation != requestGeneration) return@launch
                if (settings.reviewBeforeKeyboardReplacement && settings.reviewInsideKeyboard) {
                    showReview(selectedText, result)
                } else if (settings.reviewBeforeKeyboardReplacement) {
                    openFullScreenReview(request, selectedText, result, settings.provider)
                } else {
                    replaceUnchangedSelection(selectedText, result)
                }
            } catch (_: CancellationException) {
                if (generation == requestGeneration) setIdleStatus()
            } catch (error: Exception) {
                if (generation == requestGeneration) {
                    showError(error.message ?: uiString(R.string.ime_request_failed))
                }
            }
        }
    }

    private fun BuiltInAction.labelResource(): Int = when (this) {
        BuiltInAction.CORRECT -> R.string.process_text_correct_label
        BuiltInAction.REWRITE -> R.string.process_text_rewrite_label
        BuiltInAction.RUN_PROMPT -> R.string.process_text_run_label
    }

    private suspend fun replaceUnchangedSelection(original: String, result: String) {
        if (passwordField) {
            hideReview()
            showError(uiString(R.string.ime_password_disabled))
            return
        }
        val connection = currentInputConnection
        val currentSelection = runCatching { connection?.getSelectedText(0)?.toString() }.getOrNull()
        if (connection == null || currentSelection != original) {
            hideReview()
            showError(uiString(R.string.ime_selection_changed))
            return
        }

        val committed = runCatching {
            connection.beginBatchEdit()
            try {
                connection.commitText(result, 1)
            } finally {
                connection.endBatchEdit()
            }
        }.getOrDefault(false)
        if (!committed) {
            setReviewControlsEnabled(true)
            showError(uiString(R.string.ime_replace_failed))
            return
        }

        hideReview()
        hideError()
        val palette = palette()
        statusView.setTextColor(palette.primary)
        statusView.text = uiString(R.string.ime_replaced)
        progressView.visibility = View.GONE
        delay(350)
        returnToPreviousKeyboard()
    }

    private fun showReview(original: String, result: String) {
        pendingReviewSelection = original
        reviewTextView.text = result
        actionsScroll.visibility = View.GONE
        reviewContainer.visibility = View.VISIBLE
        progressView.visibility = View.GONE
        settingsButton.isEnabled = true
        setReviewControlsEnabled(true)
        val palette = palette()
        statusView.setTextColor(palette.onBackground)
        statusView.text = uiString(R.string.ime_review_result)
        requestInputViewResize()
    }

    private fun openFullScreenReview(
        request: ActionRequest,
        original: String,
        result: String,
        activeProvider: Provider,
    ) {
        val provider = Provider.entries.firstOrNull { it.id == request.providerId } ?: activeProvider
        KeyboardReviewSession.clear()
        progressView.visibility = View.GONE
        statusView.text = uiString(R.string.ime_opening_review)
        runCatching {
            startActivity(
                KeyboardReviewActivity.createIntent(
                    context = this,
                    original = original,
                    result = result,
                    actionLabel = request.label,
                    providerLabel = provider.displayName,
                ),
            )
        }.onFailure {
            showReview(original, result)
        }
    }

    private fun consumeKeyboardReviewOutcome(): Boolean {
        if (!::statusView.isInitialized) return false
        return when (val outcome = KeyboardReviewSession.consume()) {
            is KeyboardReviewOutcome.Replace -> {
                hideError()
                setRunning(uiString(R.string.ime_replacing))
                requestJob = serviceScope.launch {
                    delay(150)
                    replaceUnchangedSelection(outcome.original, outcome.result)
                }
                true
            }
            KeyboardReviewOutcome.Cancel -> {
                hideReview()
                hideError()
                setIdleStatus()
                requestJob = serviceScope.launch {
                    delay(150)
                    returnToPreviousKeyboard()
                }
                true
            }
            null -> false
        }
    }

    private fun confirmReview() {
        val original = pendingReviewSelection ?: return
        val result = reviewTextView.text.toString()
        setReviewControlsEnabled(false)
        if (::selectAllButton.isInitialized) selectAllButton.isEnabled = false
        if (::settingsButton.isInitialized) settingsButton.isEnabled = false
        val palette = palette()
        statusView.setTextColor(palette.onBackground)
        statusView.text = uiString(R.string.ime_replacing)
        requestJob = serviceScope.launch {
            replaceUnchangedSelection(original, result)
        }
    }

    private fun dismissReview() {
        hideReview()
        setIdleStatus()
    }

    private fun hideReview() {
        pendingReviewSelection = null
        if (!::reviewContainer.isInitialized) return
        reviewContainer.visibility = View.GONE
        actionsScroll.visibility = View.VISIBLE
        setReviewControlsEnabled(true)
        requestInputViewResize()
    }

    private fun requestInputViewResize() {
        if (!::rootLayout.isInitialized) return
        rootLayout.layoutParams = (rootLayout.layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )).apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        rootLayout.requestLayout()
        rootLayout.post {
            rootLayout.requestLayout()
            (rootLayout.parent as? View)?.requestLayout()
            window?.window?.decorView?.requestLayout()
        }
    }

    private fun setReviewControlsEnabled(enabled: Boolean) {
        if (!::reviewReplaceButton.isInitialized) return
        reviewReplaceButton.isEnabled = enabled
        reviewCancelButton.isEnabled = enabled
    }

    private fun setRunning(message: String) {
        val palette = palette()
        statusView.setTextColor(palette.onBackground)
        statusView.text = message
        progressView.visibility = View.VISIBLE
        actionButtons.forEach { it.isEnabled = false }
        if (::selectAllButton.isInitialized) selectAllButton.isEnabled = false
        if (::settingsButton.isInitialized) settingsButton.isEnabled = false
    }

    private fun setIdleStatus() {
        progressView.visibility = View.GONE
        if (::selectAllButton.isInitialized) selectAllButton.isEnabled = !passwordField
        if (::settingsButton.isInitialized) settingsButton.isEnabled = true
        actionButtons.forEach { it.isEnabled = !passwordField }
        val palette = palette()
        statusView.setTextColor(palette.onBackground)
        statusView.text = if (passwordField) {
            uiString(R.string.ime_password_disabled)
        } else {
            uiString(R.string.ime_select_text)
        }
    }

    private fun showError(message: String) {
        progressView.visibility = View.GONE
        if (::selectAllButton.isInitialized) selectAllButton.isEnabled = !passwordField
        if (::settingsButton.isInitialized) settingsButton.isEnabled = true
        if (::reviewContainer.isInitialized && reviewContainer.visibility == View.VISIBLE) {
            setReviewControlsEnabled(true)
        } else {
            actionButtons.forEach { it.isEnabled = !passwordField }
        }
        val palette = palette()
        statusView.setTextColor(palette.error)
        statusView.text = uiString(R.string.ime_request_failed)
        if (::errorContainer.isInitialized) {
            errorTextView.text = message
            errorContainer.visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        if (::errorContainer.isInitialized) {
            errorContainer.visibility = View.GONE
        }
        setIdleStatus()
    }

    private fun cancelRequest(resetUi: Boolean = true) {
        requestGeneration++
        requestJob?.cancel()
        requestJob = null
        if (resetUi && ::statusView.isInitialized) {
            hideError()
            setIdleStatus()
        }
    }

    private fun returnToPreviousKeyboard() {
        cancelRequest(resetUi = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && switchToPreviousInputMethod()) return
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
    }

    private fun openPlyphSettings() {
        cancelRequest(resetUi = false)
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_SETTINGS, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun isPasswordInput(inputType: Int): Boolean {
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return when (inputClass) {
            InputType.TYPE_CLASS_TEXT -> variation in setOf(
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            )
            InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }

    private fun selectAllText() {
        if (passwordField) return
        val connection = currentInputConnection ?: return
        val handled = connection.performContextMenuAction(android.R.id.selectAll)
        if (!handled) {
            val extracted = connection.getExtractedText(ExtractedTextRequest(), 0)
            if (extracted?.text != null && extracted.text.isNotEmpty()) {
                connection.setSelection(0, extracted.text.length)
            }
        }
    }

    private fun iconButton(
        iconRes: Int,
        contentDesc: String,
        palette: Palette,
        sizeDp: Int = 34,
        iconSizeDp: Int = 18,
    ): ImageButton = ImageButton(this).apply {
        setImageResource(iconRes)
        contentDescription = contentDesc
        setColorFilter(palette.onSurface)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        val pad = dp((sizeDp - iconSizeDp) / 2)
        setPadding(pad, pad, pad, pad)
        layoutParams = LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp))
        background = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(palette.surface)
            setStroke(dp(1), palette.stroke)
        }
    }

    private fun compactButton(label: String, palette: Palette, primary: Boolean): Button = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 13f
        setTypeface(typeface, Typeface.BOLD)
        minWidth = 0
        minimumWidth = 0
        minHeight = dp(38)
        minimumHeight = dp(38)
        setPadding(dp(14), 0, dp(14), 0)
        setTextColor(if (primary) palette.onPrimary else palette.onSurface)
        background = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            if (primary) {
                setColor(palette.primary)
            } else {
                setColor(palette.surface)
                setStroke(dp(1), palette.stroke)
            }
        }
    }

    private fun space(width: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(width, 1)
    }

    private fun isDarkTheme(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

    private fun palette(): Palette {
        val dark = isDarkTheme()
        return if (dark) {
            Palette(
                background = Color.rgb(66, 62, 55), // #423E37
                surface = Color.rgb(72, 67, 60), // #48433C
                onBackground = Color.rgb(245, 243, 221), // #F5F3DD
                onSurface = Color.rgb(245, 243, 221), // #F5F3DD
                onSurfaceVariant = Color.rgb(216, 204, 177), // #D8CCB1
                primary = Color.rgb(91, 86, 76), // #5B564C
                onPrimary = Color.rgb(245, 243, 221), // #F5F3DD
                stroke = Color.rgb(109, 103, 91), // #6D675B
                error = Color.rgb(248, 113, 113), // #F87171
                errorContainer = Color.rgb(43, 18, 18), // #2B1212
                onErrorContainer = Color.rgb(252, 165, 165), // #FCA5A5
            )
        } else {
            Palette(
                background = Color.rgb(255, 255, 255), // #FFFFFF
                surface = Color.rgb(250, 250, 250), // #FAFAFA
                onBackground = Color.rgb(23, 23, 23), // #171717
                onSurface = Color.rgb(23, 23, 23), // #171717
                onSurfaceVariant = Color.rgb(102, 102, 102), // #666666
                primary = Color.rgb(23, 23, 23), // #171717
                onPrimary = Color.WHITE,
                stroke = Color.rgb(229, 229, 229), // #E5E5E5
                error = Color.rgb(220, 38, 38), // #DC2626
                errorContainer = Color.rgb(254, 242, 242), // #FEF2F2
                onErrorContainer = Color.rgb(153, 27, 27), // #991B1B
            )
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private data class Palette(
        val background: Int,
        val surface: Int,
        val onBackground: Int,
        val onSurface: Int,
        val onSurfaceVariant: Int,
        val primary: Int,
        val onPrimary: Int,
        val stroke: Int,
        val error: Int,
        val errorContainer: Int,
        val onErrorContainer: Int,
    )
}
