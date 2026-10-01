# MD Viewer User Guide & Manual

Welcome to **MD Viewer**! A high-performance, lightweight, and 100% offline Markdown workspace for Android. Optimized for everything from quick daily notes to massive documents up to **1,000,000 lines** at a fluid 60 FPS.

---

## 🚀 1. Triple-App Architecture

MD Viewer provides three dedicated modes tailored for different writing, reading, and task workflows:

- **✍️ Notes Mode (Basic Editor & PocketMark Notes)**:
  - Designed for drafting notes, prose reading, and personal note organization.
  - Multi-note waterfall gallery with dynamic card heights, colored cards, split-screen comparison, and rich formatting.
  - 100% full-screen workspace with no empty bottom bar.
- **📑 Outline Viewer Mode**:
  - Designed for large-scale documentation, books, research, and Obsidian vaults.
  - Custom virtualized rendering pipeline capable of opening 100k to 1,000,000 lines with zero lag.
  - Quick note edit button (**`✏️`**) with **Large Document Guard** protection.
- **☑️ Checklist Mode**:
  - Virtualized interactive checklist reader and task manager.
  - Dedicated crisp SVG toolbar matching Outline mode: **Check all**, **Uncheck all**, **Undo**, and **Redo**.
  - Direct one-tap task toggling (`- [ ]` / `- [x]`) with animated custom squircle checkboxes and haptic feedback.

> [!TIP]
> **Switching Modes**: Switch anytime using the modern floating pill dropdown on the top header bar (**✍️ Notes** / **📑 Outline** / **☑️ Checklist**) with instant mode previews, or tap the mode tabs inside the left File Manager drawer.

---

## 🗂️ 2. PocketMark Notes Shelf

Your personal idea board and notebook shelf on Android:

- **Waterfall Masonry Layout (Auto-Fit Height)**:
  - Cards dynamically stack in a 2-column waterfall layout with zero blank space below short cards. Subsequent notes automatically fill gaps directly beneath shorter notes.
- **Card Size Preview Controls**:
  - Tap **`↕️`** on the top shelf toolbar to cycle global card preview sizes (Full, Compact, Medium).
  - **Dual Compact / Expand Buttons**: Long cards feature toggle buttons at **both the top and bottom** (`▲` / `▼`). You can collapse a long note from the bottom (`▲`) without scrolling all the way back up, or expand (`▼`) when previewing!
- **Star (⭐) & Pin (📌) Notes**:
  - **Pin to Top**: Tap **`📌`** / **`📍`** on any card or the editor top header to pin critical notes. Pinned notes feature a luminous accent badge and always float to the top of the shelf.
  - **Star Notes**: Tap **`⭐`** / **`☆`** on any card or editor header to mark favorites.
  - **Quick Filter Chips**: When pinned or starred notes exist, dedicated **`📌 Pinned`** and **`⭐ Starred`** chips appear in the filter row for one-tap filtering.
- **Color Palettes & Dynamic Themes**:
  - **Change Note Color**: Tap the interactive color dot on any card or the color circle in the editor header / tag bar to open the color picker popover.
  - **5 Curated Preset Palettes**: Tokyo Night, Pastel Dream, Vibrant Pop, Nord Frost, and Earthy Forest.
  - **Custom Palette Designer**: Tap **`＋ Create Custom Palette`** in Settings (or shortcut **`🎨 Palettes`** in the color picker) to define your own named palettes with custom colors via interactive hex/native pickers.
  - **Custom Hex Input**: Enter or pick any freeform hex color (`#rrggbb`) for any note.
  - **Random Color Assignment**:
    - Tap **`🎲 Random`** in the color popover to randomize a note's color on the fly.
    - Toggle **"Randomize New Note Color"** in Settings (ON by default) to automatically assign fresh random colors from the active palette whenever you create notes.
