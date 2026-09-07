package com.mdviewer.app

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException

class AppFileProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.mdviewer.app.fileprovider"

        fun getUriForFile(file: File): Uri {
            return Uri.parse("content://$AUTHORITY/${file.name}")
        }
    }

    override fun onCreate(): Boolean = true

    private fun getFileForUri(uri: Uri): File {
        val fileName = uri.lastPathSegment ?: throw FileNotFoundException("Missing file path")
        val ctx = context ?: throw FileNotFoundException("Null context")
        val file = File(ctx.cacheDir, fileName)
        if (file.exists()) return file
        val extFile = File(ctx.getExternalFilesDir(null), fileName)
        if (extFile.exists()) return extFile
        return file
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val file = getFileForUri(uri)
        val cols = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val cursor = MatrixCursor(cols)
        val row = cursor.newRow()
        for (col in cols) {
            when (col) {
                OpenableColumns.DISPLAY_NAME -> row.add(col, file.name)
                OpenableColumns.SIZE -> row.add(col, if (file.exists()) file.length() else 0L)
                else -> row.add(col, null)
            }
        }
        return cursor
    }

    override fun getType(uri: Uri): String {
        return "application/vnd.android.package-archive"
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val file = getFileForUri(uri)
        if (!file.exists()) throw FileNotFoundException("File not found: ${file.absolutePath}")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
