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

Turn any markdown document, note, or synced Anki deck into interactive study flashcards:

- **Automatic Flashcard Generation**:
  - Headings (`# Heading`) and their body content automatically turn into Question / Answer cards.
  - Question-and-answer patterns (`Q: ... A: ...`) and term definitions (`Term :: Definition`) are detected.
  - Heading level filter allows studying specific depths (e.g., H2 for Anki imported decks, H1 for main chapters, or All).
- **🎴 Anki `.apkg` Imported Deck Full Compatibility**:
  - **Direct Desktop Sync Parity**: Full support for markdown notes and folders exported from desktop MD Viewer's APKG importer.
  - **Inline Audio & Flashcard Audio Wraps**: Offline audio playback via zero-latency base64 streaming and SAF tree resolution (`[sound:filename.mp3]` / `🔊 audio.mp3`).
  - **Audio Auto-Play & Replay Shortcut**: Automatically plays card audio upon advancing or flipping. Press **`R`** on a physical keyboard or tap the **`🔊`** button on either the front or back face to replay audio anytime.
  - **Embedded Image Thumbnails & Interactive Lightbox Zoom**: Images embedded in cards (`![alt](image.png)` or `<img src="...">`) render with smooth thumbnails. Tap any image to open the full-screen interactive lightbox with zoom in (**`+`**), zoom out (**`−`**), and **`1:1`** reset.
  - **Glowing Metadata Badges & Cloze Highlights**: Formats key-value pairs (`**Word:** ...`, `**IPA:** ...`, `**Meaning:** ...`) with vibrant glowing badges (`.fc-key-badge`), plus full styling for Obsidian cloze highlights (`==highlight==`), strikethrough (`~~text~~`), and task checkboxes.
- **📁 Unified Folder Deck Mode (`🎴 Folder Deck`)**:
  - Study an entire folder, subfolder, or full vault as a unified, seamless flashcard deck across multiple markdown files.
  - Select heading level filter (H2 recommended for Anki imported decks, H1, H3, H4, or All levels).
  - Optional recursive scanning across nested subdirectories.
  - One-tap access from the Flashcard toolbar (**`🎴 Folder Deck`**) or by long-pressing / tapping the `⋮` menu on any folder in the File Tree drawer and choosing **`🎴 Study Folder Deck`**.
  - Active folder deck indicator badge displaying current folder, heading filter level, and card count with instant one-tap exit (`✕`).
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

## 🚀 15. What's New in v2.4.7

- **🛡️ Android 13+ PackageInstaller Session Pipeline**:
  - **Modern Session-Based Installation**: Implemented modern `android.content.pm.PackageInstaller.Session` API for Android 13+ and Android 12+, streaming APK bytes directly into system install sessions without exposing world-accessible files.
  - **`PACKAGE_SOURCE_DOWNLOADED_FILE` (API 33+)**: Configures `PackageInstaller.SessionParams.setPackageSource(PACKAGE_SOURCE_DOWNLOADED_FILE)` to correctly attribute the installation source to a user-downloaded package file on Android 13+.
  - **`USER_ACTION_NOT_REQUIRED` & Update Ownership (API 31+ & 34+)**: Configured `UPDATE_PACKAGES_WITHOUT_USER_ACTION` permission with `setRequireUserAction(USER_ACTION_NOT_REQUIRED)` and `setRequestUpdateOwnership(true)` for fluid, frictionless in-place updates.
  - **Asynchronous Status Callback Receiver**: Added dedicated `InstallStatusReceiver` to cleanly handle lifecycle states (`STATUS_PENDING_USER_ACTION`, `STATUS_SUCCESS`, and error diagnostics) with automatic user action dispatch.
  - **Dual-Engine Architecture**: Automatically attempts modern `PackageInstaller` sessions first, with instant seamless fallback to `Intent.ACTION_VIEW` via sandboxed `ApkProvider` for legacy Android environments.

---

## 🚀 16. What's New in v2.4.8

- **🛡️ Google Play Protect Clearance (Bibliotheca Alignment)**:
  - Aligned permissions strictly with the trusted Bibliotheca architecture (`INTERNET`, `ACCESS_NETWORK_STATE`, `REQUEST_INSTALL_PACKAGES`).
  - Completely purged `UPDATE_PACKAGES_WITHOUT_USER_ACTION` and background status broadcast receivers, which trigger Play Protect heuristic flags against sideloaded applications.
  - Restored verified, 100% compliant `Intent.ACTION_VIEW` package installation with secure `ApkProvider` URI sharing and system unknown app source management.