- **Tag Organization (`#tags`)**:
  - Add tags like `#work`, `#study`, `#todo` to your notes via **`🏷️`**.
  - Tap any tag chip on the shelf bar to instantly filter cards by topic.
- **Interactive Checklist Tasks**:
  - Toggle checklist items (`- [x]`) directly on cards without entering edit mode.
- **Multi-Tab Strip**:
  - Open multiple notes simultaneously in the editor's tab strip for rapid switching.

---

## 📁 3. Move Notes to Outline Vault

Easily graduate quick notes into your permanent file vaults:

1. **Open Move Dialog**:
   - Tap **`📁`** in the top action bar when viewing or editing a note.
   - Tap **`📁`** in the Markdown Editor formatting toolbar.
   - Tap the **`📁`** quick action button on any note card footer on the shelf.
   - Tap **`📁`** on the reading mode floating bar.
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

The editor toolbar and interface are streamlined with space-optimized, icon-only buttons designed specifically for mobile screens:

- **Compact Formatting Toolbar Icons**:
  - **Headings & Text Styles**: `H1`, `H2`, `H3`, `B` (Bold `**`), `I` (Italic `*`), `S` (Strikethrough `~~`), `` `code` `` (Inline code).
  - **Lists & Elements**: `☑` (Task checklist), `•` (Bullet list), `1.` (Numbered list), `>` (Blockquote), `</>` (Fenced code block), `▦` (ASCII diagram), `⊞` (Markdown table), `―` (Horizontal divider).
  - **Obsidian Extensions**:
    - Wikilinks: `[[ ]]` inserts `[[Note Title]]`.
    - Callouts: `[!]` inserts Obsidian callout blocks (`> [!NOTE]`, `> [!TIP]`, `> [!WARNING]`).
  - **Editor Actions**:
    - `⇄`: Toggle between soft line wrap and horizontal scrolling monospace diagram grid.
    - `📁`: Move current note into an Outline Vault directory.
    - `↶` / `↷`: Instant Undo and Redo.
- **Top Header Actions (Icon-Only)**:
  - `‹`: Return to Pocket Notes shelf.
  - `🏷️`: Manage note tags.
  - `📁`: Move note to Outline Vault.
  - `◫`: Toggle Split Screen mode.
  - `✍️` / `👁️`: Toggle between Markdown Editor and Formatted Prose Reader.
  - `🔍`: Search within the note.
  - `🗑️`: Delete note with confirmation.
- **Split Screen Mode (`◫`)**:
  - Preview formatted HTML alongside the raw markdown editor, or compare and edit two notes side-by-side.
  - Pane headers use compact `👁️` (Preview) and `✍️` (Edit) toggles to conserve horizontal space.
- **ASCII Box Diagrams & Grid Alignment**:
  - Tap **`▦`** to insert structured box-drawing templates.
  - Monospace character alignment is enforced with ligatures disabled (`liga 0`, `calt 0`) so box borders (`┌─┐`, `+---+`, `│`) align vertically.
  - Tap **`⇄`** to toggle soft wrap off for a wide ASCII grid.
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
- **Quick Note Edit Button (`✏️`) & Large Document Guard**:
  - Tap **`✏️`** on the top outline header bar to quickly fix typos or revise text using the rich markdown editor.
  - **Large Document Guard (>5,000 to 100,000+ lines)**: If the document is large, the Guard Modal prompts you to choose between:
    - *Edit Active Section Only*: Loads only the current heading's content (~50-300 lines) with zero latency.
    - *Edit Visible Window*: Loads ~300 lines around your current scroll position.
    - *Load Entire File Anyway*: Full file editing.
  - When finished, tap **`‹ Back`** to instantly splice edits back into the document and refresh the outline structure without memory spikes.

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
- **Reading Mode Floating Bar**: When reading formatted notes, a quick floating bar provides compact icon actions:
  - `✍️`: Switch to Markdown editor mode.
  - `📁`: Move or save note to Outline Vault.
  - `📑`: Open Table of Contents sidebar.
  - `📖`: Toggle 2-Page Book Mode (two-column landscape eBook view).
  - `↔`: Toggle Wide Edge-to-Edge layout.

