from pathlib import Path
root = Path(__file__).resolve().parents[1]
ui = root / 'app/src/main/java/com/henrylumis/rvhgrader/ui'
single = (ui / 'MarkSheetScreen.kt').read_text()
bulk = (ui / 'BulkMarkEntryScreen.kt').read_text()
checks = {
    'single mark sheet controls software keyboard': 'LocalSoftwareKeyboardController.current' in single,
    'single mark draft save hides keyboard and clears focus': 'keyboardController?.hide(); focusManager.clearFocus(); val error = onSaveDraft' in single,
    'finalizing assessment hides keyboard and clears focus': 'keyboardController?.hide(); focusManager.clearFocus(); val error = onFinalize' in single,
    'reopen action clears keyboard focus': 'keyboardController?.hide(); focusManager.clearFocus(); message = onReopen()' in single,
    'draft success is not styled as an error': 'it.startsWith("Draft saved")' in single,
    'finalized and reopened statuses are styled as success': 'it.startsWith("Assessment finalized")' in single and 'it.startsWith("Reopened")' in single,
    'bulk marks save controls software keyboard': 'LocalSoftwareKeyboardController.current' in bulk,
    'bulk save hides keyboard and clears focus before persistence': 'keyboardController?.hide()\n                    focusManager.clearFocus()\n                    // Never write unchanged rows:' in bulk,
    'bulk partial failure message remains available': 'savedCount > 0 -> "Saved $savedCount; ${errors.size} failed.' in bulk,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)}/{len(checks)} mark-entry interaction checks passed')
