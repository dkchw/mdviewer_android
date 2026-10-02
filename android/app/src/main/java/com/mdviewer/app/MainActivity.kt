package com.mdviewer.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.annotation.TargetApi
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Collections
import java.util.Comparator
import java.util.HashSet
import java.util.LinkedList
import java.util.Queue

class MainActivity : Activity() {

    companion object {
        private const val TAG = "MDViewer"
        private const val REQUEST_CODE_FILE_CHOOSER = 1001
        private const val REQUEST_CODE_FOLDER_CHOOSER = 1002
        private const val REQUEST_CODE_CREATE_FILE = 1003
        private const val REQUEST_CODE_DEFAULT_SAVE_FOLDER = 1004
        private const val REQUEST_CODE_UNKNOWN_APP_SOURCES = 1005
    }

    private var mWebView: WebView? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mPendingIntentUri: Uri? = null
    private var mPendingSharedText: String? = null
    private var mPendingCreateContent: String? = null
    private var mCurrentTreeUri: Uri? = null
    private lateinit var mPrefs: SharedPreferences

    private var downloadReceiver: BroadcastReceiver? = null
    private var activeDownloadId: Long = -1L
    private var pendingInstallFile: File? = null
    private var lastDownloadedApkFile: File? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPrefs = getSharedPreferences("mdviewer_prefs", Context.MODE_PRIVATE)
        val savedTree = mPrefs.getString("last_folder_tree_uri", null)
        if (savedTree != null) {
            try {
                mCurrentTreeUri = Uri.parse(savedTree)
            } catch (e: Exception) {
                mCurrentTreeUri = null
            }
        }

        window.decorView.setBackgroundColor(0xFF1a1b26.toInt())

        val layout = FrameLayout(this).apply {
            setBackgroundColor(0xFF1a1b26.toInt())
        }

        val webView = WebView(this).apply {
            setBackgroundColor(0xFF1a1b26.toInt())
        }
        mWebView = webView

        layout.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(layout)

