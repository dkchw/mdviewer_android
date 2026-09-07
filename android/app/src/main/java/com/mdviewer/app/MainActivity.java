package com.mdviewer.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
                } else {
                    Log.w(TAG, "Cursor is null for children of: " + targetId);
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
            String rootId = DocumentsContract.getTreeDocumentId(mCurrentTreeUri);
            Queue<String> dirQueue = new LinkedList<>();
            dirQueue.add(rootId);

            int scannedFiles = 0;
            while (!dirQueue.isEmpty() && results.length() < 100 && scannedFiles < 80) {
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
                            String mime = mimeCol >= 0 ? c.getString(mimeCol) : null;

                            if (name == null || name.startsWith(".")) continue;
                            boolean isDir = DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);
                            if (isDir) {
                                String nameLower = name.toLowerCase();
                                if (!nameLower.equals("node_modules") && !nameLower.equals(".git") &&
                                        !nameLower.equals(".obsidian") && !nameLower.equals(".idea")) {
                                    dirQueue.add(id);
                                }
                            } else {
                                String nameLower = name.toLowerCase();
                                if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown") || nameLower.endsWith(".txt")) {
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
