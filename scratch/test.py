with open('scratch/settingsModal.html', 'r') as f:
    html = f.read()
header_end = html.find('<div class="settings-body">')
body = html[header_end + len('<div class="settings-body">'):]
body = body.rsplit('</div>', 2)[0]
groups = body.split('<div class="settings-group')
print(repr(groups[1][:50]))
