with open('android/app/src/main/assets/index.html', 'r') as f:
    html = f.read()

with open('scratch/tab_css.txt', 'r') as f:
    css = f.read()

# find #settingsModal.open {
idx = html.find('#settingsModal.open {')
idx2 = html.find('}', idx) + 1

new_html = html[:idx2] + '\n' + css + html[idx2:]
with open('android/app/src/main/assets/index.html', 'w') as f:
    f.write(new_html)
