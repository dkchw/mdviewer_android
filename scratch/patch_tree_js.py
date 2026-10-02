import re

with open('android/app/src/main/assets/index.html', 'r') as f:
    content = f.read()

js_logic = """
  // --- File Tree Context Menu Logic ---
  let activeTreeCtxItem = null;
  const treeContextMenu = document.getElementById('treeContextMenu');
  const treeCtxBackdrop = document.getElementById('treeContextMenuBackdrop');
  const ctxBtnNewFile = document.getElementById('ctxBtnNewFile');
  const ctxBtnMove = document.getElementById('ctxBtnMove');
  const ctxBtnDelete = document.getElementById('ctxBtnDelete');

  const moveModal = document.getElementById('moveModal');
  const inputMoveTargetDocId = document.getElementById('inputMoveTargetDocId');
  const btnCancelMove = document.getElementById('btnCancelMove');
  const btnConfirmMove = document.getElementById('btnConfirmMove');

  window.openTreeContextMenu = function(item, x, y) {
    activeTreeCtxItem = item;
    
    // Adjust menu items
    if (item.kind === 'directory') {
      ctxBtnNewFile.style.display = 'block';
    } else {
      ctxBtnNewFile.style.display = 'none';
    }

    treeContextMenu.style.left = Math.min(x, window.innerWidth - 160) + 'px';
    treeContextMenu.style.top = Math.min(y, window.innerHeight - 150) + 'px';
    treeContextMenu.classList.add('open');
    treeCtxBackdrop.style.display = 'block';
  };

  function closeTreeContextMenu() {
    treeContextMenu.classList.remove('open');
    treeCtxBackdrop.style.display = 'none';
    activeTreeCtxItem = null;
  }

  if (treeCtxBackdrop) {
    treeCtxBackdrop.addEventListener('click', closeTreeContextMenu);
  }

  if (ctxBtnNewFile) {
    ctxBtnNewFile.addEventListener('click', () => {
      if (!activeTreeCtxItem || activeTreeCtxItem.kind !== 'directory') return;
      const parentId = activeTreeCtxItem.id;
      const folderName = activeTreeCtxItem.name;
      const vaultUri = openedFolderTreeUri;
      closeTreeContextMenu();
      
      const fileName = prompt(`Create new markdown file in '${folderName}'\\nEnter file name (e.g. Note.md):`, 'Untitled.md');
      if (!fileName) return;

      if (window.AndroidBridge && window.AndroidBridge.createFileInTreeFolder) {
        try {
          const respRaw = window.AndroidBridge.createFileInTreeFolder(vaultUri, parentId, fileName, "");
          const resp = JSON.parse(respRaw);
          if (resp.status === 'ok') {
            alert('File created successfully!');
            renderRecentFilesForDrawer(true); // Refresh tree
          } else {
            alert('Error creating file: ' + (resp.message || 'Unknown error'));
          }
        } catch(e) { alert('Error: ' + e.message); }
      }
    });
  }

  if (ctxBtnDelete) {
    ctxBtnDelete.addEventListener('click', () => {
      if (!activeTreeCtxItem) return;
      const docId = activeTreeCtxItem.id;
      const name = activeTreeCtxItem.name;
      closeTreeContextMenu();
      
      if (!confirm(`Are you sure you want to delete '${name}'? This cannot be undone.`)) return;

      if (window.AndroidBridge && window.AndroidBridge.deleteTreeDocument) {
        try {
          const respRaw = window.AndroidBridge.deleteTreeDocument(docId);
          const resp = JSON.parse(respRaw);
          if (resp.status === 'ok') {
            renderRecentFilesForDrawer(true); // Refresh tree
          } else {
            alert('Error deleting: ' + (resp.message || 'Unknown error'));
          }
        } catch(e) { alert('Error: ' + e.message); }
      }
    });
  }

  if (ctxBtnMove) {
    ctxBtnMove.addEventListener('click', () => {
      if (!activeTreeCtxItem) return;
      // Because SAF move needs source Parent, we can only safely do it if we know the parent, 
      // but Android moveDocument allows null for sourceParent? No, it requires sourceParent.
      // Wait, let's just ask user for target ID, and maybe we can find source parent by traversing our JS tree.
      
      const itemToMove = activeTreeCtxItem;
      closeTreeContextMenu();

      // Find parent doc id from our cached `folderTreeItems` or tree traversal.
      let sourceParentId = openedFolderRootDocId; // Default to root
      function findParent(nodes, targetId, currentParent) {
        for (const n of nodes) {
          if (n.id === targetId) return currentParent;
          if (n.children) {
            const p = findParent(n.children, targetId, n.id);
            if (p) return p;
          }
        }
        return null;
      }
      
      if (typeof folderTreeItems !== 'undefined') {
        const found = findParent(folderTreeItems, itemToMove.id, openedFolderRootDocId);
        if (found) sourceParentId = found;
      }

      activeTreeCtxItem = itemToMove; // Restore for modal
      activeTreeCtxItem._sourceParentId = sourceParentId;

      inputMoveTargetDocId.value = '';
      moveModal.style.display = 'flex';
    });
  }

  if (btnCancelMove) {
    btnCancelMove.addEventListener('click', () => {
      moveModal.style.display = 'none';
      activeTreeCtxItem = null;
    });
  }

  if (btnConfirmMove) {
    btnConfirmMove.addEventListener('click', () => {
      if (!activeTreeCtxItem) return;
      const targetParentId = inputMoveTargetDocId.value.trim() || openedFolderRootDocId;
      const sourceId = activeTreeCtxItem.id;
      const sourceParentId = activeTreeCtxItem._sourceParentId;
      
      moveModal.style.display = 'none';
      
      if (window.AndroidBridge && window.AndroidBridge.moveTreeDocument) {
        try {
          const respRaw = window.AndroidBridge.moveTreeDocument(sourceId, sourceParentId, targetParentId);
          const resp = JSON.parse(respRaw);
          if (resp.status === 'ok') {
            renderRecentFilesForDrawer(true);
          } else {
            alert('Error moving: ' + (resp.message || 'Unknown error'));
          }
        } catch(e) { alert('Error: ' + e.message); }
      }
    });
  }
"""

if 'window.openTreeContextMenu =' not in content:
    content = content.replace('// --- Initial Load ---', js_logic + '\n  // --- Initial Load ---')
    with open('android/app/src/main/assets/index.html', 'w') as f:
        f.write(content)
    print("JS logic injected")
else:
    print("JS logic already exists")
