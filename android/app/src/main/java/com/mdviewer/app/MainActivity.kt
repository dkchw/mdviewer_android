package com.mdviewer.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
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
    }

    private var mWebView: WebView? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mPendingIntentUri: Uri? = null
    private var mPendingSharedText: String? = null
    private var mCurrentTreeUri: Uri? = null
    private lateinit var mPrefs: SharedPreferences

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
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
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
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                handlePendingIntentData()

                mCurrentTreeUri?.let { treeUri ->
                    val folderName = getFolderName(treeUri)
                    val rootDocId = getRootDocumentId(treeUri)
                    webView.post {
                        notifyFolderOpened(folderName, rootDocId)
                    }
                }
            }
        }

        handleIntent(intent)
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
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch file chooser", e)
        }
    }

    fun openFolderChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
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
        mPrefs.edit().remove("last_folder_tree_uri").apply()
        mWebView?.evaluateJavascript("if(window.onFolderClosed){ window.onFolderClosed(); }", null)
    }

    private fun notifyFolderOpened(folderName: String, rootDocId: String) {
        val js = "if(window.onFolderOpened){ window.onFolderOpened(" +
                "${JSONObject.quote(folderName)}, ${JSONObject.quote(rootDocId)}); }"
        mWebView?.evaluateJavascript(js, null)
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
    fun getRecursiveTreeJson(): String {
        val response = JSONObject()
        val treeUri = mCurrentTreeUri ?: run {
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

            val rootNode = buildRecursiveDirNode(treeUri, rootDocId, folderName, 0, counters, visited, 25, 15000)

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
    fun checkGitHubRelease(): String {
        return try {
            val url = URL("https://api.github.com/repos/dkchw/mdviewer_android/releases/latest")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "MDViewer-Android-App")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                connectTimeout = 8000
                readTimeout = 8000
            }

            val code = conn.responseCode
            if (code == 200) {
                BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { reader ->
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    sb.toString()
                }
            } else {
                JSONObject().apply {
                    put("status", "error")
                    put("code", code)
                }.toString()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot fetch GitHub release", e)
            JSONObject().apply {
                put("status", "error")
                put("message", e.message)
            }.toString()
        }
    }

    fun downloadAndInstallApk(apkUrl: String, versionName: String) {
        runOnUiThread {
            try {
                Toast.makeText(this, "Downloading MD Viewer update v$versionName...", Toast.LENGTH_SHORT).show()

                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                if (dm == null) {
                    Toast.makeText(this, "DownloadManager unavailable, opening browser...", Toast.LENGTH_SHORT).show()
                    openWebUrl(apkUrl)
                    return@runOnUiThread
                }

                val downloadUri = Uri.parse(apkUrl)
                val fileName = "mdviewer-v$versionName.apk"

                val request = DownloadManager.Request(downloadUri).apply {
                    setTitle("MD Viewer v$versionName")
                    setDescription("Downloading update package...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setMimeType("application/vnd.android.package-archive")
                    try {
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    } catch (e: Exception) {
                        Log.w(TAG, "Cannot set public downloads dir for update", e)
                    }
                }

                val downloadId = dm.enqueue(request)
                Toast.makeText(this, "Download started in background. Check status bar notifications.", Toast.LENGTH_LONG).show()

                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                        if (id == downloadId) {
                            try {
                                unregisterReceiver(this)
                            } catch (ignored: Exception) {}

                            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                            val fileUri = manager?.getUriForDownloadedFile(downloadId)
                            if (fileUri != null) {
                                installApk(fileUri)
                            } else {
                                Toast.makeText(this@MainActivity, "Update download finished! Tap notification to install.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                if (Build.VERSION.SDK_INT >= 33) {
                    registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    registerReceiver(receiver, filter)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Native download error", e)
                Toast.makeText(this, "Download error, opening in browser: ${e.message}", Toast.LENGTH_SHORT).show()
                openWebUrl(apkUrl)
            }
        }
    }

    private fun installApk(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Install launch error", e)
            Toast.makeText(this, "Update downloaded! Tap notification or open Downloads to install.", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, "Cannot open browser: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFolderName(treeUri: Uri): String {
        try {
            val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
            contentResolver.query(docUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0)
                    if (!name.isNullOrEmpty()) return name
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
                    takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                try {
                    contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                } catch (e: Exception) {
                    Log.w(TAG, "Cannot take persistable permission: ${e.message}")
                }

                mCurrentTreeUri = treeUri
                mPrefs.edit().putString("last_folder_tree_uri", treeUri.toString()).apply()

                val folderName = getFolderName(treeUri)
                val rootDocId = getRootDocumentId(treeUri)

                mWebView?.post {
                    notifyFolderOpened(folderName, rootDocId)
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mWebView?.evaluateJavascript("window.handleBackPressed ? window.handleBackPressed() : false") { value ->
                if (value != "true") {
                    moveTaskToBack(true)
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        mWebView?.destroy()
        mWebView = null
        super.onDestroy()
    }
}