        // Secure WebSettings hardened for Google Play security scans
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            // Hardened: disallow arbitrary local file access from WebView (Play Store security requirement)
            allowFileAccess = false
            allowContentAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            useWideViewPort = true
            loadWithOverviewMode = true
            displayZoomControls = false
            builtInZoomControls = false
        }

        webView.addJavascriptInterface(AndroidBridge(this, webView), "AndroidBridge")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                wv: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                mFilePathCallback?.onReceiveValue(null)
                mFilePathCallback = filePathCallback

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf(
                            "text/plain",
                            "text/markdown",
                            "text/x-markdown",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                }
                return try {
                    startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER)
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Cannot launch file picker", e)
                    mFilePathCallback = null
                    false
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            @Suppress("OVERRIDE_DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {}
                    return true // Block WebView from loading remote URLs
                }
                if (url.startsWith("file:///android_asset/")) {
                    return false // Allow local assets
                }
                return true // Block everything else
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {}
                    return true // Block WebView from loading remote URLs
                }
                if (url.startsWith("file:///android_asset/")) {
                    return false // Allow local assets
                }
                return true // Block everything else
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                handlePendingIntentData()

                mCurrentTreeUri?.let { treeUri ->
                    val folderName = getFolderName(treeUri)
                    val rootDocId = getRootDocumentId(treeUri)
                    val uriStr = treeUri.toString()
                    webView.post {
                        notifyFolderOpened(folderName, rootDocId, uriStr, isStartup = true)
                    }
                }
            }
        }

        handleIntent(intent)

        // Clear WebView cache upon version change to guarantee fresh assets
        val currentVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
        val prefs = getSharedPreferences("mdviewer_prefs", Context.MODE_PRIVATE)
        val lastLoadedVersion = prefs.getString("last_loaded_app_version", null)
        if (lastLoadedVersion != currentVersion) {
            webView.clearCache(true)
            prefs.edit().putString("last_loaded_app_version", currentVersion).apply()
            // Clean up stale downloaded APKs from previous versions to prevent
            // the updater from falsely detecting an old APK as a pending update
            cleanupStaleUpdateApks(currentVersion)
        }

        webView.loadUrl("file:///android_asset/index.html")
    }

    fun openFileChooser() {
        mFilePathCallback?.onReceiveValue(null)
        mFilePathCallback = null

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("text/plain", "text/markdown", "text/x-markdown", "application/octet-stream", "*/*")
            )
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch file chooser", e)
        }
    }

    fun openCreateFileChooser(suggestedName: String, initialContent: String) {
        mPendingCreateContent = initialContent
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            putExtra(Intent.EXTRA_TITLE, suggestedName)
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_CREATE_FILE)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch create file chooser", e)
        }
    }

    fun openFolderChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_FOLDER_CHOOSER)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch folder chooser", e)
        }
    }

    fun closeCurrentFolder() {
        mCurrentTreeUri = null
        mPrefs.edit().remove("last_folder_tree_uri").remove("last_folder_name").apply()
        mWebView?.evaluateJavascript("if(window.onFolderClosed){ window.onFolderClosed(); }", null)
    }

    private fun notifyFolderOpened(folderName: String, rootDocId: String, treeUri: String = mCurrentTreeUri?.toString() ?: "", isStartup: Boolean = false) {
        val js = "if(window.onFolderOpened){ window.onFolderOpened(" +
                "${JSONObject.quote(folderName)}, ${JSONObject.quote(rootDocId)}, ${JSONObject.quote(treeUri)}, $isStartup); }"
        mWebView?.evaluateJavascript(js, null)
    }

    fun openFolderByUri(uriString: String): Boolean {
        return try {
            val treeUri = Uri.parse(uriString)
            mCurrentTreeUri = treeUri
            val folderName = getFolderName(treeUri)
            val rootDocId = getRootDocumentId(treeUri)
            mPrefs.edit().putString("last_folder_tree_uri", uriString).putString("last_folder_name", folderName).apply()
            runOnUiThread {
                notifyFolderOpened(folderName, rootDocId, uriString, isStartup = false)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open folder by uri: $uriString", e)
            false
        }
    }

    fun renameFolder(treeUriString: String?, newFolderName: String): String {
        val response = JSONObject()
        val trimmed = newFolderName.trim()
        if (trimmed.isEmpty()) {
            response.put("status", "error")
            response.put("message", "Folder name cannot be empty")
            return response.toString()
        }

        val targetTreeUri = if (!treeUriString.isNullOrEmpty()) {
            try { Uri.parse(treeUriString) } catch (e: Exception) { mCurrentTreeUri }
        } else {
            mCurrentTreeUri
        } ?: run {
            response.put("status", "error")
            response.put("message", "No folder specified or opened")
            return response.toString()
        }

        try {
            val rootDocId = getRootDocumentId(targetTreeUri)
            if (rootDocId.isEmpty()) {
                response.put("status", "error")
                response.put("message", "Cannot find root folder ID")
                return response.toString()
            }

            val docUri = DocumentsContract.buildDocumentUriUsingTree(targetTreeUri, rootDocId)
            val renamedDocUri = DocumentsContract.renameDocument(contentResolver, docUri, trimmed)
            if (renamedDocUri == null) {
                response.put("status", "error")
                response.put("message", "Storage provider did not allow renaming this folder")
                return response.toString()
            }

            val newRootDocId = try {
                DocumentsContract.getDocumentId(renamedDocUri)
            } catch (e: Exception) {
                rootDocId
            }

            val newTreeUri = try {
                DocumentsContract.buildTreeDocumentUri(targetTreeUri.authority, newRootDocId)
            } catch (e: Exception) {
                renamedDocUri
            }

            try {
                contentResolver.takePersistableUriPermission(
                    newTreeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (ignored: Exception) {}

            val isCurrent = (mCurrentTreeUri != null && (mCurrentTreeUri == targetTreeUri || mCurrentTreeUri.toString() == targetTreeUri.toString()))
            if (isCurrent) {
                mCurrentTreeUri = newTreeUri
                mPrefs.edit().putString("last_folder_tree_uri", newTreeUri.toString()).putString("last_folder_name", trimmed).apply()
            }

            response.put("status", "ok")
            response.put("oldUri", targetTreeUri.toString())
            response.put("newUri", newTreeUri.toString())
            response.put("newName", trimmed)
            response.put("newRootDocId", newRootDocId)
            response.put("isCurrent", isCurrent)

            if (isCurrent) {
                runOnUiThread {
                    notifyFolderOpened(trimmed, newRootDocId, newTreeUri.toString(), isStartup = false)
                }
            }
            return response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming folder", e)
            response.put("status", "error")
            response.put("message", "${e.javaClass.simpleName}: ${e.message}")
            return response.toString()
        }
    }

    fun deleteFolderFromDisk(treeUriString: String?): String {
        val response = JSONObject()
        val targetTreeUri = if (!treeUriString.isNullOrEmpty()) {
            try { Uri.parse(treeUriString) } catch (e: Exception) { mCurrentTreeUri }
        } else {
            mCurrentTreeUri
        } ?: run {
            response.put("status", "error")
            response.put("message", "No folder specified or opened")
            return response.toString()
        }

        try {
            val rootDocId = getRootDocumentId(targetTreeUri)
            if (rootDocId.isEmpty()) {
                response.put("status", "error")
                response.put("message", "Cannot find root folder ID")
                return response.toString()
            }

            val docUri = DocumentsContract.buildDocumentUriUsingTree(targetTreeUri, rootDocId)
            val deleted = DocumentsContract.deleteDocument(contentResolver, docUri)
            if (deleted) {
                val isCurrent = (mCurrentTreeUri != null && (mCurrentTreeUri == targetTreeUri || mCurrentTreeUri.toString() == targetTreeUri.toString()))
                if (isCurrent) {
                    runOnUiThread {
                        closeCurrentFolder()
                    }
                }
                response.put("status", "ok")
            } else {
                response.put("status", "error")
                response.put("message", "Storage provider did not allow deleting this folder")
            }
            return response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting folder from disk", e)
            response.put("status", "error")
            response.put("message", "${e.javaClass.simpleName}: ${e.message}")
            return response.toString()
        }
    }

    fun openDefaultSaveFolderChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_DEFAULT_SAVE_FOLDER)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch default save folder chooser", e)
        }
    }

    fun getDefaultBasicSaveFolderUri(): String? = mPrefs.getString("default_basic_save_tree_uri", null)
    fun getDefaultBasicSaveFolderName(): String? = mPrefs.getString("default_basic_save_folder_name", null)

    fun clearDefaultBasicSaveFolder() {
        mPrefs.edit().remove("default_basic_save_tree_uri").remove("default_basic_save_folder_name").apply()
        mWebView?.post {
            mWebView?.evaluateJavascript("if(window.onDefaultBasicSaveFolderCleared){ window.onDefaultBasicSaveFolderCleared(); }", null)
        }
    }

    fun setDefaultBasicSaveFolder(treeUriString: String, folderName: String) {
        mPrefs.edit().putString("default_basic_save_tree_uri", treeUriString)
            .putString("default_basic_save_folder_name", folderName).apply()
        mWebView?.post {
            val js = "if(window.onDefaultBasicSaveFolderSet){ window.onDefaultBasicSaveFolderSet(" +
                    "${JSONObject.quote(folderName)}, ${JSONObject.quote(treeUriString)}); }"
            mWebView?.evaluateJavascript(js, null)
        }
    }

    private fun getRootDocumentId(treeUri: Uri): String {
        return try {
            DocumentsContract.getTreeDocumentId(treeUri)
        } catch (e: Exception) {
            try {
                DocumentsContract.getDocumentId(treeUri)
            } catch (ignored: Exception) {
                ""
            }
        }
    }

    // --- Recursive Tree Scanner ---
    fun getRecursiveTreeJson(treeUriString: String? = null): String {
        val response = JSONObject()
        val treeUri = if (!treeUriString.isNullOrEmpty()) {
            try { Uri.parse(treeUriString) } catch (e: Exception) { mCurrentTreeUri }
        } else {
            mCurrentTreeUri
        } ?: run {
            response.put("status", "error")
            response.put("message", "No folder is currently opened")
            return response.toString()
        }

        return try {
            val rootDocId = getRootDocumentId(treeUri)
            if (rootDocId.isEmpty()) {
                response.put("status", "error")
                response.put("message", "Cannot determine root folder ID")
                return response.toString()
            }

            val folderName = getFolderName(treeUri)
            val counters = intArrayOf(0, 0) // [0] files, [1] dirs
            val visited = HashSet<String>()

            val rootNode = buildRecursiveDirNode(treeUri, rootDocId, folderName, 0, counters, visited, 3, 200)

            response.put("status", "ok")
            response.put("root", rootNode)
            response.put("totalFiles", counters[0])
            response.put("totalDirs", counters[1])
            response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error building recursive tree", e)
            try {
                response.put("status", "error")
                response.put("message", "${e.javaClass.simpleName}: ${e.message}")
            } catch (ignored: Exception) {}
            response.toString()
        }
    }

    private fun buildRecursiveDirNode(
        treeUri: Uri,
        dirDocId: String,
        dirName: String,
        depth: Int,
        counters: IntArray,
        visited: HashSet<String>,
        maxDepth: Int,
        maxItems: Int
    ): JSONObject {
        val dirObj = JSONObject().apply {
            put("id", dirDocId)
            put("name", dirName)
            put("kind", "directory")
        }

        val childrenArr = JSONArray()
        dirObj.put("children", childrenArr)

        if (visited.contains(dirDocId) || depth > maxDepth || (counters[0] + counters[1]) >= maxItems) {
            dirObj.put("fileCount", 0)
            return dirObj
        }
        visited.add(dirDocId)

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, dirDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE
        )

        val subDirs = ArrayList<Pair<String, String>>()
        val files = ArrayList<JSONObject>()
        var directFileCount = 0

        try {
            contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)

                while (cursor.moveToNext() && (counters[0] + counters[1]) < maxItems) {
                    val id = if (idCol >= 0) cursor.getString(idCol) else null
                    val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L

                    if (name == null || name.startsWith(".")) continue
                    val nameLower = name.lowercase()
                    if (nameLower == "node_modules" || nameLower == ".git" ||
                        nameLower == ".obsidian" || nameLower == ".idea" ||
                        nameLower == ".vscode" || nameLower == ".trash" ||
                        nameLower == "__pycache__" || nameLower == ".gradle" ||
                        nameLower == "target" || nameLower == "dist"
                    ) {
                        continue
                    }

                    val isDir = DocumentsContract.Document.MIME_TYPE_DIR == mime
                    if (isDir && id != null) {
                        subDirs.add(Pair(id, name))
                    } else if (id != null) {
                        if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown") ||
                            nameLower.endsWith(".txt") || nameLower.endsWith(".mdown") ||
                            nameLower.endsWith(".mkd") || nameLower.endsWith(".text") ||
                            "text/markdown" == mime || "text/x-markdown" == mime ||
                            "text/plain" == mime
                        ) {
                            val f = JSONObject().apply {
                                put("id", id)
                                put("name", name)
                                put("kind", "file")
                                put("size", size)
                            }
                            files.add(f)
                            counters[0]++
                            directFileCount++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot read children for dir: $dirDocId", e)
        }

        subDirs.sortWith { a, b -> a.second.compareTo(b.second, ignoreCase = true) }
        files.sortWith { a, b -> a.optString("name").compareTo(b.optString("name"), ignoreCase = true) }

        var totalFilesUnder = directFileCount

        for (subDir in subDirs) {
            counters[1]++
            val childDirObj = buildRecursiveDirNode(
                treeUri, subDir.first, subDir.second, depth + 1, counters, visited, maxDepth, maxItems
            )
            totalFilesUnder += childDirObj.optInt("fileCount", 0)
            childrenArr.put(childDirObj)
        }

        for (file in files) {
            childrenArr.put(file)
        }

        dirObj.put("fileCount", totalFilesUnder)
        return dirObj
    }

    fun getTreeChildrenJson(parentDocId: String): String {
        val response = JSONObject()
        val treeUri = mCurrentTreeUri ?: run {
            response.put("status", "error")
            response.put("message", "No folder is currently opened")
            return response.toString()
        }

        try {
            var targetId = parentDocId
            if (targetId.isEmpty()) {
                targetId = getRootDocumentId(treeUri)
            }
            if (targetId.isEmpty()) {
                response.put("status", "error")
                response.put("message", "Cannot determine root folder ID")
                return response.toString()
            }

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, targetId)
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )

            val entries = JSONArray()
            val dirs = ArrayList<JSONObject>()
            val files = ArrayList<JSONObject>()

            contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getString(idCol) else null
                    val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null

                    if (name == null || name.startsWith(".")) continue
                    val nameLower = name.lowercase()
                    if (nameLower == "node_modules" || nameLower == ".git" ||
                        nameLower == ".obsidian" || nameLower == ".idea" ||
                        nameLower == ".vscode"
                    ) {
                        continue
                    }

                    val isDir = DocumentsContract.Document.MIME_TYPE_DIR == mime
                    if (isDir) {
                        dirs.add(JSONObject().apply {
                            put("id", id)
                            put("name", name)
                            put("kind", "directory")
                        })
                    } else {
                        if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown") ||
                            nameLower.endsWith(".txt") || "text/markdown" == mime || "text/plain" == mime
                        ) {
                            files.add(JSONObject().apply {
                                put("id", id)
                                put("name", name)
                                put("kind", "file")
                            })
                        }
                    }
                }
            }

            val cmp = Comparator<JSONObject> { a, b ->
                a.optString("name").compareTo(b.optString("name"), ignoreCase = true)
            }
            dirs.sortWith(cmp)
            files.sortWith(cmp)

            for (d in dirs) entries.put(d)
            for (f in files) entries.put(f)

            response.put("status", "ok")
            response.put("data", entries)
            return response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error querying tree children", e)
            try {
                response.put("status", "error")
                response.put("message", "${e.javaClass.simpleName}: ${e.message}")
            } catch (ignored: Exception) {}
            return response.toString()
        }
    }

    fun readTreeFileContent(docId: String): String {
        val treeUri = mCurrentTreeUri ?: return "ERROR: No folder opened"
        return try {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            contentResolver.openInputStream(docUri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                    val sb = StringBuilder()
                    val buf = CharArray(8192)
                    var n: Int
                    while (reader.read(buf).also { n = it } > 0) {
                        sb.append(buf, 0, n)
                    }
                    sb.toString()
                }
            } ?: "ERROR: Cannot open stream"
        } catch (e: Exception) {
            Log.e(TAG, "Error reading tree file docId=$docId", e)
            "ERROR: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    fun readNativeFileContent(uriStr: String): String {
        return try {
            val uri = Uri.parse(uriStr)
            contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                    val sb = StringBuilder()
                    val buf = CharArray(8192)
                    var n: Int
                    while (reader.read(buf).also { n = it } > 0) {
                        sb.append(buf, 0, n)
                    }
                    sb.toString()
                }
            } ?: "ERROR: Cannot open stream"
        } catch (e: Exception) {
            Log.e(TAG, "Error reading native file: $uriStr", e)
            "ERROR: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    fun writeNativeFileContent(uriStr: String, content: String): String {
        return try {
            val uri = Uri.parse(uriStr)
            contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(content.toByteArray(StandardCharsets.UTF_8))
                stream.flush()
            } ?: return "ERROR: Cannot open output stream"
            "OK"
        } catch (e: Exception) {
            Log.e(TAG, "Error writing native file: $uriStr", e)
            "ERROR: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    fun writeTreeFileContent(docId: String, content: String): String {
        return writeFileInTreeUri(null, docId, content)
    }

    fun writeFileInTreeUri(treeUriString: String?, docId: String, content: String): String {
        val treeUri = if (!treeUriString.isNullOrEmpty()) {
            try { Uri.parse(treeUriString) } catch (e: Exception) { mCurrentTreeUri }
        } else {
            mCurrentTreeUri
        } ?: return "ERROR: No folder opened"

        return try {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            contentResolver.openOutputStream(docUri, "wt")?.use { stream ->
                stream.write(content.toByteArray(StandardCharsets.UTF_8))
                stream.flush()
            } ?: return "ERROR: Cannot open output stream"
            "OK"
        } catch (e: Exception) {
            Log.e(TAG, "Error writing tree file docId=$docId", e)
            "ERROR: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    fun createTreeFileContent(fileName: String, content: String): String {
        return createFileInTreeFolder(null, null, fileName, content)
    }

    fun createFileInTreeUri(treeUriString: String?, fileName: String, content: String): String {
        return createFileInTreeFolder(treeUriString, null, fileName, content)
    }

    fun createFileInTreeFolder(treeUriString: String?, parentDocId: String?, fileName: String, content: String): String {
        val treeUri = if (!treeUriString.isNullOrEmpty()) {
            try { Uri.parse(treeUriString) } catch (e: Exception) { mCurrentTreeUri }
        } else {
            mCurrentTreeUri
        } ?: return "ERROR: No folder opened"

        return try {
            val targetDocId = if (!parentDocId.isNullOrEmpty()) parentDocId else getRootDocumentId(treeUri)
            val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, targetDocId)
            val newDocUri = DocumentsContract.createDocument(contentResolver, parentUri, "text/markdown", fileName)
                ?: return "ERROR: Could not create document"
            contentResolver.openOutputStream(newDocUri, "wt")?.use { stream ->
                stream.write(content.toByteArray(StandardCharsets.UTF_8))
                stream.flush()
            }
            val newDocId = DocumentsContract.getDocumentId(newDocUri)
            val finalName = getFileName(newDocUri)
            JSONObject().apply {
                put("status", "ok")
                put("docId", newDocId)
                put("uri", newDocUri.toString())
                put("name", if (finalName.isNotEmpty()) finalName else fileName)
            }.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating tree file: $fileName", e)
            "ERROR: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    // --- Checkpoints in Vault (.checkpoints folder) ---
    private fun getOrCreateCheckpointsFolder(treeUri: Uri): String? {
        val rootDocId = getRootDocumentId(treeUri)
        if (rootDocId.isEmpty()) return null

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

        try {
            contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getString(idCol) else null
                    val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                    if (name == ".checkpoints" && mime == DocumentsContract.Document.MIME_TYPE_DIR && id != null) {
                        return id
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking for .checkpoints folder: ${e.message}")
        }

        return try {
            val rootUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootDocId)
            val newDirUri = DocumentsContract.createDocument(
                contentResolver,
                rootUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                ".checkpoints"
            ) ?: return null
            DocumentsContract.getDocumentId(newDirUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating .checkpoints folder", e)
            null
        }
    }

    fun saveCheckpointToVault(docName: String, checkpointId: String, content: String): String {
        val response = JSONObject()
        val treeUri = mCurrentTreeUri ?: run {
            response.put("status", "no_vault")
            response.put("message", "No vault folder currently open")
            return response.toString()
        }

        return try {
            val dirDocId = getOrCreateCheckpointsFolder(treeUri) ?: run {
                response.put("status", "error")
                response.put("message", "Could not access or create .checkpoints folder in vault")
                return response.toString()
            }

            val safeDocName = docName.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim()
            val fileName = "${safeDocName}_${checkpointId}.md"

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, dirDocId)
            var existingDocUri: Uri? = null
            contentResolver.query(
                childrenUri,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getString(idCol) else null
                    val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                    if (name == fileName && id != null) {
                        existingDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id)
                        break
                    }
                }
            }

            val targetDocUri = existingDocUri ?: run {
                val dirUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, dirDocId)
                DocumentsContract.createDocument(contentResolver, dirUri, "text/markdown", fileName)
                    ?: run {
                        response.put("status", "error")
                        response.put("message", "Could not create checkpoint document in .checkpoints")
                        return response.toString()
                    }
            }

            contentResolver.openOutputStream(targetDocUri, "wt")?.use { stream ->
                stream.write(content.toByteArray(StandardCharsets.UTF_8))
                stream.flush()
            }

            val finalDocId = DocumentsContract.getDocumentId(targetDocUri)
            response.put("status", "ok")
            response.put("docId", finalDocId)
            response.put("fileName", fileName)
            response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving checkpoint to vault", e)
            response.put("status", "error")
            response.put("message", "${e.javaClass.simpleName}: ${e.message}")
            response.toString()
        }
    }

    fun getCheckpointsFromVault(docName: String?): String {
        val response = JSONObject()
        val treeUri = mCurrentTreeUri ?: run {
            response.put("status", "no_vault")
            response.put("checkpoints", JSONArray())
            return response.toString()
        }

        return try {
            val rootDocId = getRootDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocId)
            var dirDocId: String? = null
            contentResolver.query(
                childrenUri,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getString(idCol) else null
                    val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                    if (name == ".checkpoints" && mime == DocumentsContract.Document.MIME_TYPE_DIR && id != null) {
                        dirDocId = id
                        break
                    }
                }
            }

            if (dirDocId == null) {
                response.put("status", "ok")
                response.put("checkpoints", JSONArray())
                return response.toString()
            }

            val safeDocName = if (!docName.isNullOrEmpty()) docName.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim() else ""
            val cpChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, dirDocId)
            val checkpointsArr = JSONArray()

            contentResolver.query(
                cpChildrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                ),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val modCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getString(idCol) else null
                    val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val lastMod = if (modCol >= 0) cursor.getLong(modCol) else 0L

                    if (id != null && name != null) {
                        if (safeDocName.isEmpty() || name.startsWith(safeDocName)) {
                            try {
                                val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id)
                                val text = contentResolver.openInputStream(fileUri)?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
                                val itemObj = JSONObject().apply {
                                    put("docId", id)
                                    put("fileName", name)
                                    put("lastModified", lastMod)
                                    put("content", text)
                                }
                                checkpointsArr.put(itemObj)
                            } catch (readEx: Exception) {
                                Log.w(TAG, "Error reading checkpoint file $name: ${readEx.message}")
                            }
                        }
                    }
                }
            }

            response.put("status", "ok")
            response.put("checkpoints", checkpointsArr)
            response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting checkpoints from vault", e)
            response.put("status", "error")
            response.put("message", "${e.javaClass.simpleName}: ${e.message}")
            response.put("checkpoints", JSONArray())
            response.toString()
        }
    }

    fun deleteCheckpointFromVault(checkpointDocId: String): String {
        val response = JSONObject()
        val treeUri = mCurrentTreeUri ?: run {
            response.put("status", "no_vault")
            return response.toString()
        }

        return try {
            var deleted = false
            try {
                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, checkpointDocId)
                deleted = DocumentsContract.deleteDocument(contentResolver, docUri)
            } catch (ignored: Exception) {}

            if (!deleted) {
                // Fallback: search .checkpoints folder for matching filename or checkpoint ID
                val rootDocId = getRootDocumentId(treeUri)
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocId)
                var dirDocId: String? = null
                contentResolver.query(
                    childrenUri,
                    arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE),
                    null, null, null
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    while (cursor.moveToNext()) {
                        val id = if (idCol >= 0) cursor.getString(idCol) else null
                        val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                        val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                        if (name == ".checkpoints" && mime == DocumentsContract.Document.MIME_TYPE_DIR && id != null) {
                            dirDocId = id
                            break
                        }
                    }
                }

                if (dirDocId != null) {
                    val cpChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, dirDocId)
                    contentResolver.query(
                        cpChildrenUri,
                        arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                        null, null, null
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                        val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                        while (cursor.moveToNext()) {
                            val id = if (idCol >= 0) cursor.getString(idCol) else null
                            val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                            if (id != null && (id == checkpointDocId || (name != null && (name == checkpointDocId || name.contains(checkpointDocId))))) {
                                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id)
                                deleted = DocumentsContract.deleteDocument(contentResolver, docUri)
                                if (deleted) break
                            }
                        }
                    }
                }
            }

            response.put("status", if (deleted) "ok" else "error")
            response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting checkpoint $checkpointDocId", e)
            response.put("status", "error")
            response.put("message", "${e.javaClass.simpleName}: ${e.message}")
            response.toString()
        }
    }

    fun setSystemUiFullscreen(fullscreen: Boolean) {
        runOnUiThread {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val controller = window.insetsController
                    if (controller != null) {
                        if (fullscreen) {
                            controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_DEFAULT
                            controller.hide(android.view.WindowInsets.Type.systemBars())
                        } else {
                            controller.show(android.view.WindowInsets.Type.systemBars())
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    if (fullscreen) {
                        window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        )
                    } else {
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error setting system UI fullscreen: ${e.message}")
            }
        }
    }

    fun searchFolderJson(query: String): String {
        val results = JSONArray()
        val treeUri = mCurrentTreeUri ?: return results.toString()
        if (query.trim().isEmpty()) return results.toString()

        val qLower = query.lowercase()
        try {
            val rootId = getRootDocumentId(treeUri)
            if (rootId.isEmpty()) return results.toString()

            val dirQueue: Queue<String> = LinkedList()
            val visitedDirs = HashSet<String>()
            dirQueue.add(rootId)
            visitedDirs.add(rootId)

            var scannedFiles = 0
            while (dirQueue.isNotEmpty() && results.length() < 100 && scannedFiles < 300) {
                val currentDirId = dirQueue.poll() ?: break
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, currentDirId)
                val projection = arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                )

                contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

                    while (cursor.moveToNext() && results.length() < 100) {
                        val id = if (idCol >= 0) cursor.getString(idCol) else null
                        val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                        val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null

                        if (name == null || name.startsWith(".")) continue
                        val isDir = DocumentsContract.Document.MIME_TYPE_DIR == mime
                        if (isDir) {
                            val nameLower = name.lowercase()
                            if (nameLower != "node_modules" && nameLower != ".git" &&
                                nameLower != ".obsidian" && nameLower != ".idea" &&
                                nameLower != ".vscode" && nameLower != ".trash" &&
                                nameLower != "__pycache__" && nameLower != ".gradle"
                            ) {
                                if (id != null && !visitedDirs.contains(id)) {
                                    visitedDirs.add(id)
                                    dirQueue.add(id)
                                }
                            }
                        } else if (id != null) {
                            val nameLower = name.lowercase()
                            if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown") ||
                                nameLower.endsWith(".txt") || nameLower.endsWith(".mdown") ||
                                nameLower.endsWith(".mkd") || nameLower.endsWith(".text") ||
                                "text/markdown" == mime || "text/x-markdown" == mime ||
                                "text/plain" == mime
                            ) {
                                scannedFiles++
                                searchInFile(treeUri, id, name, qLower, results)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching folder", e)
        }
        return results.toString()
    }

    private fun searchInFile(treeUri: Uri, docId: String, fileName: String, qLower: String, results: JSONArray) {
        try {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            contentResolver.openInputStream(docUri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                    var line: String?
                    var lineNo = 0
                    while (reader.readLine().also { line = it } != null && results.length() < 100) {
                        val l = line ?: ""
                        if (l.lowercase().contains(qLower)) {
                            val r = JSONObject().apply {
                                put("docId", docId)
                                put("file", fileName)
                                put("line", lineNo)
                                put("text", if (l.trim().length > 180) l.trim().substring(0, 180) else l.trim())
                            }
                            results.put(r)
                        }
                        lineNo++
                    }
                }
            }
        } catch (ignored: Exception) {}
    }

    // --- Google Play Compliant Update & Link Support ---
    fun isAppSignedWithDebugKey(): Boolean {
        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners else signingInfo.signingCertificateHistory
                } else null
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            }
            if (signatures != null && signatures.isNotEmpty()) {
                val certBytes = signatures[0].toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(certBytes)
                val hexString = digest.joinToString("") { "%02X".format(it) }
                // Official Release Key SHA-256:
                // A8476B84AF080938F0257E161887751A7680027B55CF4AFA0CEABB9FC644FD07
                val isOfficialRelease = hexString.equals("A8476B84AF080938F0257E161887751A7680027B55CF4AFA0CEABB9FC644FD07", ignoreCase = true)
                !isOfficialRelease
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun downloadUpdateOnly(apkUrl: String, versionName: String) {
        downloadViaDownloadManager(apkUrl, versionName)
    }

    fun downloadAndInstallApk(apkUrl: String, versionName: String) {
        downloadViaDownloadManager(apkUrl, versionName)
    }

    private fun downloadViaDownloadManager(apkUrl: String, versionName: String) {
        runOnUiThread {
            try {
                if (apkUrl.isBlank()) {
                    Toast.makeText(this, "Invalid update URL", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                // SECURITY HARDENING: Prevent Arbitrary Code Execution (Dropper protection)
                // Strict validation to ONLY allow downloads from our official GitHub release repository.
                if (!apkUrl.startsWith("https://github.com/dkchw/mdviewer_android/releases/download/")) {
                    Log.e(TAG, "Blocked untrusted APK update source: \$apkUrl")
                    Toast.makeText(this, "Security Error: Blocked untrusted update source", Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }

                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                if (dm == null) {
                    Toast.makeText(this, "Android DownloadManager not available, opening browser...", Toast.LENGTH_SHORT).show()
                    openWebUrl(apkUrl)
                    return@runOnUiThread
                }

                val downloadUri = Uri.parse(apkUrl)
                val cleanVersion = versionName.replace("^v".toRegex(), "").trim()
                val fileName = if (apkUrl.contains("/")) {
                    val seg = apkUrl.substringAfterLast("/")
                    if (seg.endsWith(".apk")) seg else "mdviewer-v$cleanVersion.apk"
                } else {
                    "mdviewer-v$cleanVersion.apk"
                }

                val extDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (extDir != null) {
                    val existingFile = File(extDir, fileName)
                    if (existingFile.exists()) {
                        existingFile.delete()
                    }
                }

                val request = DownloadManager.Request(downloadUri).apply {
                    setTitle("MD Viewer v$cleanVersion")
                    setDescription("Downloading MD Viewer update package...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setMimeType("application/vnd.android.package-archive")
                    setDestinationInExternalFilesDir(this@MainActivity, Environment.DIRECTORY_DOWNLOADS, fileName)
                }

                val downloadId = dm.enqueue(request)
                activeDownloadId = downloadId
                Toast.makeText(this, "Downloading MD Viewer update via Android system...", Toast.LENGTH_SHORT).show()

                if (downloadReceiver != null) {
                    try { unregisterReceiver(downloadReceiver) } catch (ignored: Exception) {}
                    downloadReceiver = null
                }

                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                        if (id == activeDownloadId && id != -1L) {
                            try { unregisterReceiver(this) } catch (ignored: Exception) {}
                            if (downloadReceiver == this) downloadReceiver = null

                            val query = DownloadManager.Query().setFilterById(id)
                            val cursor = dm.query(query)
                            var success = false
                            if (cursor != null && cursor.moveToFirst()) {
                                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                                if (statusIndex >= 0) {
                                    val status = cursor.getInt(statusIndex)
                                    success = (status == DownloadManager.STATUS_SUCCESSFUL)
                                }
                                cursor.close()
                            }

                            if (success) {
                                val targetFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                                if (targetFile.exists() && targetFile.length() > 50000L) {
                                    lastDownloadedApkFile = targetFile
                                    runOnUiThread {
                                        Toast.makeText(this@MainActivity, "Download complete! Prompting installer...", Toast.LENGTH_SHORT).show()
                                        mWebView?.evaluateJavascript(
                                            "if (typeof window.onUpdateDownloadComplete === 'function') { window.onUpdateDownloadComplete('$cleanVersion'); }",
                                            null
                                        )
                                        promptInstallApk(targetFile)
                                    }
                                } else {
                                    Log.w(TAG, "Downloaded update file not found: ${targetFile.absolutePath}")
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, "Update download failed or was cancelled.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                downloadReceiver = receiver
                val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                if (Build.VERSION.SDK_INT >= 33) {
                    registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    registerReceiver(receiver, filter)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start DownloadManager", e)
                Toast.makeText(this, "Could not start download: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun isUpdateDownloaded(versionName: String? = null): Boolean {
        val extDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return false
        val currentVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: ""
        } catch (e: Exception) { "" }

        if (!versionName.isNullOrBlank()) {
            val cleanVersion = versionName.replace("^v".toRegex(), "").trim()
            // Don't report "ready" if the requested version matches the already-installed version
            if (cleanVersion == currentVersion) return false
            val files = extDir.listFiles()
            if (files != null) {
                for (f in files) {
                    if (f.name.startsWith("mdviewer") && f.name.contains(cleanVersion) && f.name.endsWith(".apk") && f.length() > 50000L) {
                        return true
                    }
                }
            }
            return false
        }
        // No version specified: check if lastDownloadedApkFile is valid and not for the current version
        if (lastDownloadedApkFile != null && lastDownloadedApkFile!!.exists() && lastDownloadedApkFile!!.length() > 50000L) {
            if (currentVersion.isNotEmpty() && lastDownloadedApkFile!!.name.contains(currentVersion)) return false
            return true
        }
        return false
    }

    fun installDownloadedApk(versionName: String? = null) {
        runOnUiThread {
            val extDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            var file: File? = null
            if (!versionName.isNullOrBlank() && extDir != null) {
                val cleanVersion = versionName.replace("^v".toRegex(), "").trim()
                val files = extDir.listFiles()
                if (files != null) {
                    file = files.firstOrNull { it.name.startsWith("mdviewer") && it.name.contains(cleanVersion) && it.name.endsWith(".apk") && it.length() > 50000L }
                }
            }
            if (file == null) {
                file = lastDownloadedApkFile
            }
            if (file == null && extDir != null) {
                val files = extDir.listFiles()
                if (files != null) {
                    file = files.filter { it.name.startsWith("mdviewer") && it.name.endsWith(".apk") && it.length() > 50000L }
                        .maxByOrNull { it.lastModified() }
                }
            }

            if (file != null && file.exists() && file.length() > 50000L) {
                promptInstallApk(file)
            } else {
                Toast.makeText(this, "Please download the update package first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun promptInstallApk(file: File) {
        runOnUiThread {
            if (!file.exists() || file.length() < 10000L) {
                Toast.makeText(this, "Update file is invalid or missing.", Toast.LENGTH_SHORT).show()
                return@runOnUiThread
            }

            pendingInstallFile = file

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(this, "Please allow 'Install unknown apps' for MD Viewer to update directly", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivityForResult(intent, REQUEST_CODE_UNKNOWN_APP_SOURCES)
                    return@runOnUiThread
                }
            }

            executeInstall(file)
        }
    }

    private fun executeInstall(file: File) {
        runOnUiThread {
            try {
                file.setReadable(true, false)
                val contentUri = FileProvider.getUriForFile(this, "$packageName.provider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(contentUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                Toast.makeText(this, "Opening Android Package Installer...", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Cannot launch package installer", e)
                Toast.makeText(this, "Cannot prompt package installer: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cleanupStaleUpdateApks(currentVersion: String) {
        try {
            val extDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
            val files = extDir.listFiles() ?: return
            for (f in files) {
                if (f.name.startsWith("mdviewer") && f.name.endsWith(".apk")) {
                    // Delete APK files that don't match the current version
                    // (they are leftover from previous updates)
                    if (!f.name.contains(currentVersion)) {
                        try {
                            f.delete()
                            Log.d(TAG, "Cleaned up stale update APK: ${f.name}")
                        } catch (ignored: Exception) {}
                    }
                }
            }
            // Clear the in-memory reference too
            lastDownloadedApkFile = null
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning up stale APKs", e)
        }
    }

    fun openWebUrl(url: String) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open browser URL: $url", e)
            Toast.makeText(this, "Cannot open browser: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFolderName(treeUri: Uri): String {
        val cached = mPrefs.getString("last_folder_name", null)
        if (!cached.isNullOrEmpty()) return cached

        try {
            val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
            contentResolver.query(docUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0)
                    if (!name.isNullOrEmpty()) {
                        mPrefs.edit().putString("last_folder_name", name).apply()
                        return name
                    }
                }
            }
        } catch (ignored: Exception) {}

        val path = treeUri.lastPathSegment
        if (path != null) {
            val idx = path.lastIndexOf(':')
            if (idx >= 0 && idx < path.length - 1) return path.substring(idx + 1)
            return path
        }
        return "Workspace"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        handlePendingIntentData()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (Intent.ACTION_VIEW == action || Intent.ACTION_EDIT == action) {
            val data = intent.data
            // Safe intent handling: only accept content or file schemes
            if (data != null && (data.scheme == "content" || data.scheme == "file")) {
                mPendingIntentUri = data
            }
        } else if (Intent.ACTION_SEND == action && "text/plain" == intent.type) {
            mPendingSharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        }
    }

    private fun handlePendingIntentData() {
        mPendingIntentUri?.let { uri ->
            mPendingIntentUri = null
            val fileName = getFileName(uri)
            mWebView?.post {
                val js = "if(window.loadFromNativeUri){ window.loadFromNativeUri(" +
                        "${JSONObject.quote(uri.toString())}, ${JSONObject.quote(fileName)}); }"
                mWebView?.evaluateJavascript(js, null)
            }
        }

        mPendingSharedText?.let { text ->
            mPendingSharedText = null
            mWebView?.post {
                val js = "if(window.loadTextFromNative){ window.loadTextFromNative(" +
                        "${JSONObject.quote(text)}, 'Shared.md'); }"
                mWebView?.evaluateJavascript(js, null)
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if ("content" == uri.scheme) {
            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            result = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not resolve display name", e)
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result ?: "Document.md"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_FILE_CHOOSER) {
            var results: Array<Uri>? = null
            var singleUri: Uri? = null

            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.data != null) {
                    singleUri = data.data
                    results = arrayOf(singleUri!!)
                } else if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    val uriList = ArrayList<Uri>(count)
                    for (i in 0 until count) {
                        uriList.add(data.clipData!!.getItemAt(i).uri)
                    }
                    results = uriList.toTypedArray()
                    if (count > 0) singleUri = uriList[0]
                }
            }

            mFilePathCallback?.onReceiveValue(results)
            mFilePathCallback = null

            singleUri?.let { uri ->
                val takeFlags = (data?.flags ?: 0) and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val flagsToTake = if (takeFlags == 0) Intent.FLAG_GRANT_READ_URI_PERMISSION else takeFlags
                try {
                    contentResolver.takePersistableUriPermission(uri, flagsToTake)
                } catch (e: Exception) {
                    Log.w(TAG, "Cannot take persistable permission for single file: ${e.message}")
                }

                val fileName = getFileName(uri)
                mWebView?.post {
                    val js = "if(window.loadFromNativeUri){ window.loadFromNativeUri(" +
                            "${JSONObject.quote(uri.toString())}, ${JSONObject.quote(fileName)}); }"
                    mWebView?.evaluateJavascript(js, null)
                }
            }
            return
        } else if (requestCode == REQUEST_CODE_FOLDER_CHOOSER) {
            if (resultCode == Activity.RESULT_OK && data?.data != null) {
                val treeUri = data.data!!
                var takeFlags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                if (takeFlags == 0) {
                    takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                }
                try {
                    contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                } catch (e: Exception) {
                    Log.w(TAG, "Cannot take persistable permission: ${e.message}")
                }

                mCurrentTreeUri = treeUri
                val folderName = getFolderName(treeUri)
                val rootDocId = getRootDocumentId(treeUri)
                mPrefs.edit().putString("last_folder_tree_uri", treeUri.toString()).putString("last_folder_name", folderName).apply()

                mWebView?.post {
                    notifyFolderOpened(folderName, rootDocId, treeUri.toString(), isStartup = false)
                }
            }
            return
        } else if (requestCode == REQUEST_CODE_DEFAULT_SAVE_FOLDER) {
            if (resultCode == Activity.RESULT_OK && data?.data != null) {
                val treeUri = data.data!!
                var takeFlags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                if (takeFlags == 0) {
                    takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                }
                try {
                    contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                } catch (e: Exception) {
                    Log.w(TAG, "Cannot take persistable permission for default save folder: ${e.message}")
                }

                val folderName = getFolderName(treeUri)
                val uriStr = treeUri.toString()
                mPrefs.edit()
                    .putString("default_basic_save_tree_uri", uriStr)
                    .putString("default_basic_save_folder_name", folderName)
                    .apply()

                mWebView?.post {
                    val js = "if(window.onDefaultBasicSaveFolderSet){ window.onDefaultBasicSaveFolderSet(" +
                            "${JSONObject.quote(folderName)}, ${JSONObject.quote(uriStr)}); }"
                    mWebView?.evaluateJavascript(js, null)
                }
            }
            return
        } else if (requestCode == REQUEST_CODE_CREATE_FILE) {
            if (resultCode == Activity.RESULT_OK && data?.data != null) {
                val newUri = data.data!!
                val contentToWrite = mPendingCreateContent ?: ""
                mPendingCreateContent = null
                val takeFlags = (data.flags) and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val flagsToTake = if (takeFlags == 0) (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) else takeFlags
                try {
                    contentResolver.takePersistableUriPermission(newUri, flagsToTake)
                } catch (e: Exception) {
                    try {
                        contentResolver.takePersistableUriPermission(newUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (ignored: Exception) {}
                }

                try {
                    contentResolver.openOutputStream(newUri, "wt")?.use { stream ->
                        stream.write(contentToWrite.toByteArray(StandardCharsets.UTF_8))
                        stream.flush()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error writing new file", e)
                }

                val fileName = getFileName(newUri)
                mWebView?.post {
                    val js = "if(window.loadFromNativeUri){ window.loadFromNativeUri(" +
                            "${JSONObject.quote(newUri.toString())}, ${JSONObject.quote(fileName)}); }"
                    mWebView?.evaluateJavascript(js, null)
                }
            }
            return
        } else if (requestCode == REQUEST_CODE_UNKNOWN_APP_SOURCES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.canRequestPackageInstalls()) {
                val file = pendingInstallFile
                if (file != null && file.exists()) {
                    pendingInstallFile = null
                    executeInstall(file)
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        mWebView?.evaluateJavascript("window.handleBackPressed ? window.handleBackPressed() : false") { value ->
            if (value != "true") {
                moveTaskToBack(true)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        try {
            mWebView?.onPause()
            mWebView?.pauseTimers()
        } catch (e: Exception) {
            Log.w(TAG, "Error pausing WebView: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            mWebView?.onResume()
            mWebView?.resumeTimers()
        } catch (e: Exception) {
            Log.w(TAG, "Error resuming WebView: ${e.message}")
        }

        // Prompt install if user just enabled "Install unknown apps"
        val file = pendingInstallFile
        if (file != null && file.exists()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls()) {
                pendingInstallFile = null
                executeInstall(file)
            }
        }
    }

    override fun onDestroy() {
        if (downloadReceiver != null) {
            try {
                unregisterReceiver(downloadReceiver)
            } catch (ignored: Exception) {}
            downloadReceiver = null
        }
        mWebView?.destroy()
        mWebView = null
        super.onDestroy()
    }
}
