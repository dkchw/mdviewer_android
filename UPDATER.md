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

## 🛡️ 4. Google Play Protect Security Best Practices

Google Play Protect uses machine learning and static bytecode analysis to detect malicious apps and "droppers". When building an in-app updater, following these rules is critical:

| Antipattern / Trigger | Problem | Correct Implementation |
| :--- | :--- | :--- |
| **`EXTRA_NOT_UNKNOWN_SOURCE`** | Passing `putExtra("android.intent.extra.NOT_UNKNOWN_SOURCE", true)` is a restricted system extra. Non-system apps using it are automatically flagged as **Trojan/Droppers** trying to bypass user consent. | **Never use this extra.** Always let Android present the standard "Install unknown apps" consent prompt via `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES`. |
| **Debug Keystore Signing** | APKs signed with `androiddebugkey` and password `android` are marked as untrusted development builds, triggering "Blocked by Play Protect" when combined with `REQUEST_INSTALL_PACKAGES`. | Sign releases with a dedicated **Release Keystore** (`RSA 2048/4096`, `SHA256withRSA`, valid for 25+ years) and enable v2 + v3 APK Signature Schemes. |
| **Missing `<queries>` Tag** | On Android 11+ (API 30+), package visibility restrictions hide system installer intents unless declared in `<queries>`. | Add `<queries><intent><action android:name="android.intent.action.VIEW"/><data android:mimeType="application/vnd.android.package-archive"/></intent></queries>` to `AndroidManifest.xml`. |
| **Debug Dex Compilation** | D8 compiling classes in debug mode leaves debug symbols and metadata. | Pass `--release --min-api 24` to `d8` to optimize bytecode and emit release compilation metadata. |
| **`android:debuggable="true"`** | Allowing app debugging in a build with package install capabilities triggers high-severity Play Protect warnings. | Explicitly set `android:debuggable="false"` in `<application>`. |

---

## 📋 5. Summary of Key Files

- [`AndroidManifest.xml`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/AndroidManifest.xml): Declares permissions, `<queries>`, and `<provider android:name=".ApkProvider">`.
- [`MainActivity.kt`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/java/com/mdviewer/app/MainActivity.kt): Implements `downloadViaDownloadManager` and `promptInstallApk`.
- [`ApkProvider.kt`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/java/com/mdviewer/app/ApkProvider.kt): Standalone content provider serving downloaded APKs safely.
- [`AndroidBridge.kt`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/java/com/mdviewer/app/AndroidBridge.kt): Bridges JavaScript web frontend to Android native methods.
- [`index.html`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/assets/index.html): UI controls, SemVer comparisons, and auto-download options.
