from pathlib import Path
p = Path('app/src/main/java/com/henrylumis/rvhgrader/ui/BulkMarkEntryScreen.kt')
s = p.read_text()
checks = {
    'confirmation dialog': 'RvhConfirmDialog(' in s and 'Unsaved marks' in s,
    'keep editing action': 'Keep editing' in s,
    'discard action': 'Discard changes' in s,
    'term switch guarded': 'requestAction { onTermChange(t) }' in s,
    'section switch guarded': 'requestAction { onSectionChange(sec) }' in s,
    'subject switch guarded': 'requestAction { selectedSubject = subject.code }' in s,
    'back guarded': 'requestAction(onBack)' in s,
    'accurate saved status': 'All changes saved · Blank marks remain missing' in s and 'changedCount == 0' in s,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()):
    raise SystemExit(1)
print('BULK_MARKS_UNSAVED_GUARD_OK')
