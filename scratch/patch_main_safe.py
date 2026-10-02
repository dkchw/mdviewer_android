with open('android/app/src/main/java/com/mdviewer/app/MainActivity.kt', 'r') as f:
    content = f.read()

new_methods = """
    fun deleteTreeDocument(docId: String): String {
        val response = org.json.JSONObject()
        val treeUri = mCurrentTreeUri ?: run {
            response.put("status", "error")
            response.put("message", "No vault opened")
            return response.toString()
        }
        try {
            val docUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            val success = android.provider.DocumentsContract.deleteDocument(contentResolver, docUri)
            if (success) {
                response.put("status", "ok")
            } else {
                response.put("status", "error")
                response.put("message", "Failed to delete document")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error deleting document: $docId", e)
            response.put("status", "error")
            response.put("message", e.message)
        }
        return response.toString()
    }

    fun moveTreeDocument(sourceDocId: String, sourceParentDocId: String, targetParentDocId: String): String {
        val response = org.json.JSONObject()
        val treeUri = mCurrentTreeUri ?: run {
            response.put("status", "error")
            response.put("message", "No vault opened")
            return response.toString()
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            response.put("status", "error")
            response.put("message", "Move operation requires Android 7.0+")
            return response.toString()
        }
        try {
            val sourceUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, sourceDocId)
            val sourceParentUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, sourceParentDocId)
            val targetParentUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, targetParentDocId)
            
            val movedUri = android.provider.DocumentsContract.moveDocument(contentResolver, sourceUri, sourceParentUri, targetParentUri)
            if (movedUri != null) {
                response.put("status", "ok")
                response.put("newDocId", android.provider.DocumentsContract.getDocumentId(movedUri))
            } else {
                response.put("status", "error")
                response.put("message", "Move operation failed")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error moving document: $sourceDocId", e)
            response.put("status", "error")
            response.put("message", e.message)
        }
        return response.toString()
    }
}
"""

content = content.rstrip()
if content.endswith('}'):
    content = content[:-1] + new_methods

with open('android/app/src/main/java/com/mdviewer/app/MainActivity.kt', 'w') as f:
    f.write(content)
print("Injected into MainActivity safely")
