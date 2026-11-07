from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
bulk = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/BulkMarkEntryScreen.kt').read_text()
sheet = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/MarkSheetScreen.kt').read_text()
checks = {
    'bulk screen detects IME visibility': 'WindowInsets.ime.getBottom(LocalDensity.current) > 0' in bulk,
    'bulk screen reclaims header space while keyboard is open': 'if (!keyboardVisible) Card(' in bulk and 'if (!keyboardVisible) OutlinedTextField(' in bulk,
    'bulk mark inputs request visibility when focused': 'BringIntoViewRequester()' in bulk and '.bringIntoViewRequester(markBringIntoView)' in bulk and 'markBringIntoView.bringIntoView()' in bulk,
    'individual mark sheet detects IME visibility': 'WindowInsets.ime.getBottom(LocalDensity.current) > 0' in sheet,
    'individual mark sheet collapses nonessential controls while keyboard is open': 'if (!keyboardVisible) {' in sheet and 'if (!keyboardVisible && assessment?' in sheet,
    'individual subject inputs request visibility when focused': 'BringIntoViewRequester()' in sheet and '.bringIntoViewRequester(subjectBringIntoView)' in sheet and 'subjectBringIntoView.bringIntoView()' in sheet,
    'both screens retain IME inset handling': '.imePadding()' in bulk and '.imePadding()' in sheet,
    'bulk save action remains present': 'Save $changedCount change' in bulk,
    'individual save and finalize actions remain present': 'Save draft' in sheet and 'Finalize assessment' in sheet,
}
for name, passed in checks.items():
    print(('PASS' if passed else 'FAIL') + ' ' + name)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
raise SystemExit(0 if all(checks.values()) else 1)
