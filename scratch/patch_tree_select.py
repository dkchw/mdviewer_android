import re

with open('android/app/src/main/assets/index.html', 'r') as f:
    content = f.read()

# Replace the text input with a select dropdown
new_html = """
      <p style="font-size:14px; color:var(--muted);">Select destination folder:</p>
      <select id="selectMoveTargetDocId" style="width:100%; padding:10px; border-radius:6px; background:#181b2a; color:#fff; border:1px solid rgba(255,255,255,0.1); margin-bottom:16px;">
      </select>
"""
content = re.sub(
    r'<p style="font-size:14px; color:var\(--muted\);">Select destination folder ID[^\n]*\n\s*<input type="text" id="inputMoveTargetDocId"[^>]*>',
    new_html.strip(),
    content
)

# Update JS logic to populate select and read from select
js_populate = """
      const selectTarget = document.getElementById('selectMoveTargetDocId');
      if (selectTarget) {
        selectTarget.innerHTML = `<option value="${openedFolderRootDocId}">/ (Root)</option>`;
        
        function populateFolders(nodes, prefix) {
          if (!nodes) return;
          for (const n of nodes) {
            if (n.kind === 'directory') {
              // Prevent moving a folder into itself or its children
              if (itemToMove.kind === 'directory' && n.id === itemToMove.id) continue;
              
              const opt = document.createElement('option');
              opt.value = n.id;
              opt.textContent = prefix + n.name;
              selectTarget.appendChild(opt);
              populateFolders(n.children, prefix + n.name + '/');
            }
          }
        }
        if (typeof folderTreeItems !== 'undefined') {
          populateFolders(folderTreeItems, '/');
        }
      }
"""
content = content.replace("inputMoveTargetDocId.value = '';", js_populate)
content = content.replace("inputMoveTargetDocId.value.trim()", "document.getElementById('selectMoveTargetDocId').value")

with open('android/app/src/main/assets/index.html', 'w') as f:
    f.write(content)
print("Updated Move Modal to use dropdown")
