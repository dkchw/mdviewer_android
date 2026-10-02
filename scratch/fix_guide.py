with open('android/app/src/main/assets/guide.md', 'r') as f:
    content = f.read()

# Remove it from the top
top_notes = """
## 📁 26. What's New in v3.1.0 (File Management)

- **File Management**: Added the ability to natively Create, Move, and Delete files directly inside the file tree sidebar! Click the vertical three-dots (`⋮`) icon next to any file or folder to access the context menu.

---
"""
content = content.replace(top_notes, "---\n")

# Append to bottom before the end footer
content = content.replace("## 🔒 25.", "## 📁 26. What's New in v3.1.0 (File Management)\n\n- **File Management**: Added the ability to natively Create, Move, and Delete files directly inside the file tree sidebar! Click the vertical three-dots (`⋮`) icon next to any file or folder to access the context menu.\n\n---\n\n## 🔒 25.")

with open('android/app/src/main/assets/guide.md', 'w') as f:
    f.write(content)
print("Guide fixed")
