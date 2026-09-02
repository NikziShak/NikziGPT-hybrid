# NikziGPT hybrid app

NikziGPT is a responsive Vite web app packaged with Capacitor. The same HTML/CSS/JavaScript screens in `src/` run in a browser/PWA and inside native Android or iOS web containers. There is no separate native UI implementation to maintain.

## Run the web app

```bash
npm install
npm run dev
```

Open the local URL, select OpenRouter or NVIDIA in Settings, add that provider's API key, then refresh the model catalog. OpenRouter is filtered to zero-priced models and includes `openrouter/free`; NVIDIA uses its OpenAI-compatible `/v1/chat/completions` API and shows a catalog fallback when the models endpoint is unavailable.

## Build Android

```bash
npm run android:apk
```

This runs a production web build, syncs it into Capacitor, and creates `android/app/build/outputs/apk/debug/app-debug.apk`. Android Studio or a machine with JDK 17 and the Android SDK is required for the final Gradle step.

## Build iOS

On macOS with Xcode installed:

```bash
npm install
npx cap add ios       # first time only
npm run ios:sync
npx cap open ios
```

Xcode then signs and builds the iOS container for a simulator or device. iOS project generation and `.ipa` builds cannot run on Windows because Apple requires Xcode on macOS.

## Desktop apps

The same webpage UI is also wrapped in Electron for Windows, Linux, and macOS:

```bash
npm run desktop:dev       # build web UI and launch desktop shell
npm run desktop:dist      # create a package for the current OS
```

Outputs are written to `release/` (Windows installer/portable executable, Linux AppImage/deb, or macOS dmg/zip). The `Build NikziGPT Desktop` GitHub Actions workflow builds all three platforms as downloadable artifacts.

Each user only needs one download for their operating system:

- Windows: double-click `NikziGPT Setup ... .exe` for a one-click install, or use the portable `.exe`.
- Linux: download `NikziGPT ... .AppImage`, mark it executable once, then double-click it.
- macOS: open the `.dmg` and drag NikziGPT to Applications, then open it normally from Launchpad.

Settings and conversations are persisted locally on the device. API keys are never sent anywhere except the provider endpoint selected in Settings.
