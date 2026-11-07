from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
screen = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/BulkMarkEntryScreen.kt').read_text()
checks = {
    'clear class and academic-year hierarchy': 'Academic year $year' in screen and 'MARK ENTRY' in screen,
    'learner count summary': '"${roster.size}"' in screen and 'LEARNERS' in screen,
    'compact marking summary frees vertical roster space': 'CURRENT MARKING' in screen and 'Text("Change")' in screen,
    'term selector is available in the marking setup sheet': 'ModalBottomSheet(' in screen and 'Term.entries.forEach' in screen and 'title = "TERM"' in screen,
    'section selector retained in marking setup sheet': 'TermSection.entries.forEach' in screen and 'title = "ASSESSMENT PERIOD"' in screen,
    'subject selector retained in marking setup sheet': 'subjects.forEach' in screen and 'title = "SUBJECT"' in screen,
    'entered and missing counts visible': '"$enteredCount / ${roster.size}"' in screen and '"$missingCount missing"' in screen,
    'blank values explicitly not zero': 'Blank marks are not zero' in screen,
    'learner rows use cards': 'Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)' in screen,
    'learner initials avatar': 'CircleShape' in screen and 'uppercaseChar()' in screen,
    'numeric input capped at 100': 'it <= 100' in screen,
    'unsaved-draft recovery preserved': 'persistRecoveryDraft()' in screen and 'Recovered unsaved marks' in screen,
    'save only enabled for changed values': 'enabled = roster.isNotEmpty() && changedCount > 0' in screen,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
if failed:
    raise SystemExit(f'{len(failed)} check(s) failed')
print(f'{len(checks)} mark-entry UI checks passed')
