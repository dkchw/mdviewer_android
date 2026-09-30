package com.mdviewer.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

class AndroidBridge(
    private val activity: MainActivity,
    private val webView: WebView
) {
    private val handler = Handler(Looper.getMainLooper())
    private val prefs: SharedPreferences =
        activity.getSharedPreferences("mdviewer_prefs", Context.MODE_PRIVATE)

    @JavascriptInterface
    fun openNativeFilePicker() {
        handler.post { activity.openFileChooser() }
    }

    @JavascriptInterface
    fun openNativeFolderPicker() {
        handler.post { activity.openFolderChooser() }
    }

    @JavascriptInterface
    fun openFolderByUri(uriString: String?): Boolean {
        if (uriString.isNullOrEmpty()) return false
        return activity.openFolderByUri(uriString)
    }

    @JavascriptInterface
    fun pickDefaultBasicSaveFolder() {
        handler.post { activity.openDefaultSaveFolderChooser() }
    }

    @JavascriptInterface
    fun getDefaultBasicSaveFolderUri(): String? {
        return activity.getDefaultBasicSaveFolderUri()
    }

    @JavascriptInterface
    fun getDefaultBasicSaveFolderName(): String? {
        return activity.getDefaultBasicSaveFolderName()
    }

    @JavascriptInterface
    fun setDefaultBasicSaveFolder(uriString: String?, folderName: String?) {
        if (uriString.isNullOrEmpty() || folderName.isNullOrEmpty()) return
        activity.setDefaultBasicSaveFolder(uriString, folderName)
    }

    @JavascriptInterface
    fun clearDefaultBasicSaveFolder() {
        activity.clearDefaultBasicSaveFolder()
    }

    @JavascriptInterface
    fun closeFolder() {
        handler.post { activity.closeCurrentFolder() }
    }

    @JavascriptInterface
    fun getRecursiveTree(): String {
        return activity.getRecursiveTreeJson()
    }

    @JavascriptInterface
    fun getTreeChildren(docId: String?): String {
        return activity.getTreeChildrenJson(docId ?: "")
    }

    @JavascriptInterface
    fun readTreeFile(docId: String?): String {
        return activity.readTreeFileContent(docId ?: "")
    }

    @JavascriptInterface
    fun searchFolder(query: String?): String {
        return activity.searchFolderJson(query ?: "")
    }

    @JavascriptInterface
    fun readNativeFile(uriString: String?): String {
        return activity.readNativeFileContent(uriString ?: "")
    }

    @JavascriptInterface
    fun writeNativeFile(uriString: String?, content: String?): String {
        if (uriString.isNullOrEmpty() || content == null) return "ERROR: Invalid params"
        return activity.writeNativeFileContent(uriString, content)
    }

    @JavascriptInterface
    fun writeTreeFile(docId: String?, content: String?): String {
        if (docId.isNullOrEmpty() || content == null) return "ERROR: Invalid params"
        return activity.writeTreeFileContent(docId, content)
    }

    @JavascriptInterface
    fun writeFileInTreeUri(treeUriString: String?, docId: String?, content: String?): String {
        if (docId.isNullOrEmpty() || content == null) return "ERROR: Invalid params"
        return activity.writeFileInTreeUri(treeUriString, docId, content)
    }

    @JavascriptInterface
    fun createTreeFile(fileName: String?, content: String?): String {
        if (fileName.isNullOrEmpty() || content == null) return "ERROR: Invalid params"
        return activity.createTreeFileContent(fileName, content)
    }

    @JavascriptInterface
    fun createFileInTreeUri(treeUriString: String?, fileName: String?, content: String?): String {
        if (fileName.isNullOrEmpty() || content == null) return "ERROR: Invalid params"
        return activity.createFileInTreeUri(treeUriString, fileName, content)
    }

    @JavascriptInterface
    fun saveFileAs(suggestedName: String?, content: String?) {
        handler.post {
            activity.openCreateFileChooser(suggestedName ?: "Document.md", content ?: "")
        }
    }

    @JavascriptInterface
    fun downloadUpdateOnly(apkUrl: String?, versionName: String?) {
        activity.downloadUpdateOnly(apkUrl ?: "", versionName ?: "")
    }

    @JavascriptInterface
    fun installDownloadedUpdate() {
        activity.installDownloadedApk()
    }

    @JavascriptInterface
    fun isUpdateReadyToInstall(): Boolean {
        return activity.isUpdateDownloaded()
    }

    @JavascriptInterface
    fun downloadAndInstall(apkUrl: String?, versionName: String?) {
        activity.downloadAndInstallApk(apkUrl ?: "", versionName ?: "")
    }

    @JavascriptInterface
    fun openExternalUrl(url: String?) {
        activity.openWebUrl(url ?: "")
    }

    @JavascriptInterface
    fun checkLatestRelease(): String {
        return activity.checkGitHubRelease()
    }

    @JavascriptInterface
    fun setFullscreen(fullscreen: Boolean) {
        activity.setSystemUiFullscreen(fullscreen)
    }

    @JavascriptInterface
    fun getAppVersion(): String {
        return try {
            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            pInfo.versionName ?: "1.4.2"
        } catch (e: Exception) {
            "1.4.2"
        }
    }

    @JavascriptInterface
    fun showToast(message: String?) {
        if (message.isNullOrEmpty()) return
        handler.post {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun copyToClipboard(text: String?) {
        if (text == null) return
        handler.post {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Markdown Content", text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(activity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun shareText(text: String?, title: String?) {
        if (text == null) return
        handler.post {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, title ?: "Share Markdown")
            activity.startActivity(shareIntent)
        }
    }

    @JavascriptInterface
    fun savePreference(key: String?, value: String?) {
        if (key != null) {
            prefs.edit().putString(key, value).apply()
        }
    }

    @JavascriptInterface
    fun getPreference(key: String?, defaultValue: String?): String? {
        return if (key != null) prefs.getString(key, defaultValue) else defaultValue
    }

    @JavascriptInterface
    fun closeApp() {
        handler.post { activity.finish() }
    }
}
