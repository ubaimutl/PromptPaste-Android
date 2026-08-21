# PromptPaste for Android

[![Android CI](https://github.com/ubaimutl/PromptPaste-Android/actions/workflows/android.yml/badge.svg)](https://github.com/ubaimutl/PromptPaste-Android/actions/workflows/android.yml)
[![Latest release](https://img.shields.io/github/v/release/ubaimutl/PromptPaste-Android)](https://github.com/ubaimutl/PromptPaste-Android/releases/latest)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)

PromptPaste brings the GNOME extension's text actions to Android. It can correct, rewrite, or run selected text as a prompt, supports custom actions, and works with the same providers as the desktop extension.

## Download

[Download the latest Android APK](https://github.com/ubaimutl/PromptPaste-Android/releases/latest)

The app requires Android 8.0 or newer. When installing outside an app store, Android may ask you to allow installation from the browser or file manager you used to open the APK.

After installation, open PromptPaste, choose a provider, enter its API key, and select a model. API keys remain encrypted on the device with Android Keystore.

## Android workflow

Android does not expose another app's current selection or allow silent synthetic paste. PromptPaste uses Android's supported integrations instead:

For the quickest path:

1. Select text in an app.
2. Open the selection menu and choose **Correct**, **Rewrite**, or **Run prompt**. These entries may be under **More**.
3. PromptPaste runs the action and returns the result to replace the selection automatically. Enable **Review before replacing** in Settings to check or edit the result and confirm it first.

Choose **PromptPaste…** instead when you want a custom action or want to review and edit the result before replacing the selection.
All selection-processing screens close as soon as the result is returned and are excluded from Android Recents.

If the source app marks its text read-only, PromptPaste offers **Copy and close**. You can also share plain text to PromptPaste or use its standalone paste-and-copy editor.

The app containing the text controls its selection menu. Some apps and custom fields do not expose Android process-text actions, so PromptPaste cannot force entries into that menu. For those apps, enable the optional **PromptPaste Actions** keyboard from PromptPaste Settings:

1. Tap **Enable keyboard** and enable **PromptPaste Actions** in Android Settings.
2. Return to the source app, select the text, and choose **PromptPaste Actions** from Android's keyboard switcher.
3. Tap Correct, Rewrite, Run prompt, or any enabled custom action.

The compact action keyboard replaces the unchanged selection and switches back to the previous keyboard automatically. **Review before replacing** also applies here and requires Replace or Cancel confirmation first. It reads text only when you tap an action and only through the active selection; actions are disabled in password fields. The share and clipboard workflows remain available when an app does not provide an editable selection.

## Features

- Correct, rewrite, and run-selected-prompt actions
- Direct selection-menu actions with automatic or reviewed replacement
- Optional action keyboard for apps that hide process-text menu entries
- Optional result review before keyboard or selection-menu replacement
- Unlimited custom transform or prompt actions
- `${language}`, `${tone}`, `${style}`, and `${selection}` variables
- Optional provider, model, input-token, and output-token overrides per action
- Editable result preview before copying or replacing
- Provider model discovery with manual model-ID entry
- Ollama, Groq, Gemini, OpenRouter, Cerebras, OpenAI, and Vercel AI Gateway
- API keys encrypted with a non-exportable Android Keystore key
- No account, analytics, background clipboard monitoring, or accessibility service

## Build

Requirements:

- JDK 17 or newer
- Android SDK Platform 37.0
- Android SDK Build Tools 36.0.0 or newer

Open the project in a current Android Studio release, or build from the command line:

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Install it on a connected device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Run the complete local verification suite with:

```bash
./gradlew testDebugUnitTest assembleDebug lintDebug
```

GitHub Actions runs the debug verification suite for every push and pull request. Signed releases are built locally and attached to [GitHub Releases](https://github.com/ubaimutl/PromptPaste-Android/releases); release automation should only be enabled after its signing secrets are configured.

### Signed release APK

Public releases must be signed with the same private key. Create an ignored `keystore.properties` file in the repository root:

```properties
storeFile=/absolute/path/to/promptpaste-release.jks
storePassword=your-private-password
keyAlias=promptpaste
keyPassword=your-private-password
```

Then build:

```bash
./gradlew assembleRelease
```

The signed APK is written to `app/build/outputs/apk/release/app-release.apk`. Never commit the keystore or `keystore.properties`; losing the signing key prevents compatible future updates.

## Release history

See [CHANGELOG.md](CHANGELOG.md) for release notes.

## Ollama

`127.0.0.1` means the Android device itself. An Android emulator usually reaches the host computer at `http://10.0.2.2:11434`; a physical phone normally needs the computer's LAN address. Ollama must listen on an address the device can reach.

Cleartext HTTP is enabled because local Ollama servers commonly use HTTP. All bundled cloud-provider endpoints use HTTPS.

## Privacy

PromptPaste sends text only after you explicitly run an action. Its optional keyboard reads only the selected text after an action button is tapped and is disabled in password fields. The destination is the provider and model selected in Settings. API keys are encrypted locally with Android Keystore and app data is excluded from Android backup.

Cloud providers have their own retention, privacy, pricing, and usage-limit policies. Review the provider and model policy before sending sensitive text.

## License

GPL-3.0, matching the original PromptPaste project. See [LICENSE](LICENSE).
