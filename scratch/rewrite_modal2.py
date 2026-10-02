import re

with open('scratch/settingsModal.html', 'r') as f:
    html = f.read()

header_end = html.find('<div class="settings-body">')
header = html[:header_end]
body = html[header_end + len('<div class="settings-body">'):]
body = body.rsplit('</div>', 2)[0]

groups = body.split('<div class="settings-group"')
reconstructed_groups = []
for g in groups[1:]:
    # In case there's an id, it would be `<div class="settings-group" id="..."` but there is one: `<div class="settings-group" id="settingsGroupBasicStorage">`
    # Let's use a regex split instead.
    pass

import re
# Regex to find all settings-group
# We can just find them by looking for <div class="settings-group" or <div class="settings-group" id="...">
group_starts = [m.start() for m in re.finditer(r'<div class="settings-group"', body)]
reconstructed_groups = []
for i in range(len(group_starts)):
    start = group_starts[i]
    end = group_starts[i+1] if i+1 < len(group_starts) else len(body)
    reconstructed_groups.append(body[start:end])

group_map = {}
for g in reconstructed_groups:
    title_match = re.search(r'<div class="settings-group-title">(.*?)</div>', g)
    if title_match:
        title = title_match.group(1).strip()
        group_map[title] = g
    else:
        group_map["Unknown"] = g

general_titles = [
    "Document Actions",
    "Pocket Notes &amp; Color Palettes",
    "Basic Mode Storage &amp; Folders",
    "File History",
    "Help & Documentation"
]

controls_titles = [
    "Controls &amp; Gestures (Flashcard &amp; App)"
]

advanced_titles = [
    "Performance Stress Tests",
    "App Info & Updates" # Wait, the original has App Version / GitHub ?
]
# Let's print the found titles to be sure:
print("Found titles:", list(group_map.keys()))

new_html = header
new_html += '''
  <!-- Tab Header -->
  <div class="settings-tabs">
    <button class="settings-tab active" data-tab="tab-gen">General</button>
    <button class="settings-tab" data-tab="tab-ctrl">Controls</button>
    <button class="settings-tab" data-tab="tab-adv">Advanced</button>
  </div>
  
  <div class="settings-body" style="padding-top: 12px; padding-bottom: 24px;">
'''

new_html += '    <div class="settings-tab-content active" id="tab-gen">\n'
for title in general_titles:
    if title in group_map:
        new_html += group_map[title]
new_html += '    </div>\n'

new_html += '    <div class="settings-tab-content" id="tab-ctrl">\n'
for title in controls_titles:
    if title in group_map:
        ctrl_group = group_map[title]
        # Split the Controls group into multiple cards instead of one giant card
        # Find all subtitles
        # `<div class="settings-card-subtitle">Touch Gestures</div>`
        parts = re.split(r'(<div class="settings-card-subtitle">.*?</div>)', ctrl_group)
        # parts[0] is everything up to the first subtitle.
        # So parts[0] has `<div class="settings-group"> ... <div class="settings-card">`
        # Then parts[1] is subtitle 1, parts[2] is content 1
        # parts[3] is subtitle 2, parts[4] is content 2
        
        # We want each part to be its own group and card!
        new_ctrl_html = ""
        # The first bit before any subtitle:
        # Actually in index.html, Touch Gestures is the first thing in the card.
        # Let's just output them as separate settings-groups.
        
        # Let's reconstruct manually
        for i in range(1, len(parts), 2):
            subtitle_html = parts[i]
            content_html = parts[i+1]
            
            sub_title_text = re.search(r'> (.*?) <', subtitle_html.replace('>', '> ').replace('<', ' <'))
            if sub_title_text:
                st = sub_title_text.group(1).strip()
            else:
                st = subtitle_html.replace('<div class="settings-card-subtitle">', '').replace('</div>', '')
                
            # clean up any closing divs at the end of content if it's the last part
            if i == len(parts) - 2:
                # it's the last content, remove the trailing </div></div> for the card and group
                content_html = content_html.rsplit('</div>', 2)[0]
                
            new_ctrl_html += f'      <div class="settings-group">\n        <div class="settings-group-title">{st}</div>\n        <div class="settings-card">\n          {content_html.strip()}\n        </div>\n      </div>\n'
            
        new_html += new_ctrl_html
new_html += '    </div>\n'

new_html += '    <div class="settings-tab-content" id="tab-adv">\n'
for title in advanced_titles:
    if title in group_map:
        new_html += group_map[title]
for title, g in group_map.items():
    if title not in general_titles and title not in controls_titles and title not in advanced_titles:
        new_html += g
new_html += '    </div>\n'

new_html += '''
  </div>
</div>
'''

with open('scratch/settingsModal_new.html', 'w') as f:
    f.write(new_html)
