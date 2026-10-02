import re

with open('android/app/src/main/assets/index.html', 'r') as f:
    content = f.read()

# 1. Add context menu HTML & CSS
context_menu_html = """
  <!-- Tree Context Menu -->
  <style>
    .tree-context-menu {
      position: fixed;
      background: #1f2335;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
      z-index: 10000;
      display: none;
      flex-direction: column;
      min-width: 150px;
      padding: 4px 0;
    }
    .tree-context-menu.open {
      display: flex;
    }
    .tree-context-menu-item {
      padding: 12px 16px;
      font-size: 14px;
      color: #c0caf5;
      background: transparent;
      border: none;
      text-align: left;
      cursor: pointer;
    }
    .tree-context-menu-item:hover {
      background: rgba(255, 255, 255, 0.05);
    }
    .tree-context-menu-item.danger {
      color: #f7768e;
    }
  </style>
  <div class="tree-context-menu" id="treeContextMenu">
    <button class="tree-context-menu-item" id="ctxBtnNewFile">New File</button>
    <button class="tree-context-menu-item" id="ctxBtnMove">Move</button>
    <button class="tree-context-menu-item danger" id="ctxBtnDelete">Delete</button>
  </div>
  <div id="treeContextMenuBackdrop" style="display:none; position:fixed; top:0; left:0; right:0; bottom:0; z-index:9999;"></div>

  <!-- Move Modal -->
  <div class="modal" id="moveModal">
    <div class="modal-content" style="max-width:320px;">
      <h3 style="margin-top:0;">Move Item</h3>
      <p style="font-size:14px; color:var(--muted);">Select destination folder ID (or leave blank for root):</p>
      <input type="text" id="inputMoveTargetDocId" placeholder="Parent docId" style="width:100%; padding:10px; border-radius:6px; background:#181b2a; color:#fff; border:1px solid rgba(255,255,255,0.1); margin-bottom:16px;">
      <div style="display:flex; justify-content:flex-end; gap:8px;">
        <button class="btn btn-outline" id="btnCancelMove">Cancel</button>
        <button class="btn btn-primary" id="btnConfirmMove">Move</button>
      </div>
    </div>
  </div>
"""

if 'id="treeContextMenu"' not in content:
    content = content.replace('</body>', context_menu_html + '\n</body>')

# 2. Add '⋮' icon in buildTreeNode
more_icon_js = """
      const moreIcon = document.createElement('span');
      moreIcon.innerHTML = '&#8942;';
      moreIcon.style.marginLeft = 'auto';
      moreIcon.style.padding = '0 8px';
      moreIcon.style.fontSize = '18px';
      moreIcon.style.color = '#7a88cf';
      moreIcon.onclick = (e) => {
        e.stopPropagation();
        openTreeContextMenu(item, e.clientX, e.clientY);
      };
      selfEl.appendChild(moreIcon);
"""

# We need to inject this into both the directory block and the file block of buildTreeNode
# Directory block:
if 'moreIcon.innerHTML' not in content:
    content = re.sub(
        r'(title\.className = \'tree-item-inner\';\s*title\.textContent = item\.name;\s*selfEl\.append\(chevron, folderIcon, title\);)',
        r'\1\n' + more_icon_js,
        content
    )
    # File block:
    content = re.sub(
        r'(title\.className = \'tree-item-inner\';\s*title\.textContent = item\.name;\s*selfEl\.append\(fileIcon, title\);)',
        r'\1\n' + more_icon_js,
        content
    )

with open('android/app/src/main/assets/index.html', 'w') as f:
    f.write(content)
print("HTML and UI patches applied")
