# AGENT.md — Developer & AI Agent Guidelines for MD Viewer Android

This document defines the architectural conventions, performance invariants, security constraints, and maintenance workflows for autonomous AI agents and human contributors working on the **MD Viewer for Android** codebase.

---

## 1. Architectural Overview

MD Viewer for Android is a high-performance, offline-first Markdown outline viewer capable of rendering and folding documents containing up to **1,000,000 lines** at 60 FPS.

The application uses a **lightweight native Kotlin WebView shell** containing a self-contained, virtualized web client:

```
                  ┌────────────────────────────────────────────────────────┐
                  │                 Android Native Shell                   │
                  │   MainActivity.kt  ◄──►  AndroidBridge.kt              │
                  │   - SAF Folder Access    - Clipboard / Intent Sharing  │
                  │   - Recursive Scanner    - In-browser Update Trigger   │
                  │   - Secure WebView       - Hardware Back Key Cascade   │
                  └───────────────────────────▲────────────────────────────┘
                                              │ @JavascriptInterface
                                              │ (Zero CORS, synchronous)
                                              ▼
                  ┌────────────────────────────────────────────────────────┐
                  │                 Embedded Web Client                    │
                  │            assets/index.html (Vanilla JS)              │
                  │   - 1M-line DOM Virtualizer (~40 active row elements)   │
                  │   - Binary Search Line Folding & Outline Engine        │
                  │   - Obsidian Mobile Tree Explorer (SVG Icons & Lines)  │
                  │   - Dual-Scope Search (File & Deep Workspace)          │
                  │   - 5-Button Action Bar & Hover Preview Panel          │
                  └────────────────────────────────────────────────────────┘
```

### Why This Architecture?
- **Zero Heavy Framework Bloat**: No React Native, Flutter, or Chromium bundle overhead. The compiled release APK is under **1 MB** (~820 KB with full Kotlin standard library).
- **Absolute 60 FPS Virtualization**: Uses pure vanilla JavaScript DOM recycling with constant memory consumption regardless of document length.
- **Fast Local SDK Builds**: Builds, dexes, and signs in ~3 seconds using native tools (`kotlinc`, `aapt2`, `d8`, `zipalign`, `apksigner`) without needing multi-minute Gradle daemons.

---

## 2. Critical Performance Invariants

1. **Virtualized Scroller (`assets/index.html`)**:
   - The DOM must NEVER create row elements for every line in a file. Only visible lines in the viewport + buffer (`~40` rows) are ever attached to `#rows`.
   - Vertical scrolling positions are mapped using a cumulative line-prefix array with binary search (`findSegmentIndex(line)`).
   - Document loading must parse lines in chunks or linear passes without quadratic operations (`O(N)` parsing time, `O(1)` memory per line).

2. **Zero CORS Communication**:
   - In Android WebView, `fetch('file:///...')` or `fetch('/api/...')` is blocked by Chromium CORS security policies (`Failed to fetch`).
   - **RULE**: All communication between the Web UI and the Android system MUST go through `window.AndroidBridge` methods. NEVER introduce relative HTTP `fetch()` endpoints for local data.

3. **Instant Tree Navigation**:
   - Folder scanning in `MainActivity.kt` pre-computes the complete nested tree JSON (`getRecursiveTreeJson()`) so expanding and collapsing subfolders in the Obsidian tree is instantaneous with zero per-click bridge queries.

---

## 3. Google Play & CH Play Security Rules (STRICT)

To ensure the APK always passes automated Google Play Console and Play Protect security scans, the following rules **MUST NEVER BE BROKEN**:

