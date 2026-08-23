<div align="center">

<img src="docs/images/promptpaste-icon.png" alt="PromptPaste Logo" width="100">

# PromptPaste

**AI text assistant for Android**

<br>

[![Latest Release](https://img.shields.io/github/v/release/ubaimutl/PromptPaste-Android?style=flat-square)](https://github.com/ubaimutl/PromptPaste-Android/releases/latest)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8%2B-3DDC84?style=flat-square&logo=android)](https://developer.android.com)

</div>

<br>

PromptPaste is an AI text assistant that lets you correct, rewrite, transform, and run custom AI actions on selected text.

It works with multiple AI providers and allows you to create your own actions using prompts and variables.

Your API keys stay on your device and are protected using Android Keystore.

## Showcase

### App UI

![PromptPaste app interface showing the editor, custom actions, keyboard integration, and provider settings](docs/images/promptpaste-app-ui.png)

### Workflow

Select text → choose an action → review the generated result → replace the original selection.

![PromptPaste workflow from selecting text through reviewing and replacing the result](docs/images/promptpaste-workflow.png)

## Download

[Download the latest Android APK](https://github.com/ubaimutl/PromptPaste-Android/releases/latest)

Requires Android 8.0 or newer.

When installing outside an app store, Android may ask you to allow installation from the browser or file manager used to open the APK.

After installation:

1. Open PromptPaste
2. Select your AI provider
3. Add your API key
4. Choose a model
5. Create your actions

## Features

### AI Actions

- Correct, rewrite, and transform selected text
- Run selected text as an AI prompt
- Create unlimited custom actions
- Use variables:
  - `${selection}`
  - `${language}`
  - `${tone}`
  - `${style}`
- Choose provider, model, and token limits per action
- Preview and edit results before replacing text

### Android Integration

- Replace text directly from Android selection actions
- Optional action keyboard for apps that do not expose selection actions
- No accessibility service
- No background clipboard monitoring

### Supported AI Providers

- Ollama
- OpenAI
- OpenRouter
- Gemini
- Groq
- Cerebras
- Vercel AI Gateway

### Privacy

- API keys encrypted using Android Keystore
- No account required
- No analytics
- No telemetry
- No clipboard monitoring

## How It Works

Android controls how applications can access and modify text from other apps.

PromptPaste uses official Android integrations:

1. Select text in any supported app.
2. Choose a PromptPaste action from the selection menu.
3. PromptPaste processes the text.
4. Review and replace the result.

Some applications do not expose editable text or hide Android selection actions.

For these apps, PromptPaste provides an optional action keyboard.

## Action Keyboard

The optional PromptPaste Actions keyboard provides another way to run AI actions.

1. Enable **PromptPaste Actions** in Android settings.
2. Switch to the PromptPaste keyboard.
3. Choose an action such as Correct, Rewrite, or a custom action.

The keyboard only reads selected text after you explicitly trigger an action.

It does not monitor typing and is disabled in password fields.

## AI Providers

PromptPaste supports:

- Local AI models through Ollama
- Cloud providers through their APIs

Each action can use its own provider and model configuration.

## Privacy

PromptPaste is designed with privacy in mind.

The app:

- Only sends text when you explicitly run an action
- Stores API keys locally using Android Keystore
- Does not collect analytics
- Does not monitor clipboard activity
- Does not use accessibility services
- Does not require an account

Cloud providers have their own privacy policies, retention rules, and usage limits.

Review their policies before sending sensitive information.

## Build

Requirements:

- JDK 17 or newer
- Android SDK Platform 37
- Android SDK Build Tools 36 or newer

Build debug APK:

```bash
./gradlew assembleDebug
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Run verification:

```bash
./gradlew testDebugUnitTest assembleDebug lintDebug
```

## Ollama

For local Ollama servers:

- `127.0.0.1` refers to the Android device itself.
- Android emulators usually reach the host machine through `http://10.0.2.2:11434`.
- Physical devices normally require the computer's local network address.

Ollama must listen on an address reachable by the Android device.

## Related Projects

PromptPaste is available across multiple platforms:

- GNOME Shell extension
- Android app
- Browser extensions

The goal is to provide the same AI text workflow everywhere.

## License

MIT

See [LICENSE](LICENSE).
