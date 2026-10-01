package com.mdviewer.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import android.widget.Toast

/**
 * BroadcastReceiver to receive status updates from PackageInstaller sessions
 * (Android 13+ / 12+ PackageInstaller API).
 */
class InstallStatusReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MDViewerInstaller"
        const val ACTION_INSTALL_STATUS = "com.mdviewer.app.ACTION_INSTALL_STATUS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        Log.i(TAG, "PackageInstaller session status: $status, message: $message")

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // User confirmation required by system. Launch confirmation prompt.
                val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                if (confirmIntent != null) {
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(confirmIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Cannot launch PackageInstaller user action prompt", e)
                    }
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "MD Viewer updated successfully!", Toast.LENGTH_SHORT).show()
            }
            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                Log.i(TAG, "Installation cancelled by user")
            }
            PackageInstaller.STATUS_FAILURE_CONFLICT -> {
                Toast.makeText(context, "Update failed: signature conflict with installed build.", Toast.LENGTH_LONG).show()
            }
            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                Toast.makeText(context, "Update failed: insufficient device storage.", Toast.LENGTH_LONG).show()
            }
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> {
                Toast.makeText(context, "Update failed: incompatible package.", Toast.LENGTH_LONG).show()
            }
            PackageInstaller.STATUS_FAILURE_INVALID -> {
                Toast.makeText(context, "Update failed: invalid APK package.", Toast.LENGTH_LONG).show()
            }
            else -> {
                if (!message.isNullOrBlank()) {
                    Toast.makeText(context, "Install failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