---

## 🌟 8. What's New in v2.4.0

- **✨ Modern Custom Checkboxes**:
  - Upgraded all interactive task checkboxes across Notes mode, card previews, Outline mode, and Checklist mode with custom animated squircles (`20x20px`, smooth border radius).
  - Smooth vector checkmark SVG animation on check with dynamic spring scaling.
  - Generous touch target area (36x36px hit target) for effortless mobile tapping.
  - Tactile haptic micro-vibration feedback on check/uncheck.
  - Elegant smooth strike-through transition for completed tasks.
- **🌐 Safe HTML Rendering in Markdown**:
  - Full support for rendering HTML tags within Markdown documents without escaping them into raw code.
  - Safely renders inline elements: `<span style="...">`, `<font color="...">`, `<b>`, `<strong>`, `<i>`, `<em>`, `<u>`, `<s>`, `<del>`, `<ins>`, `<mark>`, `<kbd>`, `<code>`, `<sub>`, `<sup>`, `<abbr>`, `<br>`, `<img>`, and `<a>`.
  - Safely renders block elements: `<details><summary>...</summary>...</details>` with collapsible card styling, `<div>`, `<center>`, `<table>` with formatted cells, and `<hr>`.
  - Built-in XSS protection that automatically neutralizes unsafe event handlers (`on*`) and malicious pseudo-protocols (`javascript:`).
  - Supported in both Prose Reader notes and Outline / Checklist virtual scrollers!
- **⚡ Crisp Vector Checklist Toolbar**:
  - Checklist mode bottom bar upgraded with crisp 24x24 vector SVG icons (`stroke-width="2.2"`) matching Outline mode visual standards.
  - Two-line labels (`Check all`, `Uncheck all`, `Undo`, `Redo`) and active touch animations.
  - Active and disabled button state styling (reduced opacity and event prevention when undo/redo stacks are empty).
- **💎 Modern Floating Pill Mode Dropdown**:
  - Replaced the native system `<select>` with a modern floating pill button in the top bar.
  - Features current mode icon, mode title, and rotating animated chevron indicator.
  - Rich popover menu with frosted glassmorphism (`backdrop-filter: blur(16px)`), colored squircle icon badges, mode titles, descriptions, and active checkmarks.
  - Tap-to-dismiss backdrop and haptic vibration feedback.

---

## 🛡️ 9. What's New in v2.4.1

- **🔒 Google Play Protect Security Fix**:
  - Removed deprecated and flagged system intent extras (`EXTRA_NOT_UNKNOWN_SOURCE`) from package installation logic that triggered Play Protect Trojan/Dropper false positives.
  - Added Android 11+ (API 30+) `<queries>` intent declarations for package installation and web browsing.
  - Explicitly configured `android:debuggable="false"` in the application manifest.
  - D8 dex bytecode compiled with `--release` mode and minimum SDK 24 optimizations.
- **📖 In-App Updater Architecture Guide**:
  - Published comprehensive technical guide [`UPDATER.md`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/UPDATER.md) detailing the zero-dependency background updater architecture, GitHub Releases API integration, Android `DownloadManager`, and secure sandboxed `ApkProvider`.

---

## 🚀 10. What's New in v2.4.2

