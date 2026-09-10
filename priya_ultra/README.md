# PRIYA — Personal AI Companion (Android)

A voice-controlled Android assistant: Kotlin + Jetpack Compose + Material 3,
Gemini/OpenAI/custom-endpoint AI backends, on-device speech recognition + TTS,
and Accessibility-Service-based device control (open apps, tap, type, scroll,
back/home/recents).

## Option A — No computer / no Android Studio (build from your phone)

This project is a **custom app you build yourself** — it was never submitted
to the Play Store, so it will never show up there no matter what. That's
expected, not an error. You install it directly ("sideload") instead. Android
Studio itself only ever runs on a PC/Mac/Linux laptop — it can't run on a
phone — but you don't actually need it: GitHub can build the APK for you for
free, in the cloud, from just a browser.

1. On your phone browser, go to **github.com** → sign up for a free account
   (if you don't have one).
2. Create a **New repository** (name it e.g. `priya-assistant`), keep it
   **Public**, don't add a README (this project already has one).
3. On the new repo's page, tap **"uploading an existing file"** and upload
   every file/folder from the unzipped `PriyaAssistant` folder (GitHub's web
   uploader lets you drag a whole folder — on mobile, use the "Add file →
   Upload files" screen and select everything from the extracted zip,
   including the hidden `.github` folder — you may need a file manager app
   that shows hidden folders, e.g. **Cx File Explorer** or **Solid
   Explorer**, to see and select `.github`).
4. Once uploaded, go to the **Actions** tab of your repo. A workflow called
   **"Build PRIYA Debug APK"** should already be queued/running automatically
   (it's defined in `.github/workflows/build.yml`, already included).
5. Wait 3–6 minutes for it to finish (green checkmark).
6. Open that finished run → scroll down to **Artifacts** → download
   **`priya-debug-apk`**. It downloads as a `.zip` — open it to get
   `app-debug.apk`.
7. On your Redmi: Settings → allow "Install unknown apps" for whichever app
   you used to open the file (Files/Chrome), then tap the `.apk` to install.

Your Redmi's 2GB RAM is only relevant to *running* PRIYA (2GB is fine — it's
a normal, lightweight app), not to building it — the build itself always
happens on GitHub's servers, never on your phone.

## Option B — With a computer (Android Studio)

## 1. Open the project

1. Install **Android Studio** (Ladybird/Koala or newer).
2. `File → Open` → select the `PriyaAssistant/` folder (the one containing
   `settings.gradle.kts`).
3. Let Gradle sync. **First sync note:** this project's `gradle-wrapper.jar`
   binary was not included (it was generated outside a build environment with
   no way to fetch the official jar). Android Studio will detect this and
   offer **"Repair/Regenerate Gradle Wrapper"** — accept it. If it doesn't
   prompt automatically, do one of:
   - `File → Settings → Build Tools → Gradle` → point it at a local Gradle
     8.7 install instead of the wrapper, **or**
   - If you have Gradle installed locally, run once from a terminal in the
     project root: `gradle wrapper --gradle-version 8.7` (this generates the
     missing jar using the `gradle-wrapper.properties` already in the repo).

## 2. Build

```
./gradlew assembleDebug
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## 3. Install on a device/emulator

```
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or just click **Run ▶** in Android Studio with a device/emulator selected.

## 4. Enable Accessibility (required for device-control commands)

PRIYA can chat and speak without this, but "open YouTube", "tap the button",
"scroll down", etc. need it:

1. Open the app → tap the settings gear (top right of Home) → **Accessibility**.
2. Tap **Enable Accessibility**. This opens Android's own Accessibility
   settings screen.
3. Find **PRIYA** in the list → toggle it on → confirm the system dialog.
4. Return to the app — the status dot on Home turns green ("Accessibility on").

## 5. Enter your AI provider API key

1. Settings → **AI Provider**.
2. Pick **Gemini**, **OpenAI**, or **Custom OpenAI-compatible**.
3. (Custom only) enter the Base URL, e.g. `https://your-endpoint.com/v1`.
4. Paste your API key → **Save**. Optionally tap **Test Connection** first.
5. The key is encrypted on-device via the Android Keystore
   (`EncryptedSharedPreferences`) and is shown only as a masked string
   (`••••••••1a2b`) afterward — never re-displayed in full.

Without any key configured, PRIYA still opens apps, searches the web, and
uses on-device navigation commands — it just can't hold an AI conversation
until a provider is set up (spec §27, "offline mode").

## 6. Permissions

Microphone and (Android 13+) notification permissions are requested on first
launch. You can review/re-grant them anytime in Settings → **Permissions**.

## What's implemented

- Full voice loop: SpeechRecognizer → AI → optional device action → TTS,
  with auto-restart/backoff for continuous listening and Android TTS as a
  guaranteed fallback if no external TTS is configured.
- Three AI provider backends behind one interface, all user-configurable,
  no hardcoded keys or models.
- Accessibility-based device control with a real `AccessibilityService`,
  friendly failure messages instead of crashes, and a confirmation dialog
  gate for consequential actions.
- Local, unencrypted-but-private conversation history (JSON on app storage);
  nothing is sent anywhere except the trimmed slice sent to your chosen AI
  provider when you send a message.
- Full settings surface: AI provider/model/key, voice engine/language/
  speed/pitch, accessibility status + explainer, permissions status,
  privacy explainer, about screen.
- Futuristic dark/crimson themed UI with a state-driven animated core
  (distinct animation per Idle/Listening/Thinking/Speaking/Executing/Error).

## Known limitations to be aware of

- **No source avatar image existed** to adapt (the spec references an
  optional `public/priya-avatar.jpg` from a prior web project — none was
  provided), so the avatar is a procedural circular Compose graphic instead
  of a photo. Drop a real image into `res/drawable-nodpi/priya_avatar.png`
  and swap `PriyaAvatar` (in `ui/components/CommonComponents.kt`) to an
  `Image(painterResource(...))` if you have one.
- **True multilingual auto-detection** (Nepali/Hindi/English in one
  utterance) isn't something Android's public `SpeechRecognizer` API
  actually exposes — "Auto" mode uses the device's default locale for
  recognition, and for speech *output* it does a simple Devanagari-script
  check to choose Hindi vs. English. This is the honest ceiling of the
  public API, not a shortcut.
- The action-confirmation list (`ActionManager.CONFIRMATION_REQUIRED`)
  reserves `SEND_MESSAGE` / `DELETE_CONTENT` / `MAKE_PURCHASE` /
  `CHANGE_SETTING` for future action types — the current action set
  (open/click/type/scroll/nav) doesn't include anything that consequential
  yet, so nothing currently triggers that dialog. Add new action types
  there as you extend `ActionManager`/`PriyaPersonality.actionsContract`
  together.
- `QUERY_ALL_PACKAGES` is declared to let PRIYA check/launch arbitrary
  installed apps by package name; if you publish to Play Store you'll need
  to justify this permission or narrow it to an explicit `<queries>` list
  of the specific packages in `AppLauncher.knownPackages`.

## PRIYA ULTRA PRO MAX additions

This build expands the action layer for voice-first Android automation:
- Multi-action JSON workflows (sequential execution in one command).
- Generic installed-app discovery by friendly name, in addition to common app aliases.
- Accessibility coordinate tap fallback and custom swipe gestures.
- Fuzzy/contains text matching for buttons and visible labels.
- Media play/pause control through Accessibility.
- Voice commands for media volume and screen brightness (brightness requires Android Write Settings permission).
- Consequential actions such as posting, sending, deleting, purchasing, or calling are confirmation-gated when implemented by a provider/action.

Android still controls microphone/background-service behavior and third-party app permissions. PRIYA cannot legitimately bypass those OS/app restrictions.
