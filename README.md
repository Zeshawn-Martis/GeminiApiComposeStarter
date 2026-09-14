# Gemini API Jetpack Compose Starter (MAD Lab Assignment 1)

Enhanced Android application demonstrating integration with Google Gemini AI (`gemini-3.6-flash`), built using **100% Jetpack Compose (Material 3)**, **Room Database**, **Preferences DataStore**, and **Android Keystore AES-256-GCM Encryption**.

---

## 🚀 Key Features

1. **Jetpack Compose Modern UI**:
   - Material 3 theme with dynamic Light/Dark mode support.
   - `LazyColumn` conversation view with user and Gemini chat bubbles, auto-scrolling to latest messages.
   - Copy response to clipboard & prompt retry functionality.
   - Confirmation dialog for clearing local chat history.

2. **Model Selection**:
   - Live model selector dropdown allowing dynamic switching between `gemini-3.6-flash`, `gemini-1.5-flash`, and `gemini-1.5-pro`.

3. **Multi-Modal Voice Input**:
   - Integrated Speech-to-Text input using `RecognizerIntent` and `rememberLauncherForActivityResult`.

4. **Persistence & Offline Access**:
   - **Room Database**: Persists chat history across app restarts.
   - **Preferences DataStore**: Stores user theme preferences and selected Gemini model.

5. **Security & Cryptography (Mandatory)**:
   - **API Key Security**: Key stored strictly in git-ignored `local.properties` (or CI environment variables) and exposed via `BuildConfig.GEMINI_API_KEY`.
   - **Android Keystore AES-256-GCM Encryption**: User prompt data is encrypted using Android Keystore cryptographic keys prior to persisting in Room DB storage.
   - **R8 Code Obfuscation**: `isMinifyEnabled = true` configured for release builds.

---

## 🔑 Setup & Configuration

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Zeshawn-Martis/GeminiApiComposeStarter.git
   cd GeminiApiComposeStarter
   ```

2. **Configure Gemini API Key**:
   - Obtain an API key from [Google AI Studio](https://aistudio.google.com/).
   - Open (or create) `local.properties` in the project root directory.
   - Add your key:
     ```properties
     GEMINI_API_KEY=your_actual_gemini_api_key_here
     ```

3. **Build & Run**:
   - Open the project in Android Studio (Jellyfish or newer recommended).
   - Build and run on an Android Emulator or physical device (Android 8.0+ / API 26+).

---

## 🔐 Security Architecture & Production Recommendations

### Local Key Protection
- The API key is loaded dynamically from `local.properties` or environment variables during Gradle build time into `BuildConfig`.
- Prompts persisted in the local Room database are encrypted at rest using AES-256-GCM via keys generated inside the **Android Keystore System**.

### Production Best Practices
In a production deployment, client-side API keys carry inherent risks. Recommended production safeguards include:
1. **Backend Proxy Server**: Route Gemini API requests through a secure backend proxy (e.g., Firebase Cloud Functions or Node.js backend) to avoid embedding API keys in the client APK.
2. **Firebase App Check**: Attest device and app integrity to prevent unauthorized clients from invoking your API.
3. **API Key Restrictions**: Restrict Google AI Studio keys by HTTP referrers or Android package name and SHA-1 certificate fingerprint.

---

## 🧪 Running Tests

### Unit Tests
Run unit tests for `ChatViewModel`:
```bash
./gradlew test
```

### UI Tests
Run Compose UI tests on an emulator:
```bash
./gradlew connectedAndroidTest
```
