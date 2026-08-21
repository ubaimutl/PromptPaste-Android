# Changelog

All notable changes to PromptPaste for Android are documented here.

## 1.4.0 - 2026-08-22

### Added

- Production release signing support through an ignored local `keystore.properties` file.
- Exact PromptPaste launcher and automatic light/dark top-bar branding.

### Improved

- Shortened selected-text action labels to Correct, Rewrite, and Run prompt.
- Refined the action keyboard layout and navigation-bar spacing.
- Kept result review optional for both selected-text and keyboard workflows.

### Fixed

- Close transient PromptPaste screens immediately after replacement.
- Preserve keyboard visibility and avoid navigation controls covering actions.

## 1.3.1 - 2026-08-20

### Added

- Direct Correct, Rewrite, and Run prompt actions in Android's selected-text menu.
- Optional PromptPaste Actions keyboard for apps that hide selected-text actions.
- Optional review and editing before replacing text from either integration.
- Custom actions with provider, model, input-limit, and output-limit overrides.
- Ollama, Groq, Gemini, OpenRouter, Cerebras, OpenAI, and Vercel AI Gateway support.
- On-device API-key encryption using Android Keystore.

### Fixed

- Made Review before replacing apply to direct selected-text actions as well as the PromptPaste keyboard.
- Close the transient PromptPaste screen immediately after replacement.
