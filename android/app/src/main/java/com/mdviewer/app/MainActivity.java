package com.mdviewer.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String TAG = "MDViewer";
    private static final int REQUEST_CODE_FILE_CHOOSER = 1001;
    private static final int REQUEST_CODE_FOLDER_CHOOSER = 1002;

    private WebView mWebView;
    private ValueCallback<Uri[]> mFilePathCallback;
    private Uri mPendingIntentUri;
    private String mPendingSharedText;
    private Uri mCurrentTreeUri;
    private SharedPreferences mPrefs;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getSharedPreferences("mdviewer_prefs", Context.MODE_PRIVATE);
        String savedTree = mPrefs.getString("last_folder_tree_uri", null);
        if (savedTree != null) {
            try {
                mCurrentTreeUri = Uri.parse(savedTree);
            } catch (Exception e) {
                mCurrentTreeUri = null;
            }
        }

        getWindow().getDecorView().setBackgroundColor(0xFF1a1b26);

        FrameLayout layout = new FrameLayout(this);
        layout.setBackgroundColor(0xFF1a1b26);

        mWebView = new WebView(this);
        mWebView.setBackgroundColor(0xFF1a1b26);
        layout.addView(mWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(layout);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(false);

        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.addJavascriptInterface(new AndroidBridge(this, mWebView), "AndroidBridge");

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                              FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                String[] mimeTypes = {"text/plain", "text/markdown", "text/x-markdown", "application/octet-stream", "*/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                try {
                    startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER);
                } catch (Exception e) {
                    Log.e(TAG, "Cannot launch file picker", e);
                    mFilePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                handlePendingIntentData();

                if (mCurrentTreeUri != null) {
                    final String folderName = getFolderName(mCurrentTreeUri);
                    String rootDocId = null;
                    try {
                        rootDocId = DocumentsContract.getTreeDocumentId(mCurrentTreeUri);
                    } catch (Exception e) {
                        try {
                            rootDocId = DocumentsContract.getDocumentId(mCurrentTreeUri);
                        } catch (Exception ignored) {}
                    }
                    if (rootDocId == null) rootDocId = "";
                    final String finalRoot = rootDocId;
                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyFolderOpened(folderName, finalRoot);
                        }
                    });
                }
            }
        });

        handleIntent(getIntent());
        mWebView.loadUrl("file:///android_asset/index.html");
    }

    public void openFileChooser() {
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
            mFilePathCallback = null;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"text/plain", "text/markdown", "text/x-markdown", "application/octet-stream", "*/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        try {
            startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER);
        } catch (Exception e) {
            Log.e(TAG, "Cannot launch file chooser", e);
        }
    }

    public void openFolderChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_CODE_FOLDER_CHOOSER);
        } catch (Exception e) {
            Log.e(TAG, "Cannot launch folder chooser", e);
        }
    }

    public void closeCurrentFolder() {
        mCurrentTreeUri = null;
        mPrefs.edit().remove("last_folder_tree_uri").apply();
        mWebView.evaluateJavascript("if(window.onFolderClosed){ window.onFolderClosed(); }", null);
    }

    private void notifyFolderOpened(String folderName, String rootDocId) {
        String js = "if(window.onFolderOpened){ window.onFolderOpened(" +
                JSONObject.quote(folderName) + ", " +
                JSONObject.quote(rootDocId) + "); }";
        mWebView.evaluateJavascript(js, null);
    }

    public String getRecursiveTreeJson() {
        JSONObject response = new JSONObject();
        if (mCurrentTreeUri == null) {
            try {
                response.put("status", "error");
                response.put("message", "No folder is currently opened");
            } catch (Exception ignored) {}
            return response.toString();
        }

        try {
            String rootDocId = null;
            try {
                rootDocId = DocumentsContract.getTreeDocumentId(mCurrentTreeUri);
            } catch (Exception e) {
                try {
                    rootDocId = DocumentsContract.getDocumentId(mCurrentTreeUri);
                } catch (Exception ignored) {}
            }

            if (rootDocId == null) {
                response.put("status", "error");
                response.put("message", "Cannot determine root folder ID");
                return response.toString();
            }

            String folderName = getFolderName(mCurrentTreeUri);
            int[] counters = new int[]{0, 0}; // [0]: totalFiles, [1]: totalDirs
            Set<String> visited = new HashSet<>();

            JSONObject rootNode = buildRecursiveDirNode(rootDocId, folderName, 0, counters, visited, 25, 15000);

            response.put("status", "ok");
            response.put("root", rootNode);
            response.put("totalFiles", counters[0]);
            response.put("totalDirs", counters[1]);
            return response.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error building recursive tree", e);
            try {
                response.put("status", "error");
                response.put("message", e.getClass().getSimpleName() + ": " + e.getMessage());
            } catch (Exception ignored) {}
            return response.toString();
        }
    }

    private JSONObject buildRecursiveDirNode(String dirDocId, String dirName, int depth, int[] counters,
                                             Set<String> visited, int maxDepth, int maxItems) {
        JSONObject dirObj = new JSONObject();
        try {
            dirObj.put("id", dirDocId);
            dirObj.put("name", dirName);
            dirObj.put("kind", "directory");

            JSONArray childrenArr = new JSONArray();
            dirObj.put("children", childrenArr);

            if (dirDocId == null || visited.contains(dirDocId) || depth > maxDepth || (counters[0] + counters[1]) >= maxItems) {
                dirObj.put("fileCount", 0);
                return dirObj;
            }
            visited.add(dirDocId);

            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mCurrentTreeUri, dirDocId);
            String[] projection = new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_SIZE
            };

            List<String[]> subDirs = new ArrayList<>();
            List<JSONObject> files = new ArrayList<>();
            int directFileCount = 0;

            try (Cursor cursor = getContentResolver().query(childrenUri, projection, null, null, null)) {
                if (cursor != null) {
                    int idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
                    int nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                    int mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
                    int sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE);

                    while (cursor.moveToNext() && (counters[0] + counters[1]) < maxItems) {
                        String id = idCol >= 0 ? cursor.getString(idCol) : null;
                        String name = nameCol >= 0 ? cursor.getString(nameCol) : null;
                        String mime = mimeCol >= 0 ? cursor.getString(mimeCol) : null;
                        long size = sizeCol >= 0 ? cursor.getLong(sizeCol) : 0;

                        if (name == null || name.startsWith(".")) continue;
                        String nameLower = name.toLowerCase();
                        if (nameLower.equals("node_modules") || nameLower.equals(".git") ||
                                nameLower.equals(".obsidian") || nameLower.equals(".idea") ||
                                nameLower.equals(".vscode") || nameLower.equals(".trash") ||
                                nameLower.equals("__pycache__") || nameLower.equals(".gradle") ||
                                nameLower.equals("target") || nameLower.equals("dist")) {
                            continue;
                        }

                        boolean isDir = DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);
                        if (isDir) {
                            subDirs.add(new String[]{id, name});
                        } else {
                            if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown") ||
                                    nameLower.endsWith(".txt") || nameLower.endsWith(".mdown") ||
                                    nameLower.endsWith(".mkd") || nameLower.endsWith(".text") ||
                                    "text/markdown".equals(mime) || "text/x-markdown".equals(mime) ||
                                    "text/plain".equals(mime)) {
                                JSONObject f = new JSONObject();
                                f.put("id", id);
                                f.put("name", name);
                                f.put("kind", "file");
                                f.put("size", size);
                                files.add(f);
                                counters[0]++;
                                directFileCount++;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Cannot read children for dir: " + dirDocId, e);
            }

            // Sort subdirectories alphabetically
            Collections.sort(subDirs, new Comparator<String[]>() {
                @Override
                public int compare(String[] a, String[] b) {
                    return a[1].compareToIgnoreCase(b[1]);
                }
            });

            // Sort files alphabetically
            Collections.sort(files, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    return a.optString("name").compareToIgnoreCase(b.optString("name"));
                }
            });

            int totalFilesUnder = directFileCount;

            // Recursively process subdirectories
            for (String[] subDir : subDirs) {
                counters[1]++;
                JSONObject childDirObj = buildRecursiveDirNode(subDir[0], subDir[1], depth + 1, counters, visited, maxDepth, maxItems);
                totalFilesUnder += childDirObj.optInt("fileCount", 0);
                childrenArr.put(childDirObj);
            }

            // Add files
            for (JSONObject file : files) {
                childrenArr.put(file);
            }

            dirObj.put("fileCount", totalFilesUnder);

        } catch (Exception e) {
            Log.e(TAG, "Error in buildRecursiveDirNode", e);
        }

        return dirObj;
    }

    public String getTreeChildrenJson(String parentDocId) {
        JSONObject response = new JSONObject();
        if (mCurrentTreeUri == null) {
            try {
                response.put("status", "error");
                response.put("message", "No folder is currently opened");
            } catch (Exception ignored) {}
            return response.toString();
        }

        try {
            String targetId = parentDocId;
            if (targetId == null || targetId.isEmpty()) {
                try {
                    targetId = DocumentsContract.getTreeDocumentId(mCurrentTreeUri);
                } catch (Exception e) {
                    targetId = null;
                }
                if (targetId == null) {
                    try {
                        targetId = DocumentsContract.getDocumentId(mCurrentTreeUri);
                    } catch (Exception e) {
                        targetId = null;
                    }
                }
            }

            if (targetId == null) {
                response.put("status", "error");
                response.put("message", "Cannot determine root folder ID");
                return response.toString();
            }

            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mCurrentTreeUri, targetId);
            String[] projection = new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
            };

            JSONArray entries = new JSONArray();
            List<JSONObject> dirs = new ArrayList<>();
            List<JSONObject> files = new ArrayList<>();

            try (Cursor cursor = getContentResolver().query(childrenUri, projection, null, null, null)) {
                if (cursor != null) {
                    int idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
                    int nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                    int mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);

                    while (cursor.moveToNext()) {
                        String id = idCol >= 0 ? cursor.getString(idCol) : null;
                        String name = nameCol >= 0 ? cursor.getString(nameCol) : null;
                        String mime = mimeCol >= 0 ? cursor.getString(mimeCol) : null;

                        if (name == null || name.startsWith(".")) continue;
                        String nameLower = name.toLowerCase();
                        if (nameLower.equals("node_modules") || nameLower.equals(".git") ||
                                nameLower.equals(".obsidian") || nameLower.equals(".idea") ||
                                nameLower.equals(".vscode")) {
                            continue;
                        }

                        boolean isDir = DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);
                        if (isDir) {
                            JSONObject obj = new JSONObject();
                            obj.put("id", id);
                            obj.put("name", name);
                            obj.put("kind", "directory");
                            dirs.add(obj);
                        } else {
                            if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown") ||
                                    nameLower.endsWith(".txt") || "text/markdown".equals(mime) ||
                                    "text/plain".equals(mime)) {
                                JSONObject obj = new JSONObject();
                                obj.put("id", id);
                                obj.put("name", name);
                                obj.put("kind", "file");
                                files.add(obj);
                            }
                        }
                    }
                }
            }

            Comparator<JSONObject> cmp = new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    return a.optString("name").compareToIgnoreCase(b.optString("name"));
                }
            };
            Collections.sort(dirs, cmp);
            Collections.sort(files, cmp);

            for (JSONObject d : dirs) entries.put(d);
            for (JSONObject f : files) entries.put(f);

            response.put("status", "ok");
            response.put("data", entries);
            return response.toString();

        } catch (Exception e) {
            Log.e(TAG, "Error querying tree children", e);
            try {
                response.put("status", "error");
                response.put("message", e.getClass().getSimpleName() + ": " + e.getMessage());
            } catch (Exception ignored) {}
            return response.toString();
        }
    }

    public String readTreeFileContent(String docId) {
        if (mCurrentTreeUri == null) return "ERROR: No folder opened";
        try {
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(mCurrentTreeUri, docId);
            try (InputStream is = getContentResolver().openInputStream(docUri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[8192];
                int n;
                while ((n = reader.read(buf)) > 0) {
                    sb.append(buf, 0, n);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading tree file docId=" + docId, e);
            return "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    public String readNativeFileContent(String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            try (InputStream is = getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[8192];
                int n;
                while ((n = reader.read(buf)) > 0) {
                    sb.append(buf, 0, n);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading native file: " + uriStr, e);
            return "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    public String searchFolderJson(String query) {
        JSONArray results = new JSONArray();
        if (mCurrentTreeUri == null || query == null || query.trim().isEmpty()) {
            return results.toString();
        }

        String qLower = query.toLowerCase();
        try {
            String rootId = null;
            try {
                rootId = DocumentsContract.getTreeDocumentId(mCurrentTreeUri);
            } catch (Exception e) {
                try {
                    rootId = DocumentsContract.getDocumentId(mCurrentTreeUri);
                } catch (Exception ignored) {}
            }
            if (rootId == null) return results.toString();

            Queue<String> dirQueue = new LinkedList<>();
            Set<String> visitedDirs = new HashSet<>();
            dirQueue.add(rootId);
            visitedDirs.add(rootId);

            int scannedFiles = 0;
            while (!dirQueue.isEmpty() && results.length() < 100 && scannedFiles < 300) {
                String currentDirId = dirQueue.poll();
                Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mCurrentTreeUri, currentDirId);
                String[] projection = new String[]{
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE
                };

                try (Cursor c = getContentResolver().query(childrenUri, projection, null, null, null)) {
                    if (c != null) {
                        int idCol = c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
                        int nameCol = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                        int mimeCol = c.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);

                        while (c.moveToNext() && results.length() < 100) {
                            String id = idCol >= 0 ? c.getString(idCol) : null;
                            String name = nameCol >= 0 ? c.getString(nameCol) : null;
                            String mime = mimeCol >= 0 ? cursorToStringSafe(c, mimeCol) : null;

                            if (name == null || name.startsWith(".")) continue;
                            boolean isDir = DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);
                            if (isDir) {
                                String nameLower = name.toLowerCase();
                                if (!nameLower.equals("node_modules") && !nameLower.equals(".git") &&
                                        !nameLower.equals(".obsidian") && !nameLower.equals(".idea") &&
                                        !nameLower.equals(".vscode") && !nameLower.equals(".trash") &&
                                        !nameLower.equals("__pycache__") && !nameLower.equals(".gradle")) {
                                    if (id != null && !visitedDirs.contains(id)) {
                                        visitedDirs.add(id);
                                        dirQueue.add(id);
                                    }
                                }
                            } else {
                                String nameLower = name.toLowerCase();
                                if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown") ||
                                        nameLower.endsWith(".txt") || nameLower.endsWith(".mdown") ||
                                        nameLower.endsWith(".mkd") || nameLower.endsWith(".text") ||
                                        "text/markdown".equals(mime) || "text/x-markdown".equals(mime) ||
                                        "text/plain".equals(mime)) {
                                    scannedFiles++;
                                    searchInFile(id, name, qLower, results);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching folder", e);
        }

        return results.toString();
    }

    private String cursorToStringSafe(Cursor c, int col) {
        try {
            return c.getString(col);
        } catch (Exception e) {
            return null;
        }
    }

    private void searchInFile(String docId, String fileName, String qLower, JSONArray results) {
        try {
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(mCurrentTreeUri, docId);
            try (InputStream is = getContentResolver().openInputStream(docUri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                int lineNo = 0;
                while ((line = reader.readLine()) != null && results.length() < 100) {
                    if (line.toLowerCase().contains(qLower)) {
                        JSONObject r = new JSONObject();
                        r.put("docId", docId);
                        r.put("file", fileName);
                        r.put("line", lineNo);
                        r.put("text", line.trim().length() > 180 ? line.trim().substring(0, 180) : line.trim());
                        results.put(r);
                    }
                    lineNo++;
                }
            }
        } catch (Exception ignored) {}
    }

    // Auto-update support
    public String checkGitHubRelease() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // background check
            }
        }).start();

        try {
            URL url = new URL("https://api.github.com/repos/dkchw/mdviewer_android/releases/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "MDViewer-Android-App");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int code = conn.getResponseCode();
            if (code == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    return sb.toString();
                }
            } else {
                JSONObject obj = new JSONObject();
                obj.put("status", "error");
                obj.put("code", code);
                return obj.toString();
            }
        } catch (Exception e) {
            Log.w(TAG, "Cannot fetch GitHub release", e);
            JSONObject obj = new JSONObject();
            try {
                obj.put("status", "error");
                obj.put("message", e.getMessage());
            } catch (Exception ignored) {}
            return obj.toString();
        }
    }

    public void downloadAndInstallApk(final String apkUrl, final String versionName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(MainActivity.this, "Downloading MD Viewer update v" + versionName + "...", Toast.LENGTH_LONG).show();

                    DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    if (dm == null) {
                        openWebUrl(apkUrl);
                        return;
                    }

                    Uri downloadUri = Uri.parse(apkUrl);
                    DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                    request.setTitle("MD Viewer v" + versionName);
                    request.setDescription("Downloading latest APK...");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setMimeType("application/vnd.android.package-archive");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "mdviewer-v" + versionName + ".apk");

                    final long downloadId = dm.enqueue(request);

                    BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                            if (id == downloadId) {
                                try {
                                    unregisterReceiver(this);
                                } catch (Exception ignored) {}

                                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                Uri fileUri = manager != null ? manager.getUriForDownloadedFile(downloadId) : null;
                                if (fileUri != null) {
                                    installApk(fileUri);
                                } else {
                                    openWebUrl(apkUrl);
                                }
                            }
                        }
                    };

                    if (Build.VERSION.SDK_INT >= 33) {
                        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
                    } else {
                        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Download error", e);
                    openWebUrl(apkUrl);
                }
            }
        });
    }

    private void installApk(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Install error", e);
            Toast.makeText(this, "Cannot open installer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void openWebUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open browser: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFolderName(Uri treeUri) {
        try {
            String treeDocId = DocumentsContract.getTreeDocumentId(treeUri);
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId);
            try (Cursor c = getContentResolver().query(docUri, new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    String name = c.getString(0);
                    if (name != null && !name.isEmpty()) return name;
                }
            }
        } catch (Exception ignored) {}

        String path = treeUri.getLastPathSegment();
        if (path != null) {
            int idx = path.lastIndexOf(':');
            if (idx >= 0 && idx < path.length() - 1) return path.substring(idx + 1);
            return path;
        }
        return "Workspace";
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        handlePendingIntentData();
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) {
            mPendingIntentUri = intent.getData();
        } else if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(intent.getType())) {
            mPendingSharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
    }

    private void handlePendingIntentData() {
        if (mPendingIntentUri != null) {
            final Uri uri = mPendingIntentUri;
            mPendingIntentUri = null;
            final String fileName = getFileName(uri);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    String js = "if(window.loadFromNativeUri){ window.loadFromNativeUri(" +
                            JSONObject.quote(uri.toString()) + ", " +
                            JSONObject.quote(fileName) + "); }";
                    mWebView.evaluateJavascript(js, null);
                }
            });
        } else if (mPendingSharedText != null) {
            final String text = mPendingSharedText;
            mPendingSharedText = null;
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    String js = "if(window.loadTextFromNative){ window.loadTextFromNative(" +
                            JSONObject.quote(text) + ", 'Shared.md'); }";
                    mWebView.evaluateJavascript(js, null);
                }
            });
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not resolve display name", e);
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "Document.md";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FILE_CHOOSER) {
            Uri[] results = null;
            Uri singleUri = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.getData() != null) {
                    singleUri = data.getData();
                    results = new Uri[]{ singleUri };
                } else if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                    if (count > 0) singleUri = results[0];
                }
            }

            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            }

            if (singleUri != null) {
                final Uri uri = singleUri;
                final String fileName = getFileName(uri);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        String js = "if(window.loadFromNativeUri){ window.loadFromNativeUri(" +
                                JSONObject.quote(uri.toString()) + ", " +
                                JSONObject.quote(fileName) + "); }";
                        mWebView.evaluateJavascript(js, null);
                    }
                });
            }
            return;
        } else if (requestCode == REQUEST_CODE_FOLDER_CHOOSER) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri treeUri = data.getData();
                int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (takeFlags == 0) {
                    takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                }
                try {
                    getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                } catch (Exception e) {
                    Log.w(TAG, "Cannot take persistable permission: " + e.getMessage());
                }

                mCurrentTreeUri = treeUri;
                mPrefs.edit().putString("last_folder_tree_uri", treeUri.toString()).apply();
                final String folderName = getFolderName(treeUri);
                String rootDocId = null;
                try {
                    rootDocId = DocumentsContract.getTreeDocumentId(treeUri);
                } catch (Exception e) {
                    try {
                        rootDocId = DocumentsContract.getDocumentId(treeUri);
                    } catch (Exception ignored) {}
                }
                if (rootDocId == null) rootDocId = "";
                final String finalRootDocId = rootDocId;
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyFolderOpened(folderName, finalRootDocId);
                    }
                });
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mWebView.evaluateJavascript("window.handleBackPressed ? window.handleBackPressed() : false",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            if (!"true".equals(value)) {
                                moveTaskToBack(true);
                            }
                        }
                    });
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroy();
    }
}
