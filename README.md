# NikziGPT - Android AI Chat App

A modern Android chat application inspired by ChatGPT/Claude/Gemini with support for free AI models via OpenRouter.

## Features

- 🤖 **Modern Chat Interface** - Clean, responsive UI inspired by leading AI chat apps
- 🆓 **Free Models** - Access to free models from OpenRouter (NVIDIA, Meta, Google, etc.)
- 🔄 **Streaming Responses** - Real-time token streaming for smooth chat experience
- ⚙️ **Customizable Settings** - Temperature, max tokens, system prompts, API key management
- 🎨 **Dark Theme** - Beautiful Material 3 dark theme with custom colors
- 💾 **Local Storage** - Chat history persisted locally using Room database
- 🔐 **Secure API Key Storage** - Encrypted storage using DataStore Preferences

## Screenshots

*Coming soon*

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 34
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd NikziGPT
```

### 2. Get an OpenRouter API Key

1. Go to [OpenRouter.ai](https://openrouter.ai)
2. Sign up for a free account
3. Generate an API key from your dashboard
4. The free tier includes access to many models

### 3. Build the Project

#### Using Android Studio (Recommended)

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `NikziGPT` folder and select it
4. Wait for Gradle sync to complete
5. Click the "Run" button (green play icon) or press `Shift+F10`

#### Using Command Line

```bash
# Make gradlew executable (Linux/macOS)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

The APK will be located at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### 4. Install on Device

#### Via ADB (Debug Build)
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Via Android Studio
1. Connect your device via USB with USB debugging enabled
2. Click "Run" in Android Studio

## Configuration

### API Key Setup

1. Open the app
2. Tap the Settings icon (gear) in the top bar
3. Enter your OpenRouter API key
4. Tap "Save Settings"

### Model Selection

1. Tap the model icon (robot) in the top bar
2. Browse available free models
3. Tap a model to select it
4. The model will be used for all new chats

### Settings

- **Temperature** (0.0 - 2.0): Controls randomness
  - Lower = more focused/deterministic
  - Higher = more creative/varied
- **Max Tokens** (1 - 32768): Maximum response length
- **System Prompt**: Optional prompt to customize AI behavior
- **Theme**: Dark/Light (Dark is default)

## Architecture

```
NikziGPT/
├── app/
│   ├── src/main/
│   │   ├── java/com/nikzigpt/
│   │   │   ├── data/           # Data models (ChatMessage, AIModel, etc.)
│   │   │   ├── network/        # API client & Retrofit service
│   │   │   ├── repository/     # Data repositories (Room, DataStore)
│   │   │   ├── ui/             # Compose UI (Screens, ViewModels, Components)
│   │   │   │   ├── components/ # Reusable UI components
│   │   │   │   ├── theme/      # Material 3 theme
│   │   │   │   ├── ChatScreen.kt
│   │   │   │   ├── SettingsScreen.kt
│   │   │   │   ├── ModelSelectionScreen.kt
│   │   │   │   ├── ChatViewModel.kt
│   │   │   │   └── SettingsViewModel.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── SettingsActivity.kt
│   │   │   └── NikziGPTApplication.kt
│   │   ├── res/                # Resources (layouts, values, drawables)
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

## Tech Stack

- **Language**: Kotlin 1.9+
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with StateFlow
- **Database**: Room (SQLite)
- **Preferences**: DataStore Preferences
- **Networking**: Retrofit + OkHttp + Kotlinx Serialization
- **Image Loading**: Coil
- **Async**: Kotlin Coroutines + Flow
- **DI**: Manual (ViewModel factories)

## Supported Models (Free Tier)

The app fetches free models from OpenRouter which includes:

- **NVIDIA**: Nemotron 3 Ultra, Nemotron 3 Ultra 550B
- **Meta**: Llama 3.1 8B, 70B, 405B
- **Google**: Gemma 2 9B, 27B
- **Microsoft**: Phi-3 Mini, Medium
- **Mistral**: Mistral 7B, Mixtral 8x7B
- **And many more...**

Models are automatically fetched and cached locally.

## Building Release APK

For a production release:

1. Generate a signing key:
```bash
keytool -genkey -v -keystore nikzigpt-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias nikzigpt
```

2. Create `keystore.properties` in the project root:
```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=nikzigpt
storeFile=../nikzigpt-release-key.jks
```

3. Add signing config to `app/build.gradle.kts`:
```kotlin
android {
    ...
    signingConfigs {
        create("release") {
            val keystoreProperties = Properties().apply {
                load(FileInputStream(rootProject.file("keystore.properties")))
            }
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

4. Build release:
```bash
./gradlew assembleRelease
```

## Troubleshooting

### Gradle Sync Issues
- Ensure you're using JDK 17+
- Try `./gradlew clean` then sync again
- Check `local.properties` has correct SDK path

### API Errors
- Verify your OpenRouter API key is correct
- Check internet connection
- Some models may have rate limits

### Build Failures
- Run `./gradlew clean assembleDebug`
- Ensure all dependencies are downloaded
- Check for conflicting versions

## License

MIT License - Feel free to use and modify!

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Acknowledgments

- [OpenRouter](https://openrouter.ai) for providing free model access
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the modern UI toolkit
- [Material 3](https://m3.material.io/) for the design system
- All the open-source model providers

---

**Made with ❤️ for the Android community**