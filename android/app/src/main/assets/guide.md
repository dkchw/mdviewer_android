# MD Viewer User Guide & Manual

Welcome to **MD Viewer**! A high-performance, lightweight, and 100% offline Markdown workspace for Android. Optimized for everything from quick daily notes to massive documents up to **1,000,000 lines** at a fluid 60 FPS.

---

## 🚀 1. Dual-App Architecture

MD Viewer provides two dedicated modes tailored for different writing and reading workflows:

- **✍️ Basic Editor & PocketMark Notes (Default)**:
  - Designed for drafting notes, prose reading, and daily task management.
  - Multi-note shelf with colored cards, interactive task checkboxes, split-screen comparison, and rich formatting.
- **📑 Outline Viewer Mode**:
  - Designed for large-scale documentation, books, research, and Obsidian vaults.
  - Custom virtualized rendering pipeline capable of opening 100k to 1,000,000 lines with zero lag.
  - Obsidian-style tree explorer with hierarchical folder guides and deep workspace search.

> [!TIP]
> **Switching Modes**: Tap the mode pills in the top action bar (**✍️ Basic** / **📑 Outline**) or select the target app mode inside the left File Manager drawer.

---

## 🗂️ 2. PocketMark Notes Shelf

Your personal idea board and notebook shelf on Android:

- **Card Size Preview Controls**:
  - Tap `↕️ Full / Compact / Medium` on the top shelf toolbar to cycle global card preview sizes.
  - **Dual Compact / Expand Buttons**: Long cards feature toggle buttons at **both the top and bottom** (`▲ Collapse` / `▼ Expand`). You can collapse a long note from the bottom without scrolling all the way back up!
- **Color Categorization**:
  - Assign notes to 6 Tokyo Night pastel tones (Slate, Teal, Purple, Coral, Amber, Green).
  - Tap color swatches on the shelf filter row to isolate notes of that category.
- **Tag Organization (`#tags`)**:
  - Add tags like `#work`, `#study`, `#todo` to your notes.
  - Tap any tag chip on the shelf bar to instantly filter cards by topic.
- **Interactive Checklist Tasks**:
  - Toggle checklist items (`- [x]`) directly on cards without entering edit mode.
- **Multi-Tab Strip**:
  - Open multiple notes simultaneously in the editor's tab strip for rapid switching.

---

## 📁 3. Move Notes to Outline Vault

Easily graduate quick notes into your permanent file vaults:

1. **Open Move Dialog**:
   - Tap **`📁 To Vault`** in the top action bar when viewing or editing a note.
   - Tap **`📁 Vault`** in the Markdown Editor formatting toolbar.
   - Tap the **`📁`** quick action button on any note card footer on the shelf.
   - Tap **`📁 To Vault`** on the reading mode floating bar.
2. **Choose Destination Vault**:
   - Pick your currently **Active Vault**, any saved vault in **Recent Vaults History**, or **`➕ Choose Another Folder / Vault...`** to pick any directory on your device via Android Storage Access Framework (SAF).
3. **Select Destination Subfolder**:
   - The subfolder dropdown automatically discovers and displays all directories inside your vault (e.g., `/`, `/Notes`, `/Daily`, `/Work/Projects`).
4. **Move vs. Copy**:
   - Check *"Delete from Pocket Notes after moving"* for a clean **Move**.
   - Uncheck to keep a duplicate copy on your Pocket Notes shelf (**Copy**).
5. **Instant Outline Navigation**:
   - A success banner will appear with a **`View in Outline ➔`** button. Tapping it switches to Outline Viewer mode, refreshes the folder tree, and loads your moved file immediately.

---

## ✍️ 4. Markdown Editor & Formatting Toolbar

The editor toolbar provides quick one-tap shortcuts for standard and extended Markdown syntax:

