with open('android/app/src/main/java/com/mdviewer/app/AndroidBridge.kt', 'r') as f:
    content = f.read()

new_methods = """
    @JavascriptInterface
    fun deleteTreeDocument(docId: String?): String {
        return activity.deleteTreeDocument(docId ?: "")
    }

    @JavascriptInterface
    fun moveTreeDocument(sourceDocId: String?, sourceParentDocId: String?, targetParentDocId: String?): String {
        return activity.moveTreeDocument(sourceDocId ?: "", sourceParentDocId ?: "", targetParentDocId ?: "")
    }
}
"""

content = content.rstrip()
if content.endswith('}'):
    content = content[:-1] + new_methods

with open('android/app/src/main/java/com/mdviewer/app/AndroidBridge.kt', 'w') as f:
    f.write(content)
print("Injected into AndroidBridge safely")
