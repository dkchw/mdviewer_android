package com.mdviewer.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class AndroidBridge {
    private final MainActivity mActivity;
    private final WebView mWebView;
    private final Handler mHandler;
    private final SharedPreferences mPrefs;

    public AndroidBridge(MainActivity activity, WebView webView) {
        this.mActivity = activity;
        this.mWebView = webView;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mPrefs = activity.getSharedPreferences("mdviewer_prefs", Context.MODE_PRIVATE);
    }

    @JavascriptInterface
    public void openNativeFilePicker() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mActivity.openFileChooser();
            }
        });
    }

    @JavascriptInterface
    public void openNativeFolderPicker() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mActivity.openFolderChooser();
            }
        });
    }

    @JavascriptInterface
    public void closeFolder() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mActivity.closeCurrentFolder();
            }
        });
    }

    @JavascriptInterface
    public String getTreeChildren(String docId) {
        return mActivity.getTreeChildrenJson(docId);
    }

    @JavascriptInterface
    public String readTreeFile(String docId) {
        return mActivity.readTreeFileContent(docId);
    }

    @JavascriptInterface
    public String searchFolder(String query) {
        return mActivity.searchFolderJson(query);
    }

    @JavascriptInterface
    public String readNativeFile(String uriString) {
        return mActivity.readNativeFileContent(uriString);
    }

    @JavascriptInterface
    public void showToast(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void copyToClipboard(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Markdown Content", text);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mActivity, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @JavascriptInterface
    public void shareText(final String text, final String title) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, title != null ? title : "Share Markdown");
                mActivity.startActivity(shareIntent);
            }
        });
    }

    @JavascriptInterface
    public void savePreference(String key, String value) {
        mPrefs.edit().putString(key, value).apply();
    }

    @JavascriptInterface
    public String getPreference(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }

    @JavascriptInterface
    public void closeApp() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mActivity.finish();
            }
        });
    }
}