- **⚡ Massive Performance Boost for Big Files**:
  - **Offscreen DOM Virtualization**: Added CSS `content-visibility: auto; contain-intrinsic-size: auto 40px;` to `#docBody`, enabling files with 10,000+ to 100,000+ lines to render and scroll at smooth 60 FPS with minimal memory overhead.
  - **GPU-Accelerated Slide Drawer**: Upgraded `#fileDrawer` and `#drawerBackdrop` with dedicated 3D compositing (`translate3d`), layer containment (`contain: layout size style`), and `will-change: transform`. Swiping open the file manager slider is buttery smooth with zero dropped frames even with huge documents loaded.
  - **Zero-Thrash Double Page Mode**: Eliminated synchronous layout reflows during page turns by caching dimensions and throttling page indicators via `requestAnimationFrame`. Swiping between columns is silky smooth.
  - **Fast-Path Markdown & Outline Parsing**: Skipped regular expressions on plain lines in both Basic and Outline modes, dramatically boosting parsing and virtual scrolling speed.
  - **Delegated Event Handling**: Replaced individual event listeners on thousands of checkboxes, copy buttons, images, and wikilinks with high-performance event delegation on `docBody`.
  - **Sleek Touch Scroll Slider**: Added custom high-contrast, easily grabbable scrollbar thumbs for rapid document navigation.
  - **Responsive Edge Swipe**: Tuned left-edge swipe detection to respond immediately (`touchStartX <= 50`, `deltaX > 38`) without interference from vertical scrolling.
- **🛡️ Google Play Protect Full Security Clearance**:
  - Removed `REQUEST_INSTALL_PACKAGES` permission and `ApkProvider` package installer component to eliminate all dropper/PHA heuristics.
  - Sideloading updates now seamlessly hands off download to the user's default browser or Android system.
  - Signed official releases with dedicated `release.keystore` (RSA 2048, SHA256withRSA, v1+v2+v3 signature schemes).

---

## 🚀 11. What's New in v2.4.3

- **📲 Instant-Touch Real-Time Sidebar Swipe**:
  - Added real-time horizontal touch trajectory detection (`touchmove`) starting within 100px of the screen edge.
  - The drawer opens immediately during the swipe gesture without waiting for `touchend` release, completely eliminating gesture cancellation caused by Chromium Android WebView vertical scroll recognition.
  - Added subtle haptic tick confirmation on drawer open.
- **✨ Dedicated Two-Line Header Action Toolbar**:
  - Reorganized header layout into a 2-line responsive structure on mobile devices:
    - **Top Row**: Drawer toggle `☰` / Back to shelf `‹`, Mode Switcher pill (`✍️ Notes ▾`, `📑 Outline ▾`, `☑️ Checklist ▾`), full-width Document Title & Statistics, and Quick Search button `🔍`.
    - **Second Row (Action Toolbar)**: Spacious action bar with clear touch targets and labels: `✏️ Edit`, `Aa View`, `🗂 Cards`, `🔍 Search` in Outline/Checklist modes, and full note tools in Notes mode.
  - The file title and mode pill now enjoy abundant breathing room and are never truncated or cramped.
- **🔤 Restored & Enhanced `Aa` Typography, Zoom & View Options**:
  - Fixed issue where clicking `Aa` in Checklist mode displayed a blank modal.
  - Added full multi-mode support:
    - **Text Zoom**: Universal font size zoom (`A-`, `100%`, `A+`, `Reset`) across Notes, Outline, and Checklist modes.
    - **Outline Depth**: Quick heading level expanders (`H1` to `H6`).
    - **Checklist Batch Tools**: One-tap `Check All`, `Uncheck All`, `Undo`, and `Redo`.
    - **Reading Layout**: One-tap toggle between Full Wide and 2-Page Book reader layouts.
  - Added clean popover header (`Aa Typography & View`) with explicit close button `✕` and tap-away backdrop.
- **🎯 Clean, Compact Mode Switcher Dropdown**:
  - Fixed visual artifact where the mode dropdown appeared covered or clipped by document floating bars and HUD elements.
  - Elevated header and dropdown stacking context (`z-index: 600-700`) above all document layers.
  - Streamlined dropdown to a sleek, compact 170px menu with crisp mode rows and active checkmarks, eliminating bulky multi-line card wrappers.

---

## 🚀 12. What's New in v2.4.4

