# MD Viewer for Android

Welcome to **MD Viewer**! This is a high-performance, lightweight, and offline Markdown Outline Reader optimized to handle documents from small notes up to **1,000,000 lines** with 60 FPS smooth scrolling.

## Key Features

- **Blazing Fast Virtualization**: Renders only the visible rows in the viewport, regardless of whether the document has 10 lines or 1 million lines.
- **Hierarchical Outline Folding**: Click on any heading to toggle collapse and expand, or use level controls.
- **Bottom Navigation Bar**: 5 quick actions always at your fingertips.
- **Swipe-to-Open File Manager**: Swipe from the left edge of your screen to browse and open markdown files.
- **In-File Search**: Collapsible search bar with live matching and instant navigation.

## Bottom Action Bar

At the bottom of your screen, you have 5 dedicated buttons:

1. **Collapse Above**: Collapses all sections located above your current viewing position.
2. **Collapse All**: Folds every heading in the entire document for an instant high-level overview.
3. **Hover Mode Toggle**: Toggles Hover Preview mode.
   - When **ON**, tapping any heading opens a floating preview of that section without losing your place.
   - When **OFF**, tapping any heading directly collapses or expands it.
4. **Expand All**: Unfolds all headings and displays the entire document content.
5. **Expand Below**: Expands all sections located below your current viewing position.

## File Manager & Gestures

- **Swipe Left-to-Right**: Swipe horizontally starting from the left edge to open the **File Manager** drawer.
- **Open Any File**: Tap "Open Device File" to select any `.md`, `.markdown`, or `.txt` file on your device or cloud storage.
- **Built-in Benchmarks**: Generate up to 1,000,000 lines on demand to test performance.
- **Share & Receive**: Open Markdown files directly from WhatsApp, Telegram, Files, or web downloads.

## Markdown Elements Preview

### Text Formatting
You can write **bold text**, *italic text*, and `inline code blocks`.

### Blockquotes
> "Simplicity is prerequisite for reliability."
> — Edsger W. Dijkstra

### Lists
- Fast outline reading
- Instantaneous search
- Clean Tokyo Night theme

### Code Blocks
```python
def benchmark():
    lines = 1_000_000
    print(f"Viewing {lines} lines with zero lag!")
```

---
*Enjoy reading your Markdown files with MD Viewer!*
