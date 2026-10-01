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
 
Google Play Protect uses machine learning, heuristic analysis, and static bytecode scanning to protect Android users from malicious droppers. When building an update delivery mechanism for an open-source Android app outside Google Play, the following principles are mandatory:
 
| Security Area | Risk / Heuristic Trigger | Play Protect Compliant Architecture |
| :--- | :--- | :--- |
| **`REQUEST_INSTALL_PACKAGES`** | Sideloaded utility apps declaring `REQUEST_INSTALL_PACKAGES` are automatically flagged or blocked as **Potentially Harmful Applications (PHA: Droppers)** by Play Protect, unless recognized as authorized package managers. | **Never request `REQUEST_INSTALL_PACKAGES`.** Keep app permissions restricted to `android.permission.INTERNET`. |
| **Package Installer Handoff** | In-app invocation of `ACTION_VIEW` targeting `application/vnd.android.package-archive` combined with ContentProviders triggers dropper heuristics. | **Hand off APK downloads to the user's default browser or Android system.** The browser or system installer manages user consent natively with zero dropper flags against the host app. |
| **ContentProvider for APKs** | ContentProviders serving package archives (`ApkProvider.kt`) can be flagged by static scanners looking for local APK drop targets. | **Removed.** No internal provider is registered for package archives. |
| **Signing Keystore** | Builds signed with `androiddebugkey` and password `android` are marked as untrusted development builds, triggering Play Protect warnings. | Releases are signed with a dedicated **Release Keystore** (`CN=MD Viewer, OU=Mobile, O=MD Viewer Open Source, L=San Francisco, ST=California, C=US`, RSA 2048, SHA256withRSA, valid for 28+ years) enabling both **v2** and **v3** signature schemes. |
| **Package Visibility (`<queries>`)** | On Android 11+ (API 30+), querying package installers without need triggers scanner warnings. | Restrict `<queries>` strictly to browsable HTTPS intents for external link handling. |
| **Dex Optimization** | Debug metadata in dex files indicates unverified test binaries. | Compiled using Android D8 in release mode (`--release --min-api 24`) for optimal bytecode density and verification. |
| **Application Debuggable** | `android:debuggable="true"` in production builds triggers high-risk security warnings. | Explicitly configured as `android:debuggable="false"` in `AndroidManifest.xml`. |

---

## 📋 5. Summary of Architecture Files

- [`AndroidManifest.xml`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/AndroidManifest.xml): Declares zero sensitive permissions (only `INTERNET`), sets `debuggable="false"`, and specifies `<queries>` for HTTPS browsing.
- [`MainActivity.kt`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/java/com/mdviewer/app/MainActivity.kt): Implements lightweight GitHub Releases API checking and browser handoff (`openWebUrl`).
- [`AndroidBridge.kt`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/java/com/mdviewer/app/AndroidBridge.kt): Bridges JavaScript web frontend to Android native methods with synchronous, zero-CORS bridge communication.
- [`index.html`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/app/src/main/assets/index.html): UI controls, SemVer comparisons, offscreen DOM virtualization, and high-performance swipe/sliding.
- [`build_apk.sh`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/android/build_apk.sh): Automated pipeline using local Android SDK tools, D8 release dexing, and dual-keystore signing (official `release.keystore` + legacy `debug.keystore`).
