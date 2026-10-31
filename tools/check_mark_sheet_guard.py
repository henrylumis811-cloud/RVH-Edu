from pathlib import Path
p = Path('app/src/main/java/com/henrylumis/rvhgrader/ui/MarkSheetScreen.kt')
s = p.read_text()
checks = {
    'unsaved state compares current and persisted values': 'currentValues != savedValues' in s,
    'back navigation guarded': 'requestNavigation(onBack)' in s,
    'term change guarded': 'requestNavigation { onTermChange(t) }' in s,
    'section change guarded': 'requestNavigation { onSectionChange(s) }' in s,
    'discard confirmation exists': 'RvhConfirmDialog(' in s and 'Discard changes' in s,
    'keep editing action exists': 'Keep editing' in s,
    'missing subject names are shown': 'Still missing:' in s,
    'finalization disabled when subjects missing': 'enabled = missingCount == 0' in s,
    'draft/final status is explicit': 'DRAFT • INCOMPLETE' in s and 'FINAL • Locked for editing' in s,
    'successful draft save updates baseline': 'if (error == null) savedValues = values.toMap()' in s,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()):
    raise SystemExit(1)
print('MARK_SHEET_ASSESSMENT_GUARD_OK')
