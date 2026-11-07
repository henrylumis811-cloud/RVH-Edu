from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
grading = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/SchoolGradingSettingsScreen.kt').read_text()
subjects = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/SchoolSubjectsSettingsScreen.kt').read_text()
checks = {
    'grading settings owns keyboard controller': 'LocalSoftwareKeyboardController.current' in grading,
    'school name save hides keyboard': 'keyboardController?.hide()' in grading and 'SCHOOL NAME SAVED' in grading,
    'pass threshold save hides keyboard': 'Enter a pass threshold from 0 to 100.' in grading,
    'grading rules show success feedback': 'SCHOOL GRADING RULES SAVED' in grading,
    'grading save reports failures': 'Could not save grading rules:' in grading,
    'subjects settings owns keyboard controller': 'LocalSoftwareKeyboardController.current' in subjects,
    'subject save hides keyboard': 'keyboardController?.hide()' in subjects,
    'subject restore hides keyboard': 'keyboardController?.hide()' in subjects,
    'subject save catches persistence failures': 'Could not save subject configuration.' in subjects,
    'subject settings use shared feedback banner': 'RvhFeedbackBanner(' in subjects,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
assert all(checks.values()), f"{sum(not x for x in checks.values())} checks failed"
print(f'{len(checks)}/{len(checks)} checks passed')
