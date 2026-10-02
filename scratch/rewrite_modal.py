import re

with open('scratch/settingsModal.html', 'r') as f:
    html = f.read()

# We want to replace everything inside `<div class="settings-body">` up to the closing `</div>` of `settingsModal`.
# So let's extract the header, the body groups, and then output the tabbed structure.

header_end = html.find('<div class="settings-body">')
header = html[:header_end]
body = html[header_end + len('<div class="settings-body">'):]
# Body ends with a few </div>
# The last two </div> are closing settings-body and settingsModal.
body = body.rsplit('</div>', 2)[0]

# Split body into groups by `<div class="settings-group"`
groups = body.split('<div class="settings-group')
# Reconstruct groups
reconstructed_groups = []
for g in groups[1:]:
    reconstructed_groups.append('<div class="settings-group' + g)

# Let's map titles to their groups
group_map = {}
for g in reconstructed_groups:
    title_match = re.search(r'<div class="settings-group-title">(.*?)</div>', g)
    if title_match:
        title = title_match.group(1).strip()
        group_map[title] = g
    else:
        # Just in case
        group_map["Unknown"] = g

# Now we construct the tabbed HTML.
# Tabs: General, Controls, Advanced

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
    "App Info & Updates"
]

new_html = header
new_html += '''
  <!-- Tab Header -->
  <div class="settings-tabs">
    <button class="settings-tab active" data-tab="tab-gen">General</button>
    <button class="settings-tab" data-tab="tab-ctrl">Controls</button>
    <button class="settings-tab" data-tab="tab-adv">Advanced</button>
  </div>
  
  <div class="settings-body" style="padding-top: 12px;">
'''

new_html += '    <div class="settings-tab-content active" id="tab-gen">\n'
for title in general_titles:
    if title in group_map:
        new_html += group_map[title]
new_html += '    </div>\n'

new_html += '    <div class="settings-tab-content" id="tab-ctrl">\n'
for title in controls_titles:
    if title in group_map:
        # In Controls, let's break it down into 3 cards if it's one big group.
        # Actually, let's just output the group for now. It's already separated by settings-card-subtitle.
        # But wait, it's one giant card. Let's split that giant card into smaller cards.
        ctrl_group = group_map[title]
        # Find all subtitles
        # <div class="settings-card-subtitle">Touch Gestures</div>
        # We can replace <div class="settings-card-subtitle"> with closing the previous card and opening a new one.
        # Wait, the first one is after <div class="settings-card">
        ctrl_group = ctrl_group.replace('<div class="settings-card-subtitle">', '</div><div class="settings-group-title" style="margin-top:12px;">').replace('</div><div class="settings-group-title" style="margin-top:12px;">', '<div class="settings-group-title">', 1)
        # We also need to add <div class="settings-card"> after the new titles.
        parts = ctrl_group.split('<div class="settings-group-title"')
        new_ctrl_group = parts[0]
        for p in parts[1:]:
            title_end = p.find('</div>')
            sub_title = p[:title_end].strip()
            rest = p[title_end+6:]
            # Remove the original <div class="settings-card"> if it exists at the start
            rest = rest.replace('<div class="settings-card">', '', 1)
            new_ctrl_group += f'<div class="settings-group-title">{sub_title}</div><div class="settings-card">{rest}'
        
        # We need to fix the closing divs. Each new card needs a closing div. 
        # But wait, string replacement like this is risky. Let's just output the original group for Controls, it's safer, but add some padding and spacing so it doesn't look crowded.
        pass
        
        # Actually, let's just output the original ctrl_group for now.
        new_html += ctrl_group
        
new_html += '    </div>\n'

new_html += '    <div class="settings-tab-content" id="tab-adv">\n'
for title in advanced_titles:
    if title in group_map:
        new_html += group_map[title]
# Also catch any others just in case
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
