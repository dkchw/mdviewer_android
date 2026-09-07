# MD Viewer for Android

[![Release](https://img.shields.io/github/v/release/dkchw/mdviewer_android?color=blue&logo=github)](https://github.com/dkchw/mdviewer_android/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-green.svg?logo=android)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

An ultra-fast, lightweight, and offline Markdown Outline Viewer ported to Android. Designed to smoothly navigate, fold, and search documents up to **1,000,000 lines** at a fluid 60 FPS, featuring an authentic **Obsidian-style mobile file tree**.

---

## 🚀 Key Features

### ⚡ Blazing Virtualized Performance (Up to 1,000,000 Lines)
- Custom virtualized rendering pipeline that recycles DOM elements, keeping only ~40 rows active in memory regardless of document size.
- Handles massive documents (10k, 100k, and 1,000,000 lines) with instant opening and silky smooth 60 FPS scrolling.
- Multi-level heading folding with binary-search line projection for instantaneous section collapse and expansion.

### 📱 Obsidian-Style Mobile Folder Explorer
- **Authentic Obsidian Tree Layout**: Features vertical indentation guide lines showing exact hierarchical nesting, just like Obsidian Mobile.
- **Phone-Optimized 42px Touch Targets**: Generously sized rows with 14.5px readable typography for effortless one-handed thumb tapping.
- **Vector SVG Icons**: Clean Obsidian-style rotating chevrons (90° smooth CSS transition), folder states (closed 📁 / open 📂), and markdown document icons.
- **Active Note Accent**: Currently open document is highlighted with an accent left border and subtle background glow.
- **Folder Count Badges**: Every directory shows a pill badge displaying the count of markdown notes inside.
- **Vault Toolbar**: Includes dedicated quick buttons for **Expand All Folders (⊞)**, **Collapse All Folders (⊟)**, **Refresh Tree (↻)**, and **Close Vault (✕)**.

### 📂 Recursive Folder Import (Storage Access Framework)
- Open any local directory or Obsidian vault via Android Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`).
- Recursively scans all subdirectories to any depth, automatically filtering out hidden files and noise folders (`.git`, `node_modules`, `.obsidian`, `.trash`, etc.).
- Pre-scans the full tree so browsing and expanding nested folders is instantaneous with zero loading delays.
- Persists opened directory permissions across app restarts.

### 🔍 Dual-Mode Search Bar
- **In-File Search**: Live query matching with line counters, pulse animations, and `▲`/`▼` navigation that automatically un-folds parent headings.
- **Deep Workspace Search**: Recursively searches across all markdown files in the opened vault, displaying matching file names, line numbers, and text snippets. Tapping any match jumps directly to that file and line.
- Can be collapsed/hidden with the top `🔍` button to maximize reading space.

### 🎛️ 5 Dedicated Bottom Toolbar Buttons
1. **Collapse above**: Folds all headings located above the current viewport.
2. **Collapse all**: Folds every heading in the document for an instant high-level outline.
3. **Hover mode toggle**: Toggles outline preview mode.
   - **ON**: Tapping any heading opens a floating preview panel with formatted section content.
   - **OFF**: Tapping headings directly folds/expands them.
4. **Expand all**: Unfolds all headings in the document.
5. **Expand below**: Unfolds all headings located below the current viewport.

### 🛡️ Google Play / CH Play Security Hardened
- **Zero Dangerous Permissions**: Only requires standard `INTERNET` permission for checking releases. Storage is handled purely through Android Storage Access Framework (SAF) without requiring legacy `READ_EXTERNAL_STORAGE`.
- **No `REQUEST_INSTALL_PACKAGES`**: Fully compliant with Google Play Console policies. Update downloads open safely via the user's browser.
- **Hardened WebView (CWE-200 Mitigated)**: Arbitrary local file access and cross-origin file URL access are disabled (`allowFileAccess = false`, `allowFileAccessFromFileURLs = false`, `allowUniversalAccessFromFileURLs = false`).
- **Encrypted Traffic**: Enforces `android:usesCleartextTraffic="false"` and `android:allowBackup="false"`.

---

## 📥 Download & Installation

Download the latest signed APK directly from the Releases page:

- **Latest APK**: [Download `mdviewer.apk`](https://github.com/dkchw/mdviewer_android/releases/latest/download/mdviewer.apk)
- **All Releases**: [GitHub Releases](https://github.com/dkchw/mdviewer_android/releases)
- **Local Path**: [`dist/mdviewer.apk`](dist/mdviewer.apk)

---

## 🛠️ Tech Stack & Architecture

- **Native Layer**: 100% Kotlin ([`MainActivity.kt`](android/app/src/main/java/com/mdviewer/app/MainActivity.kt), [`AndroidBridge.kt`](android/app/src/main/java/com/mdviewer/app/AndroidBridge.kt))
- **Client Engine**: Lightweight single-file virtualized client ([`index.html`](android/app/src/main/assets/index.html)) loaded via `file:///android_asset/`
- **Communication**: Direct two-way `@JavascriptInterface` bridge (zero CORS / zero network latency)
- **Target SDK**: Android 14 (API 34), Minimum SDK: Android 7.0 (API 24)
- **Compiler**: Kotlin 1.9.24 (`kotlinc`) with Android Build-Tools 37.0.0 / 34.0.0

---

## 🔨 Building from Source

### Fast SDK Build (Builds in ~3 seconds)
Requires `ANDROID_HOME` pointing to Android SDK with build-tools and platform android-34 installed:

```bash
git clone https://github.com/dkchw/mdviewer_android.git
cd mdviewer_android
bash android/build_apk.sh
```

The compiled and signed APK will be output to `android/dist/mdviewer.apk` and `dist/mdviewer.apk`.

### Gradle / Android Studio Build
Open the `android/` directory in Android Studio or run via Gradle wrapper:

```bash
cd android
./gradlew assembleRelease
```

---

## 📄 License

Licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
