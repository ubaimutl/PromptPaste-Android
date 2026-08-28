# Changelog

All notable changes to PromptPaste for Android are documented here.

## 1.6.1 - 2026-08-28

### Fixed

- Included the Fastlane listing metadata in the tagged release source for F-Droid.
- Disabled AGP dependency metadata in release APKs and app bundles for F-Droid-compatible reproducible builds.

## 1.6.0 - 2026-08-24

### Added

- Vercel-inspired interface with a warm charcoal and cream dark theme.
- Editable custom-action model picker with provider-specific refresh.
- Optional Select All action in the PromptPaste keyboard.

### Improved

- Reduced the optimized signed release APK from about 13 MB to 1.58 MB.
- Matched keyboard and Android navigation colors in light and dark themes.
- Added Select All translations for all supported languages.

### Fixed

- Restored Android 8.0 compatibility for navigation-bar theme resources.
- Fixed Android lint failures in the release workflow.

## 1.5.0 - 2026-08-23

### Added

- Localized app, keyboard, status, and error messages in English, German, Arabic, Spanish, French, Brazilian Portuguese, Hindi, and Simplified Chinese.
- Full-screen review flow for keyboard actions, with inline keyboard review retained as an optional setting.
- App UI and text-replacement workflow images in the README.

### Improved

- Refresh the action keyboard language whenever it opens and use right-to-left layout for Arabic.
- Refined keyboard action spacing and review behavior across supported text editors.
- Keep API keys encrypted with Android Keystore and exclude app data from backups and device transfers.

### Fixed

- Return to the original app after confirming a keyboard replacement.
- Prevent duplicate keyboard inset padding from creating empty space in Editor and Settings.
- Resize PromptPaste correctly when the normal Android keyboard opens.

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