| Security Constraint | Rule | Reason |
| :--- | :--- | :--- |
| **`REQUEST_INSTALL_PACKAGES`** | **NEVER ADD** | Google Play policy strictly rejects apps requesting this permission unless they are app stores or device managers. In-app APK self-installers violate this policy. |
| **`READ_EXTERNAL_STORAGE`** | **NEVER ADD** | Rejected on Android 13+ (API 33+). Storage Access Framework (`ACTION_OPEN_DOCUMENT` & `ACTION_OPEN_DOCUMENT_TREE`) provides all required file/folder access with zero manifest permissions. |
| **WebView File Access** | **KEEP DISABLED** | `allowFileAccess = false`, `allowFileAccessFromFileURLs = false`, and `allowUniversalAccessFromFileURLs = false` must remain disabled. Enabling them triggers Google Play CWE-200 security scan rejections. |
| **Cleartext Traffic** | **KEEP `false`** | `android:usesCleartextTraffic="false"` enforces strict HTTPS for all external API endpoints (e.g. GitHub Releases). |
| **App Backup** | **KEEP `false`** | `android:allowBackup="false"` avoids warnings regarding unencrypted ADB backup extraction. |
| **Updates Delivery** | **BROWSER ONLY** | Updates must trigger `openWebUrl(apkUrl)`, allowing the user's browser or system package installer to handle the download safely. |

---

## 4. UI Design System: Obsidian Mobile Tree

The file explorer in the slide-out drawer follows the **Obsidian Mobile** design language:

1. **42px Touch Target Minimum**:
   - Every file and folder row (`.tree-item-self`) must maintain at least `min-height: 42px;` and `font-size: 14.5px;` for comfortable single-handed mobile tapping.
2. **Indentation Guide Lines**:
   - Subfolder containers (`.tree-item-children`) must have a subtle left guide line:
     ```css
     margin-left: 20px;
     padding-left: 4px;
     border-left: 1.5px solid rgba(255, 255, 255, 0.08);
     ```
3. **Vector SVG Icons**:
   - Use inline crisp SVG paths for chevrons, folders, and markdown documents. Do not substitute with device emojis.
   - Folder chevrons rotate 90° smoothly when expanded via CSS transform transitions.
4. **Active Note Pill**:
   - The currently open note must display an accent left border (`border-left: 3.5px solid #7aa2f7;`) and background tint (`rgba(122, 162, 247, 0.22)`).

---

## 5. Build, Test & Release Workflow

### Fast Build Pipeline (`android/build_apk.sh`)
The script uses local Android SDK tools and `kotlinc` directly:
```bash
bash android/build_apk.sh
```

Steps executed:
1. `aapt2 compile`: Compiles Android resources into `res.zip`.
2. `aapt2 link`: Links resources, manifest, and assets, generating `R.java` and `unaligned.apk`.
3. `kotlinc`: Compiles `MainActivity.kt` and `AndroidBridge.kt` against `android-34/android.jar` and `android/app/libs/kotlin-stdlib.jar`.
4. `d8`: Dexes all compiled `.class` files along with `kotlin-stdlib.jar` into `classes.dex`.
5. `zip`: Bundles `classes.dex` into `unaligned.apk`.
6. `zipalign`: 4-byte page-aligns the APK into `aligned.apk`.
7. `apksigner`: Signs with debug keystore (v2 + v3 schemes) into `android/dist/mdviewer.apk`.

### Verifying Signature
```bash
$ANDROID_HOME/build-tools/37.0.0/apksigner verify --verbose dist/mdviewer.apk
```

### Publishing a New Release
1. Bump `versionCode` and `versionName` in `android/app/src/main/AndroidManifest.xml`.
2. Update `CURRENT_APP_VERSION` in `android/app/src/main/assets/index.html`.
3. Update `getAppVersion()` in `android/app/src/main/java/com/mdviewer/app/AndroidBridge.kt`.
4. Run `bash android/build_apk.sh && cp android/dist/mdviewer.apk dist/mdviewer.apk`.
5. Commit and push to `origin main`:
   ```bash
   git commit -am "feat: description of release"
   git push origin main
   ```
6. Create release on GitHub with GitHub CLI:
   ```bash
   gh release create vX.Y.Z dist/mdviewer.apk --title "MD Viewer for Android vX.Y.Z" --notes "..."
   ```

---

## 6. Code Style & Conventions

- **Kotlin**: Use standard Android Kotlin conventions, scope functions (`apply`, `use`, `let`), null-safety operators, and clean try-use stream management.
- **JavaScript**: Vanilla ES6+ inside an IIFE `(function() { ... })();`. Avoid external npm dependencies or heavy build bundlers.
- **Theme Tokens**: Keep CSS colors consistent with the Tokyo Night dark palette (`#1a1b26` background, `#24283b` surface, `#7aa2f7` accent, `#7dcfff` highlight, `#c0caf5` text, `#565f89` muted).
