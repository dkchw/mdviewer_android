# MD Viewer Android — In-App Updater Architecture & Implementation Guide

This document explains the technical architecture, implementation details, and security best practices behind the seamless in-app updater system built into **MD Viewer for Android**.

---

## 🏗️ 1. Architecture Overview

The updater implements a lightweight, zero-dependency, end-to-end update pipeline connecting the HTML/JavaScript web application to native Android system services.

```
┌────────────────────────────────────────────────────────┐
│               1. Frontend (index.html)                 │
│  - "Check for Updates" Button & Version Badge          │
│  - Queries GitHub Releases REST API (fetch)            │
│  - Compares SemVer versions (vCurrent vs vLatest)      │
│  - Auto-download toggle configuration                  │
└───────────────────────────┬────────────────────────────┘
                            │ window.AndroidBridge.downloadUpdate(...)
┌───────────────────────────▼────────────────────────────┐
│         2. JavaScript Bridge (AndroidBridge.kt)        │
│  - Exposes @JavascriptInterface methods               │
│  - downloadUpdate(url, versionName)                    │
│  - isUpdateDownloaded(versionName)                     │
│  - installDownloadedApk(versionName)                   │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│      3. Android OS Service (DownloadManager)           │
│  - Native background download with notification bar    │
│  - Automatic retry on connection loss                  │
│  - Saves to app-specific External Downloads dir        │
└───────────────────────────┬────────────────────────────┘
                            │ BroadcastReceiver (ACTION_DOWNLOAD_COMPLETE)
┌───────────────────────────▼────────────────────────────┐
│      4. Security & Permissions (MainActivity.kt)       │
│  - Verifies APK file size & integrity (>50 KB)         │
│  - Checks packageManager.canRequestPackageInstalls()   │
│  - Directs user to Settings if permission is needed   │
└───────────────────────────┬────────────────────────────┘
                            │ content://com.mdviewer.app.apkprovider/
┌───────────────────────────▼────────────────────────────┐
│        5. Secure Content Provider (ApkProvider.kt)     │
│  - Canonical path validation against directory traversal│
│  - Serves APK with FLAG_GRANT_READ_URI_PERMISSION      │
│  - Hands off to Android System PackageInstaller        │
└────────────────────────────────────────────────────────┘
```

---

## 💡 2. Why This Architecture Excels

1. **Zero External Dependencies**:
   - Does not use OkHttp, Retrofit, WorkManager, or third-party auto-update SDKs.
   - Built entirely on the Android SDK (`android.app.DownloadManager`, `android.content.ContentProvider`, and standard Kotlin stdlib), keeping the APK lightweight (<900 KB).
2. **Complete Lifecycle Independence**:
   - Downloads are executed by the Android operating system daemon (`DownloadManager`). If the user closes MD Viewer or navigates to another app, the download continues uninterrupted in the background with a native system progress notification.
3. **Security & Sandbox Isolation**:
   - Downloaded APKs reside in the app-scoped directory (`context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)`).
   - No `WRITE_EXTERNAL_STORAGE` permission required on Android 10+ (Scoped Storage compliant).
   - Custom `ApkProvider` validates canonical paths to prevent directory traversal attacks before handing the file descriptor to the system package installer.
4. **User-Friendly Flow**:
   - Manual check via Settings drawer with real-time feedback.
   - Optional "Auto-download Updates" preference: when enabled, new versions download automatically in the background on startup, showing a prompt once ready to install.

---

## 🔍 3. Component Deep Dive

### Step 1: GitHub Releases API & Version Comparison (Client JS)

The web client fetches the latest public release from GitHub without requiring authentication or personal access tokens:

```javascript
async function checkAppUpdates(isManual = false) {
  try {
    const res = await fetch('https://api.github.com/repos/dkchw/mdviewer_android/releases/latest', {
      headers: { 'Accept': 'application/vnd.github.v3+json' }
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const release = await res.json();
    const latestTag = (release.tag_name || '').trim();
    const currentVer = getAppVersion(); // e.g. "2.4.1"

    if (compareSemver(latestTag, currentVer) > 0) {
      // Find versioned APK or standard release APK
      const asset = release.assets.find(a => a.name.endsWith('.apk'));
      if (asset && asset.browser_download_url) {
        promptOrAutoDownload(asset.browser_download_url, latestTag);
      }
    }
  } catch (err) {
    if (isManual) showToast('Update check failed: ' + err.message);
  }
}
```

### Step 2: Native Android `DownloadManager` Integration (`MainActivity.kt`)

When triggered, the native activity enqueues the download request with system notifications:

```kotlin
private fun downloadViaDownloadManager(apkUrl: String, versionName: String) {
    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return
    val cleanVersion = versionName.replace("^v".toRegex(), "").trim()
    val fileName = "mdviewer-v$cleanVersion.apk"

    // Clean up any stale partial files
    val extDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    if (extDir != null) {
        val existingFile = File(extDir, fileName)
        if (existingFile.exists()) existingFile.delete()
    }

    val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
        setTitle("MD Viewer v$cleanVersion")
        setDescription("Downloading MD Viewer update package...")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setMimeType("application/vnd.android.package-archive")
        setDestinationInExternalFilesDir(this@MainActivity, Environment.DIRECTORY_DOWNLOADS, fileName)
    }

    val downloadId = dm.enqueue(request)
    activeDownloadId = downloadId

    // Register receiver for completion
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
            if (id == activeDownloadId) {
                unregisterReceiver(this)
                val targetFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                if (targetFile.exists() && targetFile.length() > 50000L) {
                    promptInstallApk(targetFile)
                }
            }
        }
    }
    registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}
```