- **Headings & Text Styles**: `H1`, `H2`, `H3`, Bold (`**`), Italic (`*`), Strikethrough (`~~`), Inline code (`` `code` ``).
- **Lists & Elements**: Task lists (`☑ Task`), Bullet list (`•`), Numbered list (`1.`), Quote (`>`), Divider (`---`), Table (`▦ Table`).
- **Obsidian Extensions**:
  - Wikilinks: `[[Note Title]]`
  - Callouts: `> [!NOTE]`, `> [!TIP]`, `> [!WARNING]`
- **ASCII Box Diagrams & Grid Alignment**:
  - Tap **`▦ ASCII`** to insert structured box-drawing templates.
  - Monospace character alignment is enforced with ligatures disabled (`liga 0`, `calt 0`) so box borders (`┌─┐`, `+---+`, `│`) align vertically.
  - Tap **`⇄ Wrap`** to toggle between soft line wrap and horizontal scrolling monospace grid.
- **Split Screen Mode (`◫ Split`)**:
  - Preview formatted HTML alongside the raw markdown editor.
  - Or compare and edit two different pocket notes side-by-side simultaneously.
- **High-Performance Large Text Paste**:
  - Zero-allocation word and line counting algorithms process large text pastes (240k+ characters in 16ms) without locking the UI thread.

---

## 📑 5. Outline Viewer & Vault Management

Designed for reading massive documents and browsing full vaults:

- **Obsidian-Style Mobile Tree Explorer**:
  - Vertical indentation guides display clear folder hierarchies.
  - Folder count pills indicate the number of notes inside each directory.
  - Tap folder rows to expand/collapse subdirectories.
- **Vault Dropdown Switcher**:
  - Tap the vault header title in the drawer to open the Vault Switcher dropdown.
  - Switch between active vaults with one tap.
  - Reorder, rename folders on disk, or remove vaults from history.
- **5 Dedicated Bottom Outline Buttons**:
  1. **Collapse Above**: Folds all headings located above your current viewport.
  2. **Collapse All**: Folds every heading in the document for an instant high-level overview.
  3. **Hover Mode Toggle**:
     - When **ON**, tapping any heading opens a floating preview without losing your reading position.
     - When **OFF**, tapping any heading toggles folding directly.
  4. **Expand All**: Unfolds all headings to read the full document text.
  5. **Expand Below**: Unfolds all headings located below your current viewport.
- **Workspace Search Across All Vault Files**:
  - Switch the search bar scope from **File** to **Folder**.
  - Searches through all markdown documents in the vault, displaying matching lines and snippets with direct jump navigation.

---

## 🎴 6. Flashcard Study & Gallery Review System

Turn any markdown document or note into interactive study flashcards:

- **Automatic Flashcard Generation**:
  - Headings (`# Heading`) and their body content automatically turn into Question / Answer cards.
  - Question-and-answer patterns (`Q: ... A: ...`) and term definitions (`Term :: Definition`) are detected.
- **Study Flip Mode**:
  - Tap the card to flip between Question and Answer with 3D flip animation.
  - Navigation buttons (`‹ Prev` and `Next ›`) and touch swipe gestures.
  - Filter cards by Outline Scope (Current Section vs Entire Document).
- **Gallery Review Mode (`🖼️ Gallery`)**:
  - Review all cards in an infinite scroll gallery for quick revision before exams or meetings.
  - **3 Face Viewing Modes**:
    1. **Front Only**: Displays all question prompts.
    2. **Back Only**: Displays all answers / explanations.
    3. **Both (Side-by-Side)**: Displays front and back next to each other on the same card.
  - **Live Filter**: Search box allows filtering cards by keywords or headings instantly.

---

## 📱 7. Gestures & Navigation Shortcuts

- **Left-Edge Swipe**: Swipe horizontally from the left edge of your screen to open the **File Manager** drawer.
- **Hardware Back Button**:
  - Dismisses active modals (Move to Vault, Color Picker, Settings, User Guide).
  - Closes floating preview panels.
  - Exits search results.
  - Closes the drawer.
- **Double-Page Book Mode**: In Basic reading mode, tap `📖 2-Page` to read prose like an eBook in two-column landscape view.

---

*MD Viewer — Fast, Offline, Private Markdown Reading & Writing on Android.*