- **✨ Fixed Checklist Mode Button & Visual Integrity**:
  - Replaced corrupted/unrendered `☑️` Unicode emoji glyphs with sharp, resolution-independent vector SVG check icons across the header mode pill, dropdown menu, drawer tabs, and batch action popovers.
  - Added dedicated **Checklist Mode Toggle Button** (`☑ Checklist` / `📑 Outline`) directly in the header action bar for instant one-tap switching without opening dropdown menus.
  - Cleaned up conflicting checkbox CSS rules, ensuring custom animated squircle checkboxes render with fluid transitions and spring animations.
- **🚩 Comprehensive Checkpoint & Multi-Run Management**:
  - Checkpoint manager is universally available across Checklist, Outline, and Notes modes.
  - Save current run state with custom title (e.g. `Bike 1: Trek FX`) and personal reminder notes.
  - One-tap `▶️ Restore / Switch` restores checkbox state and shows active run banner.
  - One-tap `📋 Copy` copies full run summary report to clipboard for quick sharing.
  - Full support for editing reminder notes (`✏️ Note`) and deleting checkpoints (`🗑️`) from both local device storage and hidden vault `.checkpoints/`.
  - Fast reset (`🔄 Start New Run`) unchecks all tasks for the next bike/run with a single tap.

---

## 🚀 17. What's New in v2.4.9

- **🎨 Redesigned Header Toolbar & Eliminated Text Overlap**:
  - **Spacious Pill Buttons**: Eliminated the restrictive 30px fixed-width rule that caused text labels (`✏️ Edit`, `Checklist`, `Aa View`, `🚩 Checkpoints`, `🗂 Cards`, `🔍 Search`) to overlap and crush into each other.
  - **Comfortable 44px Bar Height**: Increased `.header-toolbar-row` height to 44px with 8px button gaps and generous 32px pill buttons (`border-radius: 999px; padding: 0 12px; font-size: 12.5px;`).
  - **Smooth Horizontal Touch Scrolling**: Allowed the secondary action group to expand to natural button widths with seamless kinetic scrolling without truncating buttons.

- **📱 Redesigned Bottom Action Toolbar & Accessibility**:
  - **Comfortable 64px Mobile Bar**: Increased `#bottomBar` height to 64px (+ `env(safe-area-inset-bottom)`), providing comfortable 54px touch targets compliant with Android accessibility standards.
  - **Clean Single-Line Labels & Zero Overlap**: Replaced cramped 8.5px micro-text and brittle `<br>` line breaks with crisp, legible 11px font:
    - **Outline Bar**: `Above` | `Collapse` | `Hover` | `Expand` | `Below`
    - **Checklist Bar**: `Check All` | `Uncheck` | `Undo` | `Redo` | `Points`
    - **Notes Bar**: `Edit` | `TOC` | `2-Page` | `More`
  - **Enlarged Touch Targets & Icons**: Increased vector icon containers to 22px with refined active states and tactile feedback.

