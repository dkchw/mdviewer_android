package com.mdviewer.app

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException

class ApkProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val ctx = context ?: throw FileNotFoundException("No context available")
        val fileName = uri.lastPathSegment ?: throw FileNotFoundException("No file name")
        val file = File(ctx.cacheDir, "updates/$fileName").takeIf { it.exists() }
            ?: File(ctx.cacheDir, fileName).takeIf { it.exists() }
            ?: throw FileNotFoundException("Update file not found: $fileName")

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String = "application/vnd.android.package-archive"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val ctx = context ?: return null
        val fileName = uri.lastPathSegment ?: "mdviewer-update.apk"
        val file = File(ctx.cacheDir, "updates/$fileName").takeIf { it.exists() }
            ?: File(ctx.cacheDir, fileName).takeIf { it.exists() }
            ?: return null

        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val matrixCursor = MatrixCursor(columns)
        val row = arrayOfNulls<Any>(columns.size)
        for (i in columns.indices) {
            when (columns[i]) {
                OpenableColumns.DISPLAY_NAME -> row[i] = file.name
                OpenableColumns.SIZE -> row[i] = file.length()
            }
        }
        matrixCursor.addRow(row)
        return matrixCursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