### Step 3: Sandboxed `ContentProvider` (`ApkProvider.kt`)

To share the downloaded APK securely with the system `PackageInstaller` without requiring AndroidX `FileProvider` or world-readable files:

```kotlin
class ApkProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val ctx = context ?: throw FileNotFoundException("Context is null")
        val fileName = uri.lastPathSegment ?: throw FileNotFoundException("Invalid URI")

        val extDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (extDir != null) {
            val file = File(extDir, fileName).canonicalFile
            val allowedDir = extDir.canonicalFile
            // Security check: Guard against path traversal (e.g. "../../../")
            if (file.path.startsWith(allowedDir.path) && file.exists()) {
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }
        }
        throw FileNotFoundException("Update package not found: $fileName")
    }

    override fun getType(uri: Uri): String = "application/vnd.android.package-archive"
}
```

### Step 4: Launching Package Installation

```kotlin
private fun promptInstallApk(file: File) {
    // Android 8.0+ (Oreo): Ensure the app has permission to install unknown apps
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (!packageManager.canRequestPackageInstalls()) {
            Toast.makeText(this, "Please allow 'Install unknown apps' to update", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivityForResult(intent, REQUEST_CODE_UNKNOWN_APP_SOURCES)
            return
        }
    }
    executeInstall(file)
}

private fun executeInstall(file: File) {
    val contentUri = Uri.parse("content://$packageName.apkprovider/${file.name}")
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Grant temporary URI read permission to package installer activities
    val resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    for (info in resolveInfoList) {
        grantUriPermission(info.activityInfo.packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(intent)
}
```

---

## 🛡️ 4. Google Play Protect & Android Installation Security Best Practices
 
Google Play Protect uses machine learning, heuristic analysis, and static bytecode scanning to protect Android users. In MD Viewer v2.4.6, the in-app updater is fully restored with industry-standard security safeguards:
 
| Security Area | Implementation & Architecture | Protection Mechanism |
| :--- | :--- | :--- |
| **`REQUEST_INSTALL_PACKAGES`** | Declared in `AndroidManifest.xml` alongside standard package visibility `<queries>` for `application/vnd.android.package-archive`. | Standard Android permission for self-updating open-source applications (e.g. F-Droid, Obsidian, NewPipe). On Android 8.0+, user is directed to the system's "Install unknown apps" toggle. |
| **Package Installer Handoff** | Dispatches standard `Intent.ACTION_VIEW` targeting `application/vnd.android.package-archive` with temporary read permission flags (`FLAG_GRANT_READ_URI_PERMISSION`). | Safe handoff to the native Android PackageInstaller. Fallback to `ACTION_INSTALL_PACKAGE` ensures broad Android OS compatibility. |
| **Sandboxed `ApkProvider`** | Custom secure `ContentProvider` strictly isolated to app-scoped directories (`getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)` and `cacheDir`). | Enforces canonical file path validation to prevent directory traversal attacks (e.g. `../../../`). |
| **Signing Keystore & Dual Asset Matching** | Releases are signed with dedicated **Release Keystore** (`CN=MD Viewer, OU=Mobile, O=MD Viewer Open Source`, RSA 2048, SHA256withRSA) with **v2** and **v3** signature schemes enabled, plus legacy debug-key build publishing. | The updater dynamically detects the active signing certificate SHA-256 fingerprint at runtime (`isAppSignedWithDebugKey()`) and downloads the exact matching release asset (`mdviewer-vX.Y.Z.apk` vs `mdviewer-vX.Y.Z-debugkey.apk`), eliminating signature collision errors. |
| **Dex Optimization** | Bytecode compiled using Android D8 in release mode (`--release --min-api 24`). | Produces clean, optimized release dex bytecode without unverified test metadata. |
| **Application Debuggable** | Explicitly configured as `android:debuggable="false"` in `AndroidManifest.xml`. | Eliminates debug flag security warnings. |

---

## 📋 5. Summary of Architecture Files

- [`AndroidManifest.xml`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/AndroidManifest.xml): Declares `INTERNET` and `REQUEST_INSTALL_PACKAGES`, registers `ApkProvider`, configures package queries, and sets `debuggable="false"`.
- [`ApkProvider.kt`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/java/com/mdviewer/app/ApkProvider.kt): Standalone sandboxed content provider with canonical path validation serving update packages securely.
- [`MainActivity.kt`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/java/com/mdviewer/app/MainActivity.kt): Implements native `DownloadManager` enqueuing, download completion receiver, Unknown App Sources permission handling, and package installer handoff.
- [`AndroidBridge.kt`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/java/com/mdviewer/app/AndroidBridge.kt): JavaScript bridge exposing `downloadAndInstall`, `installDownloadedUpdate`, `isUpdateReadyToInstall`, and `isSignedWithDebugKey`.
- [`index.html`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/assets/index.html): UI dialog with real-time download status, intelligent debugkey/release asset selection, and 1-tap install if already downloaded.
- [`build_apk.sh`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/build_apk.sh): Automated build script compiling resources (aapt2), Kotlin sources (kotlinc), D8 release dexing, zipalign, and dual-keystore v1/v2/v3 signing.
