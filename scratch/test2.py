import re
with open('scratch/settingsModal.html', 'r') as f: html = f.read()
header_end = html.find('<div class="settings-body">')
body = html[header_end + len('<div class="settings-body">'):]
body = body.rsplit('</div>', 2)[0]
groups = body.split('<div class="settings-group')
reconstructed_groups = ['<div class="settings-group' + g for g in groups[1:]]
group_map = {}
for g in reconstructed_groups:
    m = re.search(r'<div class="settings-group-title">(.*?)</div>', g)
    if m: group_map[m.group(1).strip()] = g
print(group_map["Document Actions"][:60])
