# MD Viewer for Android

[![Release](https://img.shields.io/github/v/release/dkchw/mdviewer_android?color=blue&logo=github)](https://github.com/dkchw/mdviewer_android/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-green.svg?logo=android)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

An ultra-fast, lightweight, and 100% offline Markdown workspace and outline reader ported to Android. Designed to smoothly navigate, fold, and search massive documents up to **1,000,000 lines** at 60 FPS, featuring an authentic **Obsidian-style mobile file tree**, a **PocketMark notes engine**, and an interactive **Flashcard study & gallery review system**.

---

## 🚀 Key Features

### 1. 🔄 Unified Dual-App Architecture
- **✍️ Basic Editor & PocketMark Notes (Default)**:
  - Designed for drafting notes, prose reading, and daily task management.
  - Multi-note shelf with colored cards, interactive task checklists, tag organizing, and split-screen note comparison.
- **📑 Outline Viewer Mode**:
  - Designed for large-scale documentation, books, research, and full Obsidian vaults.
  - Custom virtualized scroller that keeps only ~40 DOM elements in memory, scrolling smoothly at 60 FPS even on 1,000,000-line files.
  - Seamlessly switch modes anytime via the top header pills (`✍️ Notes` / `📑 Outline`) or the left File Manager drawer.

---

### 2. 🗂️ PocketMark Notes Shelf
- **Full / Compact / Medium Card Previews**:
  - Cycle global preview sizes with the `↕️` shelf button.
- **Dual Compact / Expand Buttons**:
  - Toggle buttons located at **both the top and bottom** of cards (`▲` / `▼`). You can collapse long notes directly from the bottom (`▲`) without scrolling all the way back up, or expand them (`▼`) with one tap.
- **Color Categorization**:
  - Assign notes to 6 Tokyo Night pastel tones (Slate, Teal, Purple, Coral, Amber, Green). Filter notes by color with a single tap.
- **Tag Organization (`#tags`)**:
  - Tag notes with `#work`, `#study`, `#todo`, etc. Tap any tag chip on the shelf bar to instantly filter cards.
- **Interactive Checklists**:
  - Check or uncheck task items (`- [x]`) directly on cards from the shelf without having to open the editor.
- **Multi-Tab Workspace**:
  - Open multiple notes simultaneously in the editor's tab strip for rapid switching.

---

### 3. 📁 Move Notes to Outline Vault
- **Seamless Note Transfer**:
  - Move notes from your Pocket Notes shelf directly into any Outline Viewer Vault on your device storage.
- **Convenient Entry Points**:
  - Accessible via header button (`📁`), editor toolbar (`📁`), shelf card footer (`📁`), and reading mode floating bar (`📁`).
- **Vault & Subfolder Picker**:
  - Choose destination vault from the active vault, recent vaults history, or pick any directory on your device via Android Storage Access Framework (SAF).
  - Subfolder dropdown automatically scans and lists all directories within the target vault (e.g., `/`, `/Notes`, `/Daily`, `/Work/Projects`).
- **Move vs. Copy**:
  - Select *"Delete from Pocket Notes after moving"* for a clean Move, or uncheck to keep a duplicate copy on your shelf (Copy).
- **Instant "View in Outline" Navigation**:
  - Shows an immediate action banner with `View in Outline ➔` to switch to Outline mode, refresh the folder tree, and open the moved document with zero friction.

---

### 4. ✍️ Markdown Editor & Formatting Toolbar
- **Compact Icon-Only Toolbar (Optimized for Mobile)**:
  - Text labels removed across toolbars and headers to eliminate horizontal clutter.
  - Headings & Styles: `H1`, `H2`, `H3`, `B` (Bold `**`), `I` (Italic `*`), `S` (Strikethrough `~~`), `` `code` `` (Inline code).
  - Lists & Elements: `☑` (Task checklists), `•` (Bullet list), `1.` (Numbered list), `>` (Quote block), `</>` (Code block), `▦` (ASCII diagram), `⊞` (Markdown table), `―` (Divider).
  - Links & Obsidian: `[[ ]]` (Wikilink), `🔗` (Markdown link), `[!]` (Obsidian Callout).
  - Controls: `⇄` (Toggle line wrap / monospace grid), `📁` (Move to vault), `↶` / `↷` (Undo / Redo).
- **ASCII Box Diagrams & Grid Alignment**:
  - Tap `▦` to insert structured box-drawing templates.
  - Pixel-perfect monospace alignment without ligature collapsing (`font-feature-settings: "liga" 0, "calt" 0`).
  - Tap `⇄` to toggle between soft line wrap and horizontal scrolling monospace grid.
- **Split Screen Mode (`◫`)**:
  - Preview formatted HTML alongside raw markdown editor, or compare and edit two notes side-by-side with compact pane headers `👁️` (Preview) and `✍️` (Edit).
- **High-Performance Large Text Paste**:
  - Zero-allocation word and line counting algorithms process large text pastes (240k+ characters in 16ms) without locking the UI.

---

### 5. 📑 Outline Viewer & Vault Management
- **Obsidian-Style Mobile Tree Explorer**:
  - Vertical indentation guide lines display exact hierarchical folder nesting.
  - Generous touch targets with rotating vector chevrons and folder note counter badges.
- **Vault Dropdown Switcher**:
  - Tap the vault header title in the drawer to open the Vault Switcher dropdown.
  - Switch active vaults, reorder vaults, rename folders on disk, or remove vaults from history.
- **5 Dedicated Bottom Outline Buttons & Hover Preview**:
  1. **Collapse Above**: Folds all headings located above your current viewport.
  2. **Collapse All**: Folds every heading in the document for an instant high-level outline.
  3. **Hover Preview Mode (`Hover ON/OFF`)**: Toggles instant pop-up section inspection. Tap any heading or row in either Outline or Document view to inspect its content in a floating panel with zoom controls (`A-` / `A+`) and a mode-aware **Jump** button without losing your place.
  4. **Expand All**: Unfolds all headings in the document.
  5. **Expand Below**: Unfolds all headings located below your current viewport.
- **Settings & Controls Integration**:
  - Hover Mode is toggleable from the Bottom Bar and persistent via Settings (`Touch Gestures & Navigation`).
- **Deep Workspace Search**:
  - Search across all markdown files in the opened vault, displaying matching file names, line numbers, and text snippets with direct jump navigation.

---

### 6. 🎴 Flashcard Study & Gallery Review System
- **Automatic Card Generation**:
  - Automatically turns headings, Q&A sections (`Q: ... A: ...`), and term definitions (`Term :: Def`) into interactive flashcards.
- **📁 Folder Deck Mode & Searchable Datalist Dropdown**:
  - Combine all notes across a folder or entire vault into a unified deck.
  - Interactive **type-in dropdown (searchable datalist)** populated with vault subfolders and recent vaults, plus a native **📁 Pick** button to choose any storage directory.
  - Modal floats seamlessly on top of flashcard view (`z-index: 10050`) with dedicated back-button dismissal.
- **🔊 Hardware-Accelerated Audio Engine**:
  - Full native `MediaPlayer` playback for Anki sound tags (`[sound:audio.mp3]`), markdown embeds (`![audio](audio.ogg)`), Obsidian wikilinks (`![[pronunciation.mp3]]`), and HTML5 `<audio>`.
  - Smart multi-directory media resolution (searches `attachments/`, `media/`, `collection.media/`, `sounds/`, note folders, and vault hierarchy).
- **🖼️ Obsidian Wikilink & Markdown Image Support**:
  - Full support for standard markdown images (`![alt](url)`), Obsidian wikilink embeds (`![[diagram.png]]`, `![[photo.png|400]]`), and raw `<img>` tags on both front and back card faces.
  - Tap any card image for full-screen pinch-to-zoom interactive lightbox.
- **Study Flip Mode & Gallery Review**:
  - 3D flip card animation, keyboard/gamepad navigation (`Space`, `Enter`, `R` for replay audio).
  - Gallery mode with 3 face modes (Front Only, Back Only, Both Side-by-Side) and real-time search.

---

### 7. 📖 Interactive In-App User Guide & Helper
- Dedicated interactive Helper Page accessible anytime via the **File Drawer (`📖 User Guide & Helper`)**, **Settings**, or **Options Modal**.
- Features real-time topic search, navigation chips (`🚀 Overview`, `✍️ Pocket Notes`, `📁 Move to Vault`, `✍️ Formatting Toolbar`, `📑 Outline Viewer`, `🎴 Flashcards`, `📱 Gestures`), and a **`📄 Open as Note`** action to load the guide directly into your workspace.

---

### 8. 🛡️ Startup Resilience & Diagnostics
- **Fail-Safe Startup Architecture**:
  - Independent, guarded initialization blocks for saved state, note stores, and zoom configurations.
  - Guarantees immediate, zero-freeze cold startup directly into your active notes or outline workspace.
- **Integrated Diagnostic Logging**:
  - Native Android logging bridge forwarding WebView JS console messages and resource errors directly into Android logcat for full observability.

---

## 🛡️ Security & Google Play Hardening
- **Zero Dangerous Permissions**: Only requires standard `INTERNET` permission for checking releases. Storage is handled purely through Android Storage Access Framework (SAF) without requiring legacy `READ_EXTERNAL_STORAGE`.
- **System Package Installer**: APK updates download cleanly via Android's `DownloadManager` and trigger standard system package installer prompts.
- **Hardened WebView (CWE-200 Mitigated)**: Arbitrary local file access and cross-origin file URL access are disabled (`allowFileAccess = false`, `allowFileAccessFromFileURLs = false`, `allowUniversalAccessFromFileURLs = false`).
- **Encrypted Traffic**: Enforces `android:usesCleartextTraffic="false"` and `android:allowBackup="false"`.

---

## 📥 Download & Installation

Download the latest signed APK directly from the Releases page:

- **Latest APK**: [Download `mdviewer.apk`](https://github.com/dkchw/mdviewer_android/releases/latest/download/mdviewer.apk)
- **Version**: **v3.1.16** (Build 64) — *Full 5,009+ cards deck restoration, ahead-of-time card preloader & instant heading filter cache*
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

### Fast SDK Build (Builds in ~2-3 seconds)
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
