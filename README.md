# VoicePay Alert

Native Android app (Kotlin) that listens for UPI payment notifications from **PhonePe**, **Google Pay**, **Paytm**, **BHIM**, and **Amazon Pay**, then announces them with **Text-to-Speech** in English, Hindi, or Gujarati.

## Features

- Notification listener for supported payment apps
- Payment text parsing (amount + sender)
- Multi-language TTS announcements
- Room database payment history
- Settings: voice on/off, language, volume, sender name, dark mode, per-app toggles, custom templates
- Export history, auto-start on boot, foreground keep-alive service
- Material Design UI with splash, home, settings, and history screens

## Folder structure

```
voiceapp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/voicepay/alert/
│       │   ├── VoicePayApplication.kt
│       │   ├── data/          # Room entities, DAO, database, repositories
│       │   ├── service/       # NotificationListener, BootReceiver, KeepAlive
│       │   ├── tts/           # TtsManager
│       │   ├── util/          # PaymentParser, PaymentApps, helpers
│       │   └── ui/            # Activities, ViewModels, adapters
│       └── res/               # layouts, values, drawables
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/wrapper/
```

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Physical device or emulator with **API 26+** (Android 8.0+)

## Setup in Android Studio

1. Open Android Studio → **File → Open** → select the `voiceapp` folder.
2. Wait for Gradle sync to finish (Android Studio downloads the wrapper automatically).
3. If sync fails, create `local.properties` in the project root:

   ```properties
   sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk
   ```

   (See `local.properties.example`.)

4. Connect a phone with **USB debugging** enabled, or start an emulator (API 26+).
5. Click **Run** (green play button) or **Shift+F10**.

## Build APK

### Debug APK (quick test)

```bash
cd voiceapp
./gradlew assembleDebug
```

APK path:

`app/build/outputs/apk/debug/app-debug.apk`

### Release APK

1. Create a keystore (one-time):

   ```bash
   keytool -genkey -v -keystore voicepay-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias voicepay
   ```

2. Add signing config in `app/build.gradle.kts` (signingConfigs block) or use **Build → Generate Signed Bundle / APK** in Android Studio.

3. Build:

   ```bash
   ./gradlew assembleRelease
   ```

Release APK:

`app/build/outputs/apk/release/app-release.apk`

On Windows PowerShell, use `.\gradlew.bat assembleDebug` after opening the project once in Android Studio (so the Gradle wrapper JAR is present).

## Install on a physical device

### Option A — USB (Android Studio)

1. On the phone: **Settings → About phone** → tap **Build number** 7 times → enable **Developer options**.
2. Enable **USB debugging**.
3. Connect USB, accept the debugging prompt on the phone.
4. Run the app from Android Studio.

### Option B — APK file

1. Copy `app-debug.apk` to the phone (USB, email, or cloud).
2. Open the APK and allow **Install unknown apps** if prompted.
3. Open **VoicePay Alert**.

## First-time app setup (required)

1. **Notification access**  
   Home → **Enable Notification Access** → enable **VoicePay Alert** in system settings.

2. **Battery optimization** (recommended)  
   Tap **Battery Optimization** → allow unrestricted / ignore optimizations for VoicePay Alert.  
   On Xiaomi/Samsung/Oppo, also disable battery restrictions for this app in manufacturer settings.

3. **Settings**  
   - Choose language (English / Hindi / Gujarati)  
   - Tap **Test voice**  
   - Adjust volume and toggles  

4. **Test**  
   Receive a small UPI payment (or ask a friend) via PhonePe/GPay/Paytm and confirm voice + history entry.

## Supported apps (package names)

| App        | Package |
|-----------|---------|
| PhonePe   | `com.phonepe.app` |
| Google Pay | `com.google.android.apps.nbu.paisa.user` |
| Paytm     | `net.one97.paytm` |
| BHIM      | `in.org.npci.upiapp` |
| Amazon Pay | `in.amazon.mShop.android.shopping` |

## Architecture

- **MVVM**: `MainViewModel`, `SettingsViewModel`, `HistoryViewModel`
- **Repository**: `PaymentRepository`, `SettingsRepository` (DataStore)
- **Room**: `PaymentNotificationEntity`, `AppDatabase`
- **Services**: `PaymentNotificationListenerService`, `ListenerKeepAliveService`, `BootReceiver`

## Permissions

- Notification listener (user must enable in system settings)
- `RECEIVE_BOOT_COMPLETED` — auto-start
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_SPECIAL_USE` — optional keep-alive
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` — battery guide
- `POST_NOTIFICATIONS` (Android 13+) — foreground service notification

## Troubleshooting

| Issue | Fix |
|-------|-----|
| No voice | Enable notification access; turn voice on in Settings; test voice |
| No detection | Confirm payment app is enabled in Settings app toggles |
| Stops in background | Disable battery optimization; enable keep-alive in Settings |
| Gujarati/Hindi wrong | Install Google TTS language packs in device **Text-to-speech** settings |
| Amazon Pay | Shopping app notifications may differ; ensure payment notifications are enabled in Amazon app |

## License

Provided as-is for personal/educational use.
