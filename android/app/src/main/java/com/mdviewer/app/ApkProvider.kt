package com.mdviewer.app

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException

class ApkProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val ctx = context ?: throw FileNotFoundException("Context is null")
        val fileName = uri.lastPathSegment ?: throw FileNotFoundException("Invalid URI")

        // 1. Check external files downloads directory
        val extDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (extDir != null) {
            val file = File(extDir, fileName).canonicalFile
            val allowedDir = extDir.canonicalFile
            if (file.path.startsWith(allowedDir.path) && file.exists()) {
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }
        }

        // 2. Check internal cache directory
        val cacheDir = ctx.cacheDir
        if (cacheDir != null) {
            val file = File(cacheDir, fileName).canonicalFile
            val allowedDir = cacheDir.canonicalFile
            if (file.path.startsWith(allowedDir.path) && file.exists()) {
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }

            val updatesDir = File(cacheDir, "updates").canonicalFile
            val updateFile = File(updatesDir, fileName).canonicalFile
            if (updateFile.path.startsWith(updatesDir.path) && updateFile.exists()) {
                return ParcelFileDescriptor.open(updateFile, ParcelFileDescriptor.MODE_READ_ONLY)
            }
        }

        throw FileNotFoundException("Update package not found: $fileName")
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
        val fileName = uri.lastPathSegment ?: return null

        var targetFile: File? = null
        val extDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (extDir != null) {
            val f = File(extDir, fileName).canonicalFile
            if (f.path.startsWith(extDir.canonicalPath) && f.exists()) {
                targetFile = f
            }
        }

        if (targetFile == null) {
            val cacheDir = ctx.cacheDir
            if (cacheDir != null) {
                val f = File(cacheDir, fileName).canonicalFile
                if (f.path.startsWith(cacheDir.canonicalPath) && f.exists()) {
                    targetFile = f
                } else {
                    val uf = File(File(cacheDir, "updates"), fileName).canonicalFile
                    if (uf.path.startsWith(cacheDir.canonicalPath) && uf.exists()) {
                        targetFile = uf
                    }
                }
            }
        }

        if (targetFile == null) return null

        val cols = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val cursor = MatrixCursor(cols)
        val row = arrayOfNulls<Any>(cols.size)
        for (i in cols.indices) {
            when (cols[i]) {
                OpenableColumns.DISPLAY_NAME -> row[i] = targetFile.name
                OpenableColumns.SIZE -> row[i] = targetFile.length()
            }
        }
        cursor.addRow(row)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
