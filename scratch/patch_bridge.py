import re

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
"""

if 'deleteTreeDocument' not in content:
    content = re.sub(r'(fun deleteFolderFromDisk.*?return activity\.deleteFolderFromDisk\(treeUriString\)\n\s*\})', r'\1\n' + new_methods, content, flags=re.DOTALL)
    with open('android/app/src/main/java/com/mdviewer/app/AndroidBridge.kt', 'w') as f:
        f.write(content)
    print("Methods injected into AndroidBridge.kt")
else:
    print("Methods already exist")