- **🏷️ Full HTML Open & Close Tag Recognition**:
  - **Preserved Closing HTML Tags**: Fixed HTML tokenization bug where closing tags (e.g. `</span>`, `</b>`, `</i>`, `</font>`, `</strong>`) were not preserved as safe tokens, causing them to be escaped into literal text like `&lt;/span&gt;` on screen.
  - **Paired Tag Tokenization**: Opening and closing tags are now paired and protected with collision-resistant safe tokens (`@@@SAFEHTML_N@@@`), preventing Markdown inline syntax collisions (e.g., italic/bold underscores) from breaking HTML structures.
  - **Flexible Whitespace Tolerance**: Enhanced regex matching to cleanly handle tags with inner spacing, such as `</span >`, `</ span>`, `<br />`, and `<hr />`.
  - **Standalone HTML Block Tag Handling**: Opening or closing HTML tags placed on their own lines (e.g., `<span style="...">` and `</span>` wrapping multiple lines) are recognized as standalone HTML blocks rather than being wrapped in conflicting `<p>...</p>` tags that break styling.
  - **Outline Scroller HTML Heading Rendering**: Formatted HTML tags in headings (`# Header with <span style="...">Color</span>`) now render formatted HTML in Outline mode and Hover Outline preview instead of escaped raw text.

---

## 🚀 13. What's New in v2.4.5

- **🚩 Checklist Checkpoints & Multi-Run Manager**:
  - **Save Checkpoints with Reminder Notes**: Save the exact state of what is checked and still unchecked at any moment with a custom label (e.g., `Bike 1: Trek FX`) and personal reminder notes (e.g., `Adjusted derailleur, waiting for brake pads; paused for lunch`).
  - **Vault-Integrated Hidden Storage (`.checkpoints/`)**: Checkpoints are saved directly inside your vault folder under `.checkpoints/`. They remain completely invisible in MD Viewer's vault file list (via dotfile filtering), yet are readily accessible and readable in Android file explorers (e.g., Files app with "Show hidden files" turned on) or on a PC.
  - **Human-Readable Markdown Checkpoint Files**: Each checkpoint is stored on disk as a clean Markdown document with YAML metadata and complete task snapshot, making it fully portable and human-readable outside the app.
  - **Start New Run / Fast Reset**: One-tap `🔄 Start New Run` unchecks all items with confirmation, enabling you to inspect a new bike/item immediately using the same template without losing prior runs.
  - **Instant Restore & Switch**: Browse saved checkpoints anytime, see completion statistics (`8/18 (44%)`), and tap `▶️ Restore / Switch` to restore prior checkbox states and display the reminder note.
  - **Multi-Touchpoints**: Easily accessible via header toolbar (`🚩 Checkpoints`), checklist bottom bar (`Checkpoints`), and typography/view menu (`Aa View`).

---

## 🚀 14. What's New in v2.4.6

- **⚡ Restored High-Performance Native In-App Updater**:
  - **System DownloadManager Integration**: Restored background updates via Android's native `DownloadManager` with notification drawer progress, zero background interruption, and network drop recovery.
  - **Sandboxed `ApkProvider`**: Re-enabled custom secure ContentProvider to hand off downloaded APK packages directly to the Android System `PackageInstaller`.
  - **Direct Installer Prompt & Fast Re-Install**: Tapping "Download & Install" downloads seamlessly in the background and prompts the installer immediately upon completion. If already downloaded, the button instantly switches to "🚀 Install Now".
  - **Intelligent Signature Matching**: Automatically detects whether current installation uses the official release key or debug key, downloading the exact matching release asset so update never fails with signature conflict.
  - **Seamless Unknown App Sources Flow (Android 8.0+)**: Directs user to grant permission when needed and automatically resumes installation as soon as the user returns.

---

*MD Viewer — Fast, Offline, Private Markdown Reading & Writing on Android.*
