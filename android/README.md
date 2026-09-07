# MD Viewer for Android

An ultra-fast, lightweight, and offline Markdown Outline Reader ported to Android, capable of smoothly rendering and folding documents up to **1,000,000 lines** at 60 FPS.

## Features

- **Open Entire Folders & Workspaces**:
  - Open any directory on your device via Android Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`).
  - Interactive directory tree explorer in the slide-out drawer with folders and files.
  - Expand and collapse subdirectories with `▸` / `▾` arrows.
  - Automatically remembers the opened folder and restores it upon reopening the app.
  - Close or switch folders at any time.
- **Workspace Search Across Folders**:
  - Search across all Markdown files in the opened folder.
  - Real-time search result list with file names, line numbers, and matching line snippets.
  - Tapping a result automatically opens that file and navigates to the matching line.
- **Blazing Performance**: Pure virtualized scroller that only renders visible viewport DOM elements (~40 elements), achieving instantaneous rendering and fluid 60 FPS scrolling on huge files (100k to 1M lines).
- **Collapsible Top Search Bar**:
  - Toggled with the `🔍` button.
  - Switch between searching "This File" and searching "Folder Workspace".
  - Live query matching with golden target pulse highlight.
  - `▲` and `▼` buttons to jump between matches, automatically expanding folded parent headings.
  - Can be easily hidden to maximize reading space.
- **Bottom Navigation Toolbar (5 Dedicated Buttons)**:
  1. **Collapse above**: Folds all headings located above the current viewing position.
  2. **Collapse all**: Folds every heading in the document for an instant high-level summary.
  3. **Hover mode toggle**: Toggles tap outline preview mode.
     - **ON**: Tapping any heading opens a floating preview panel with formatted section text without navigating away.
     - **OFF**: Tapping any heading toggles folding/expansion directly.
  4. **Expand all**: Unfolds all headings in the document.
  5. **Expand below**: Unfolds all headings located below the current viewing position.
- **Swipe Left-to-Right File Manager**:
  - Swipe horizontally from left to right (or tap `☰`) to slide out the File Manager drawer.
  - Open any single `.md`, `.markdown`, or `.txt` file, or open an entire folder.
  - Access recent files and built-in 10K, 100K, and 1,000,000-line performance stress tests.
- **System Integration**:
  - Registered as a viewer for `.md` and `.txt` files — tap any Markdown file in your Android file manager to open directly with MD Viewer.
  - Hardware Back button support (closes preview panel -> closes workspace search -> closes file drawer -> closes search bar -> exits).
  - Share & copy tools.

## APK Deliverables

- Signed ready-to-install APK: [`dist/mdviewer.apk`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/dist/mdviewer.apk)
- Size: ~154 KB (zero bloat, zero heavy dependencies)

## Building the APK

### Instant Build (via Android SDK build-tools)
```bash
./android/build_apk.sh
```
Builds, dexes, aligns, and signs the APK in 2-3 seconds using the local Android SDK.

### Standard Gradle Build
Open the `android/` directory in Android Studio or run with Gradle wrapper:
```bash
cd android
./gradlew assembleDebug
```
