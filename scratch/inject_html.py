with open('android/app/src/main/assets/index.html', 'r') as f:
    html = f.read()

with open('scratch/settingsModal_new.html', 'r') as f:
    modal_html = f.read()

start_idx = html.find('<div id="settingsModal">')
end_idx = html.find('<!-- Auto Update Modal Dialog -->')

# The end_idx is after the settingsModal closing tags.
# We need to find the exact closing tag of settingsModal.
# In index.html, it's followed by <!-- Auto Update Modal Dialog -->
# Let's find the `</div>` before `<!-- Auto Update Modal Dialog -->`

real_end_idx = html.rfind('</div>', start_idx, end_idx) + 6

new_html = html[:start_idx] + modal_html + html[real_end_idx:]
with open('android/app/src/main/assets/index.html', 'w') as f:
    f.write(new_html)