- **📖 In-Depth In-App Updater Architecture & Documentation**:
  - Published comprehensive technical guide [`UPDATER.md`](file:///run/host/home/dkchw/Documents/Code/Ongoing/Repo/mdviewer_android/UPDATER.md) detailing the zero-dependency background updater architecture, GitHub Releases API integration, Android `DownloadManager`, and secure Android Jetpack `FileProvider`.

---

## 🚀 18. What's New in v2.4.10

- **🛡️ Google Play Protect Security Alignment with Trainly**:
  - **Standard Android Jetpack `FileProvider`**: Replaced custom `ApkProvider` with official `androidx.core.content.FileProvider` and authority `com.mdviewer.app.provider` defined via `res/xml/file_paths.xml`.
  - **Zero Dropper/PHA Heuristics**: Eliminated custom ContentProvider raw file descriptors, removed `<queries>` for `package-archive`, and dropped manual `grantUriPermission` loops that triggered Play Protect static scanners.
  - **Standard OS Intent Package Handover**: Handed off packages directly via `Intent.ACTION_VIEW` with `FLAG_GRANT_READ_URI_PERMISSION`, completely identical to the architecture tested and approved in Trainly.

- **📱 Silky-Smooth Side Panel Drawer & Enhanced Gestures**:
  - **Effortless Edge Swipe**: Widened bezel swipe detection zone to 85px and relaxed angle tolerance (`deltaX > Math.abs(deltaY) * 0.55`) so natural thumb arc swipes trigger instantly without strict horizontal alignment.
  - **Fast Responsive Trigger**: Reduced movement requirement from 40px down to 22px with subtle haptic feedback for snappy, fluid response.
  - **Silky 60/120fps Animation**: Updated drawer transition to Material 3 decelerate curve (`0.28s cubic-bezier(0.1, 0.9, 0.2, 1)`) and deferred heavy DOM layout passes by 40ms so the slide-in animation experiences zero frame drops.
  - **Auto-Close Drawer on Settings**: Opening Settings from any entry point now automatically closes the side panel drawer.

- **⚡ Zero-Lag Startup & Instant Mode Switching**:
  - **Synchronous Virtual Row Rendering (`renderImmediate`)**: Eliminated the 1-frame blank flicker and stutter when switching between Notes, Outline, and Checklist modes by rendering visible rows synchronously before paint.
  - **Checklist Stats O(1) Cache**: Added dirty-flag cache for checklist progress computation, eliminating full-text line splitting and regex parsing on every mode switch.
  - **Lazy Drawer Recents Rendering**: Defer recent files DOM tree rebuilds while the drawer is closed, eliminating wasted main-thread work during mode switching.
  - **Cached Pocket Shelf Cards**: Re-use existing DOM nodes on shelf entry unless notes or filters actually changed, keeping tab switching instant.

---

## 🚀 19. What's New in v2.4.11

- **⚡ Eliminated Startup Freeze (~20s Hang Resolved)**:
  - **Pure Asynchronous Fetch**: Removed synchronous blocking HTTP requests on the Android WebView JavaScript thread. Background update checks now run through native browser `fetch()`, keeping app startup 100% instantaneous and smooth.
  - **WebView Cache Invalidation**: Added automatic WebView cache clearing upon package version upgrades so updated index assets and styles load freshly without stale version retention.

- **🛡️ 100% Google Play Protect & Security Clearance**:
  - **Standard Android SDK Debug Keystore Alignment**: Synchronized the legacy/debug keystore with the official standard Android SDK debug key (`D4:DD:5C:FC:78:EB:90:D4:13:FE:9B:CF:71:6B:2F:1D:03:E3:20:21:EA:FD:C5:05:A2:8C:07:B8:B6:8A:4D:3D`) identical to **Trainly** and **AI_Dict**.
  - **Manifest Hardening**: Removed hardcoded `android:debuggable="false"` from `AndroidManifest.xml` (allowing Gradle/AAPT2 to handle debuggable flags automatically) and configured `android:allowBackup="true"`.
  - **Multi-Path FileProvider**: Updated `file_paths.xml` with `<cache-path name="cache_updates" path="updates/" />` and `<external-path name="external_storage" path="." />` matching **Bibliotheca**.
  - **Robust Package Installer Handover**: Added `file.setReadable(true, false)` and `FLAG_ACTIVITY_CLEAR_TOP` for smooth system package installer prompts.

- **🎨 Redesigned Floating Settings Modal Popup**:
  - **True Modal Popup & Layering**: Elevated Settings backdrop (`z-index: 1400`) and modal container (`z-index: 1410`) safely above the side panel drawer (`z-index: 1200`), eliminating visual clipping and z-index overlap.
  - **Smooth Centered Pop Animation**: Replaced conflicting `@keyframes popUp` vertical translation overrides with `@keyframes modalPopIn` (`translate(-50%, -50%)`), completely eliminating jumps, jerks, and off-screen shifts.
  - **Instant Drawer Auto-Close**: Opening Settings from any entry point immediately triggers smooth drawer closing so the side panel slides away behind the focused modal.
  - **Material 3 / iOS Sliding Pill Switches**: Replaced plain HTML checkboxes with custom animated sliding pill switches (`.cfg-toggle`) featuring smooth spring knob animations and vivid accent states.
  - **Polished Controls & Typography**: Styled custom dark dropdowns (`.cfg-select`) with integrated chevrons, comfortable 52px touch-target list rows, interactive chevrons (`›`), monospace code boxes, and refined button actions.
  - **Dual-Channel Fallback Update Links**: Provided direct fallback download links for both the Official Release APK and Debug-Key APK in the update dialog to guarantee updates can never be blocked by signature mismatches.

---

## 🚀 20. What's New in v2.4.12

- **☑️ Heading-Level Batch Item Selection (Select / Deselect All in Section)**:
  - **One-Tap Section Checkboxes**: Every heading containing checklist tasks displays a dedicated squircle section checkbox (`[✓]`, `[ ]`, or `[-]` for partial progress) and a real-time progress pill (e.g. `2/5` or `5/5 ✓`).
  - **Click Heading to Select/Deselect All**:
    - **In Checklist Mode**: Tapping the heading row, text, section checkbox, or progress badge toggles all checklist tasks under that section (checks all if any are unchecked; unchecks all if all are done).
    - **In Outline Mode**: Interactive section checkboxes and progress pills appear on headings with tasks, enabling instant batch selection without switching modes while preserving outline navigation.
    - **In Basic Reader Mode**: Clicking any document heading with tasks instantly toggles all task checkboxes in that section and synchronizes back to the document editor.
  - **Full Hierarchy & Tree Scope**: Top-level headings govern all descendant tasks in their chapter, while subheadings scope strictly to their sub-section.
  - **Integrated History (Undo / Redo)**: Section toggles are recorded in the checklist history stack with crisp haptic feedback (`vibrate(14)`), auto-saving and supporting instant Undo/Redo.
- **📱 Flashcard & Gallery Review Mobile Usability Overhaul**:
  - **Prominent One-Tap Exit Button (`✕ Exit`)**: Added a high-contrast, touch-friendly 38px+ exit button in the top bar with clear icon and text, visible and clickable on all mobile viewports without notch interference.
  - **Fixed Z-Index Layering (`z-index: 1300`)**: Raised Flashcard overlay above top header bars (`z-index: 600`) and bottom toolbars, eliminating touch-blocking and layering conflicts on phones.
  - **Clean 2-Tier Responsive Mobile Toolbar**: Dedicated top tier for `✕ Exit`, `[🗂️ Study | ▦ Gallery]` switcher, and card stats; horizontally scrollable second tier for filters (`Outline`, `H1`, `H2`, `All`), Shuffle, 2-Col, Fullscreen, and Zoom controls.
  - **Full Mobile Gallery Usability**: Compact responsive face buttons (`🏷️ Front`, `📄 Back`, `◫ Both`), effortless one-tap card selection to jump directly from Gallery to Study mode, active touch feedback, and real-time search filtering.
  - **Complete Android Back Gesture & Navigation Support**: Overrode `onBackPressed()` in `MainActivity.kt` to seamlessly catch edge-swipe gesture navigation on modern Android phones (swiping back in Gallery returns to Study; swiping back in Study cleanly exits Flashcard mode).

---

## 🚀 21. What's New in v2.4.13

- **⚡ Zero-Delay Instant Startup (Eliminated ~20s Launch Freeze)**:
  - **Identified & Eliminated Startup Freeze**: Previously, opening a vault caused MD Viewer on subsequent launches to synchronously traverse nested directories and thousands of files via ContentResolver Binder IPC before rendering, locking the UI thread for 15–25 seconds.
  - **Instant Interactive Launch (0ms Delay)**: Vault tree initialization is now deferred until the user opens the slide-in sidebar drawer. App launch is now instantaneous and immediately interactive.
  - **Cached Vault Identity**: Added persistent SharedPreferences caching for `last_folder_name`, eliminating synchronous ContentResolver queries during app initialization.
  - **On-Demand Lazy Folder Tree Expansion**: Replaced full recursive filesystem scanning with single-level direct queries (`getTreeChildrenJson`), loading child items in ~2–3ms on demand only when folders are expanded.
  - **Traversed Query Caps & Safeguards**: Bounded fallback recursive tree traversal to safe depth and node limits, preventing Android Binder thread lockups.

---

## 🚀 22. What's New in v2.4.14

- **🔄 Fixed Updater Version Detection Loop**:
  - **Root Cause**: After installing a new version, old downloaded APK files from previous updates lingered in the downloads directory. The updater's `isUpdateDownloaded()` method detected *any* `mdviewer*.apk` file as a "ready update" — including stale APKs from already-installed versions — causing a perpetual "Install Now" loop.
  - **Fix**: On version change (first launch after update), all stale APK files from previous versions are automatically cleaned up. The version detection now strictly matches the requested version and never reports a download as "ready" if it matches the currently installed version.
  - **Auto-Update Toggle**: The startup auto-check now properly respects the "Check Updates on Startup" toggle setting.
- **🔒 Google Play Security Improvements**:
  - **Removed Broad FileProvider Path**: Removed the overly broad `external-path` entry from `file_paths.xml` that exposed the entire shared external storage. The updater only needs `external-files-path` (app-private directory), which is already declared.
  - **Normalized Version Comparison**: All version comparisons now consistently strip the `v` prefix before comparing, preventing edge cases with `v2.4.13` vs `2.4.13` format mismatches.
- **📱 Interactive Sidebar Drag-Tracking**:
  - **Finger-Following Drawer**: The sidebar now follows your finger in real-time as you drag from the left edge, with smooth hardware-accelerated transforms and backdrop opacity tracking.
  - **Tighter Edge Zone (40px)**: Reduced the swipe activation zone from 30–38% of screen width to a precise 40px left-edge-only zone, eliminating false drawer activations during normal content scrolling and interaction.
  - **Velocity-Based Snap**: Fast flicks instantly open/close the drawer. Slow drags snap based on a 40% threshold — if you've pulled the drawer past 40% of its width, releasing opens it; otherwise it snaps closed.
  - **Bidirectional Drag**: Both opening and closing the drawer support smooth interactive drag tracking. Swipe left anywhere on an open drawer to close it with finger-tracking feedback.

---

## 🚀 23. What's New in v2.4.15

- **✨ Settings Menu Redesign**:
  - **Tabbed Layout**: The massive settings popup has been overhauled into three clean, distinct tabs: **General**, **Controls**, and **Advanced**. 
  - **De-Crowded Controls**: The massive "Controls & Gestures" card has been broken apart into organized sub-sections (Touch Gestures, Keyboard Shortcuts, Gamepad Mapping) for significantly better readability and to prevent UI elements from squishing together on smaller screens.
  - **Smoother Animations**: Added elegant fade-in transitions when navigating between settings categories.

---

## 🚀 24. What's New in v3.0.0 (Major UX Overhaul & Play Protect Alignment)

This major release consolidates the massive sequence of updates focused on making MD Viewer more secure, stable, and fluid.
- **📱 True Fluid Sidebar Gestures**: The navigation drawer now perfectly follows your finger in real-time. We also eliminated false-activations by restricting the swipe zone strictly to the left 40px edge, and added velocity-snapping for snappy flicks.
- **⚙️ Complete Settings Menu Redesign**: The endless list of settings has been rebuilt into an elegant tabbed layout (General / Controls / Advanced), breaking down massive config cards to fix mobile layout squishing and drastically improve readability.
- **🔄 Bulletproof Auto-Updater**: Eliminated the dreaded "update loop" where the app would perpetually ask you to install old versions. Stale APK files are now cleaned automatically on startup, version strings are cleanly normalized, and the startup toggle is correctly honored.
- **🔒 Google Play Protect Security Fixes**: Removed overly broad `FileProvider` external paths to align with strict modern Android security scanning requirements, ensuring the app won't throw Play Protect warnings.

---

## 📁 26. What's New in v3.1.0 (File Management)

- **File Management**: Added the ability to natively Create, Move, and Delete files directly inside the file tree sidebar! Click the vertical three-dots (`⋮`) icon next to any file or folder to access the context menu.

---

## 🔍 27. What's New in v3.1.2 (Hover Mode Restoration & Performance)

- **Hover Mode Fully Restored**:
  - Fixed an issue where Outline Mode was unpopulated on initial startup when switching from Notes Mode, rendering the outline tree and hover previews inactive.
  - Added native synchronous asset loading via `AndroidBridge.readAssetFile`, bypassing WebView file-access security restrictions and loading the built-in Guide and documents instantly.
  - Mode switching now automatically synchronizes and parses the active document text into the Outline tree with zero empty screens.
  - Eliminated touch micro-scroll jitter that previously swallowed heading taps on mobile touchscreens.
  - Backdrops now seamlessly forward taps to underlying outline rows so you can rapidly tap different headings to preview them without dismissing the panel.
  - Added Hover Preview support when tapping headings directly in Notes Mode.

## 🔒 25. What's New in v3.0.3 (Critical Security Update)

- **Strict Updater Validation**: Safely hardcoded the internal updater to exclusively allow APK downloads from the official GitHub release repository. This fully satisfies Google Play Protect requirements without breaking the core WebView functionality.

---

## 🎴 28. What's New in v3.1.3 (Desktop Parity: Anki APKG Decks & Folder Deck System)

- **Complete Anki APKG Deck Parity**:
  - Full compatibility with Markdown decks generated from desktop MD Viewer's APKG importer.
  - **Inline Audio & Flashcard Audio Wraps**: Offline audio playback via zero-latency base64 streaming and SAF tree resolution (`[sound:filename.mp3]` / `🔊 audio.mp3`).
  - **Audio Auto-Play & Replay Shortcut**: Automatically plays card audio upon advancing or flipping. Press **`R`** on a physical keyboard or tap the **`🔊`** button on either the front or back face to replay audio anytime.
  - **Embedded Image Thumbnails & Interactive Lightbox Zoom**: Images embedded in cards render with smooth thumbnails. Tap any image to open the full-screen interactive lightbox with zoom in (**`+`**), zoom out (**`−`**), and **`1:1`** reset.
  - **Glowing Metadata Badges & Cloze Highlights**: Formats key-value pairs (`**Word:** ...`, `**IPA:** ...`, `**Meaning:** ...`) with vibrant glowing badges (`.fc-key-badge`), plus full styling for Obsidian cloze highlights (`==highlight==`), strikethrough (`~~text~~`), and task checkboxes.
- **Unified Folder Deck Mode (`🎴 Folder Deck`)**:
  - Study an entire folder, subfolder, or full vault as a unified, seamless flashcard deck across multiple markdown files.
  - Select heading level filter (H2 recommended for Anki imported decks, H1, H3, H4, or All levels).
  - Optional recursive scanning across nested subdirectories.
  - One-tap access from the Flashcard toolbar (**`🎴 Folder Deck`**) or by long-pressing / tapping the `⋮` menu on any folder in the File Tree drawer and choosing **`🎴 Study Folder Deck`**.
  - Active folder deck indicator badge displaying current folder, heading filter level, and card count with instant one-tap exit (`✕`).

---

## ⚡ 29. What's New in v3.1.4 (Flashcard Performance Overhaul & Memory Optimization)

- **⚡ Flashcard Gallery Virtualized Windowing & Infinite Scroll**:
  - Replaced synchronous full-deck DOM rendering with high-speed windowed batching (`30` cards per chunk).
  - Utilizes `IntersectionObserver` sentinel loading and scroll threshold detection for butter-smooth 60 FPS scrolling even across decks with 5,000+ cards.
  - Eliminated UI freeze when opening large decks or switching gallery view modes.
- **🚀 On-Demand Lazy Markdown & Audio Rendering**:
  - In Front-Only mode, hidden answer bodies are rendered lazily only when "Peek Answer" is tapped, slashing initial render times by over 90%.
  - Added HTML string caching (`_cachedBodyHtml`) so rendered markdown cards are never parsed twice.
  - Offloaded audio loading: removed synchronous multi-megabyte Base64 disk reads during HTML construction; audio URLs now resolve instantly and read bytes on-demand only upon playback.
- **🔋 Eliminated Idle Background CPU Burn**:
  - Fixed a continuous 60 FPS Gamepad `requestAnimationFrame` loop that was polling `navigator.getGamepads()` constantly on Android even when no controller was connected.
  - Polling now immediately suspends when no controller is connected and wakes only upon gamepad connection events.
- **🔍 Debounced Real-Time Search**:
  - Added a 150ms input debounce and lightweight pre-lowercased matching to search filtering, eliminating keystroke lag when typing.
- **🎨 Modern CSS Hardware Acceleration**:
  - Added `contain: content;` and `content-visibility: auto;` with intrinsic size hints to flashcard cards, allowing the Chromium compositor to skip layout passes for off-screen cards.

---

## 🎴 30. What's New in v3.1.6 (Folder Deck Picker, Native Audio Engine & Obsidian Media Fix)

- **📁 Interactive Folder Deck Dropdown & Datalist Picker**:
  - Replaced the static/readonly path field with a searchable type-in dropdown (HTML5 datalist) populated with vault folders, subfolders, and recent vaults.
  - Added a dedicated **📁 Pick** button to directly open Android's native Storage Access Framework folder picker without leaving the modal.
  - Fixed modal z-index layering (`z-index: 10050`): modal now opens immediately on top of the Flashcard view (even in fullscreen mode).
  - Fixed back button handling so pressing back with the Folder Deck modal open cleanly dismisses the modal rather than exiting flashcard mode.
- **🔊 Hardware-Accelerated Native Audio Playback**:
  - Added native Android `MediaPlayer` integration with SAF file descriptors in `MainActivity.kt` and `AndroidBridge.kt`, bypassing WebView Chromium audio streaming limitations.
  - Full support for Anki sound tags (`[sound:audio.mp3]`), markdown embeds (`![audio](voice.mp3)`), Obsidian wikilinks (`![[voice.mp3]]`), and HTML5 `<audio>`.
  - Removed forced `'assets/'` prepending, ensuring audio files in note folders, `attachments/`, `media/`, `collection.media/`, and vault root are properly resolved.
- **🖼️ Obsidian Wikilink & Markdown Image Rendering**:
  - Added support for Obsidian wikilink image embeds (`![[image.png]]`, `![[image.png|width]]`), standard markdown images (`![alt](url)`), and raw `<img>` tags across both front and back card faces.
  - Single-file flashcard review now leverages the complete `renderFcMarkdown` renderer with full card image styling and lightbox zoom.
  - Smart vault-wide media resolution: automatically searches note folders, `attachments/`, `media/`, `_media/`, `collection.media/`, `images/`, and recursive vault hierarchy.

---

## 🛡️ 31. What's New in v3.1.7 (Startup Freeze Fix & Boot Resilience Overhaul)

- **🛡️ Resolved Startup Screen Freeze**:
  - Eliminated syntax error in fallback media parsing that prevented client scripts from executing on launch.
  - Guaranteed instant, zero-delay cold and warm boot directly into the active mode (Notes/Basic, Outline, or Checklist).
- **🏗️ Clean DOM Hierarchy Architecture**:
  - Relocated tree context menu, backdrop, and move modals directly into the document `<body>` prior to script execution, ensuring all DOM nodes and element IDs are properly available on initialization.
  - Added defensive null checks across file tree context menu and move modal event listeners.
- **⚡ Fail-Safe Startup Pipeline**:
  - Wrapped all startup initialization routines (`loadPocketNotes`, `restoreLastOpenedBasic`, `restoreLastOpenedOutline`, `setAppMode`, zoom restoration, background update checker) in guarded try-catch boundaries with safe fallbacks.
  - Startup will now never halt or freeze even if local storage contains invalid or corrupted data.
- **🔍 Native Diagnostics & Logging**:
  - Added `WebChromeClient.onConsoleMessage` and `WebViewClient.onReceivedError` in `MainActivity.kt` for direct, high-visibility logging of WebView JavaScript messages and network errors to Android logcat.
  - Added global `window.onerror` and `window.onunhandledrejection` crash traps in the client runtime.

---

## ⚡ 32. What's New in v3.1.8 (Performance & UI Stability Overhaul)

- **⚡ 8x Faster Native File Opening**:
  - Upgraded Android Storage Access Framework (SAF) stream buffer from 8KB to 64KB (`CharArray(65536)`) across both tree and native content resolvers.
  - Eliminated duplicate note parsing calls during file load.
  - Optimized drawer recents caching with dirty flag updates, avoiding synchronous DOM layout thrashing on document load.
  - Immediate drawer auto-dismiss with micro-yield (`setTimeout`) before reading files to ensure silky smooth 60 FPS transitions.
- **🪟 Fixed Hover Outline Preview Panel**:
  - Elevated z-index of `#panel` (`1105`) and `#panelBackdrop` (`1100`) above the top header bar (`600`), preventing the panel and close button from being obscured by top bars.
  - Added dynamic safe area padding (`top: max(60px, calc(env(safe-area-inset-top, 0px) + 54px))`), keeping headers, zoom actions, and close buttons completely visible across landscape and portrait orientations.
  - Fixed backdrop dismissal behavior: tapping outside now reliably closes the hover preview without falsely triggering clicks on underlying virtual scroller rows.
  - Integrated hardware/gesture Back button support to dismiss the hover popup cleanly.
- **🔄 Universal Hover Mode Toggles**:
  - Added dedicated Hover Mode toggles (`ON`/`OFF`) in the *Aa View Options* modal for both Basic (Notes) Mode and Outline Mode.
  - Turning hover mode OFF now completely disables popup previews across both outline navigation and basic mode heading clicks.
  - Synchronized state across bottom bar, options modal badges, and persistent storage.
- **🗂️ Fixed Card Mode & Folder Decks**:
  - Resolved `ReferenceError` crash in `loadFolderDeck` by mapping directly to `enterFlashcardMode()`.
  - Added global safe fallback alias `window.openFlashcards`.
  - Added `🗂 Cards` quick-launch button in the Basic Mode header toolbar to enter flashcard study directly from notes.
  - Support studying folder decks and fallback across basic, outline, and raw documents seamlessly.

---

## ⚡ 33. What's New in v3.1.9 (Extreme Speed & Non-Blocking Architecture)

- **⚡ Instant Hover Popup with Progressive Micro-Batching**:
  - Implemented initial 35-line micro-batch rendering in `showPanel()`, displaying the hover preview in under **5 milliseconds** with zero perceptible delay.
  - Automatically streams remaining section lines asynchronously via micro-yield, ensuring instant opening and silky smooth interaction.
  - Added background pre-warming (`scheduleBackgroundBasicParse`) that parses document headings during CPU idle immediately after note loading, making heading preview taps instant.
- **🎴 Non-Blocking Folder Deck with Native Background Indexing**:
  - Vault Pre-Warming: Android background daemon executor indexes folder hierarchy and headings silently on low-priority IO threads as soon as a vault is opened.
  - In-Memory Native Caching: `folderCardsCache` and `folderTreeCache` provide **0ms instant access** when launching Folder Deck study or opening folder trees.
  - Asynchronous Bridge API: Added `collectFolderCardsAsync()` to keep the main UI completely interactive without application freezes during large folder deck generation.
  - Non-Blocking Progress Modal: Added `#fcDeckLoadingBackdrop` with animated spinner, real-time background status, and an instant Cancel button.
  - Payload Optimization: Capped individual card bodies during vault indexing to eliminate massive multi-megabyte JSON payloads.
- **🚀 10x Faster Flashcard Gallery**:
  - Reduced initial gallery batch size to 12 cards (`FC_GALLERY_BATCH_SIZE = 12`) to match the viewport and display the grid instantly (< 20ms).
  - High-Speed Gallery Card Renderer: Implemented `getGalleryCardBodyHtml()` with streamlined plain formatting, bypassing heavy KaTeX and complex multi-pass compilers for thumbnail cards.
  - Smart Gallery Caching: Cached formatted gallery HTML on card instances to maintain 60 FPS scrolling without re-formatting.
- **⚡ Fluid Single-File Flashcard Entry**:
  - Eliminated redundant AST re-parsing on flashcard entry when headings are already parsed in background.
  - Streamlined `collectFlashcards` with an instant O(1) return path for notes mode.

---

## 🛡️ 34. What's New in v3.1.10 (Instant Boot & Blank Screen Fix)

- **⚡ Zero-Delay Cold Boot & Blank Screen Elimination**:
  - Removed premature background vault indexing from Android Activity startup (`onCreate`).
  - Completely solved the black screen freeze caused by heavy SAF ContentResolver queries and binder saturation during WebView initialization.
  - The app now launches instantly in < 50ms straight into notes or outline reading with zero background I/O contention.
- **🛡️ Memory-Safe On-Demand Vault Scanner**:
  - Vault flashcards are now strictly scanned **on demand** when requested via Folder Deck with non-blocking progress dialog.
  - Added safety limits: capped at 1,000 files and 5,000 cards max, and skips files > 2MB to prevent `OutOfMemoryError` on massive repositories.
  - Comprehensive `Throwable` handling catches low-memory conditions gracefully and displays a user toast rather than terminating the process.
- **🖼️ Gallery Card Body Display Repair**:
  - Repaired the two-sided card view (`mode-both`) in Flashcard Gallery where the card answer body was inadvertently hidden due to a truncated markup block.
  - Restored full answer preview with instant cached plain rendering.
- **🔒 Multi-Process WebView Resilience**:
  - Added `onRenderProcessGone` handler in WebViewClient to prevent app termination in case of system memory reclaiming.
  - Safely wrapped all startup notifications and background parse timers with intelligent debouncing.

---

## ⚡ 35. What's New in v3.1.11 (Hover Preview Refactor & Zero-Lag Architecture)

- **🛡️ Crash-Proof Hover Outline Preview**:
  - Eliminated unhandled `TypeError` crashes occurring when previewing documents with missing heading metadata, negative indices, or out-of-bounds line targets.
  - Implemented comprehensive bounds-checking and fallback preview mode that gracefully renders the surrounding context when heading hierarchy is not yet computed.
- **⚡ Zero-Lag 60-Line Bounded Renderer (< 1ms)**:
  - Redesigned hover outline preview with an instantaneous 60-line bounded window rendered completely in a single animation frame.
  - Abolished asynchronous `setTimeout` progressive DOM batching queues, eliminating race conditions, UI stutter, and memory churn when tapping or hovering headings.
- **🎯 Container-Scoped Modal Scrolling**:
  - Replaced viewport-disrupting `scrollIntoView()` with internal `scrollPanelToTarget()` that computes `panelBody.scrollTop` directly.
  - Completely prevents outer page jumps, scrollbar jitter, and layout reflow in the main document reader.
- **🖼️ Lightweight Media Representation**:
  - Hover preview now renders embedded media (`![alt](url)` and Obsidian `![[file]]`) as lightweight badge pills (`🖼️ [Image]`) without loading actual `<img>` tags.
  - Completely prevents heavy SAF file queries, thumbnail image decoding, and network requests from bogging down the preview panel.
- **🚀 Removed Synchronous Bridge Calls**:
  - Eliminated synchronous `@JavascriptInterface` calls from `resolveMediaUrl`, routing media resolution entirely through asynchronous WebView asset interception.
  - Guarantees butter-smooth 60fps scrolling and zero UI thread stalls.
- **⏱️ Removed Redundant Background AST Timers**:
  - Stripped redundant 200ms background re-parse timeouts during basic reading mode, freeing CPU and battery.

---

*MD Viewer — Fast, Offline, Private Markdown Reading & Writing on Android.*


