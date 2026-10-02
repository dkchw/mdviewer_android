import re

with open('android/app/src/main/java/com/mdviewer/app/MainActivity.kt', 'r') as f:
    content = f.read()

# Add a check in onResume to execute pending install if permission was granted
on_resume_patch = """
    override fun onResume() {
        super.onResume()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (packageManager.canRequestPackageInstalls()) {
                pendingInstallFile?.let { file ->
                    pendingInstallFile = null
                    executeInstall(file)
                }
            }
        }
    }
"""

if 'override fun onResume()' not in content:
    # insert before onPause
    content = content.replace("    override fun onPause()", on_resume_patch + "\n    override fun onPause()")
    with open('android/app/src/main/java/com/mdviewer/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Patched onResume")
else:
    print("onResume already exists")
