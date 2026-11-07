from pathlib import Path
p = Path('app/src/main/java/com/henrylumis/rvhgrader/ui/BulkMarkEntryScreen.kt')
s = p.read_text()
checks = {
    'confirmation dialog': 'RvhConfirmDialog(' in s and 'Unsaved marks' in s,
    'keep editing action': 'Keep editing' in s,
    'discard action': 'Discard changes' in s,
    'term switch guarded through Apply action': 'requestAction {' in s and 'onTermChange(pickerTerm)' in s,
    'section switch guarded through Apply action': 'onSectionChange(pickerSection)' in s,
    'subject switch guarded through Apply action': 'selectedSubject = pickerSubject' in s,
    'back guarded': 'requestAction(onBack)' in s,
    'accurate saved status': 'All changes saved · Blank marks remain missing' in s and 'changedCount == 0' in s,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()):
    raise SystemExit(1)
print('BULK_MARKS_UNSAVED_GUARD_OK')
