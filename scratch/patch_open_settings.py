with open('android/app/src/main/assets/index.html', 'r') as f:
    html = f.read()

target = "if (targetSection === 'pocketPalettes' && cfgActivePalette) {"
replacement = """
    // Ensure the General tab is active before scrolling
    if (typeof settingsTabs !== 'undefined') {
      settingsTabs[0].click();
    }
    if (targetSection === 'pocketPalettes' && cfgActivePalette) {
"""

html = html.replace(target, replacement)
with open('android/app/src/main/assets/index.html', 'w') as f:
    f.write(html)
