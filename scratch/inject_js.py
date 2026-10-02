with open('android/app/src/main/assets/index.html', 'r') as f:
    html = f.read()

js_code = """
  // Settings Tabs Logic
  const settingsTabs = document.querySelectorAll('.settings-tab');
  const settingsTabContents = document.querySelectorAll('.settings-tab-content');
  
  settingsTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      // Remove active from all
      settingsTabs.forEach(t => t.classList.remove('active'));
      settingsTabContents.forEach(c => c.classList.remove('active'));
      
      // Add active to clicked
      tab.classList.add('active');
      const targetId = tab.getAttribute('data-tab');
      const targetContent = document.getElementById(targetId);
      if (targetContent) targetContent.classList.add('active');
    });
  });
"""

idx = html.find("btnCloseSettings.addEventListener('click', closeSettings);")
if idx != -1:
    idx += len("btnCloseSettings.addEventListener('click', closeSettings);")
    new_html = html[:idx] + '\n' + js_code + html[idx:]
    with open('android/app/src/main/assets/index.html', 'w') as f:
        f.write(new_html)
else:
    print("Could not find insertion point!")
