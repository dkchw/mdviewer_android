# MD Viewer for Android

An ultra-fast, lightweight, and offline Markdown workspace and outline reader ported to Android, capable of smoothly rendering, folding, and searching documents up to **1,000,000 lines** at 60 FPS.

## Key Features

- **Unified Dual-App Workspace**:
  - **Basic Editor & PocketMark Notes Engine**: Multi-note shelf, full/compact/medium preview toggles with dual (top & bottom) collapse buttons, 6-color categorization, #tag filtering, checklist toggles on cards, split screen note preview & compare, high-performance large text paste (zero-allocation counters), ASCII box diagram builder with monospace grid alignment and wrap toggle.
  - **Move Notes to Outline Vault**: Transfer notes into device storage vaults with target vault and subfolder picker, Move vs Copy, and instant "View in Outline" navigation.
  - **Outline Viewer Mode**: 1,000,000-line virtualized scroller (60 FPS), Obsidian-style mobile file tree with vertical hierarchy guides and note counters, vault dropdown switcher (reorder, rename on disk, delete), 5 dedicated outline buttons (Collapse Above, Collapse All, Hover Preview, Expand All, Expand Below), deep workspace search.
- **Flashcard Study & Gallery Review System**:
  - Automatic card generation from headings, Q&A (`Q: ... A: ...`), and term definitions (`Term :: Def`).
  - 3D flip card study mode with outline scoping.
  - Gallery Review mode with Front, Back, or Both (side-by-side) views and real-time search filtering.
- **Interactive In-App User Guide & Helper**:
  - Dedicated interactive Helper Page accessible anytime via the File Drawer (`📖 User Guide & Helper`), Settings, or Options Modal.
  - Features real-time topic search, navigation chips (`🚀 Overview`, `✍️ Pocket Notes`, `📁 Move to Vault`, `✍️ Formatting Toolbar`, `📑 Outline Viewer`, `🎴 Flashcards`, `📱 Gestures`), and a `📄 Open as Note` action to load the guide directly into your workspace.
- **System Integration**:
  - Registered as a viewer for `.md` and `.txt` files — tap any Markdown file in your Android file manager to open directly with MD Viewer.
  - Hardware Back button support (dismisses modals -> closes preview panel -> exits).

## APK Deliverables

- Signed ready-to-install APK: [`dist/mdviewer.apk`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/dist/mdviewer.apk)
- Size: ~900 KB (zero bloat, zero heavy dependencies)

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
./gradlew assembleRelease
```
