from pathlib import Path
root = Path(__file__).resolve().parents[1]
ui = root / 'app/src/main/java/com/henrylumis/rvhgrader/ui'
banner = (ui / 'RvhFeedbackBanner.kt').read_text()
grading = (ui / 'SchoolGradingSettingsScreen.kt').read_text()
progression = (ui / 'ClassProgressionScreen.kt').read_text()
checks = {
    'shared feedback component supports success, error and info': all(x in banner for x in ['RvhFeedbackKind.Success', 'RvhFeedbackKind.Error', 'RvhFeedbackKind.Info']),
    'feedback uses themed container and content colors': 'MaterialTheme.colorScheme.secondaryContainer' in banner and 'MaterialTheme.colorScheme.errorContainer' in banner and 'MaterialTheme.colorScheme.primaryContainer' in banner,
    'feedback announces updates to accessibility services': 'liveRegion = LiveRegionMode.Polite' in banner,
    'feedback icon is decorative to avoid duplicate announcements': 'contentDescription = null' in banner,
    'grading settings use shared feedback': 'RvhFeedbackBanner(' in grading and 'RvhFeedbackKind.Success' in grading and 'RvhFeedbackKind.Error' in grading,
    'progression errors and success use shared feedback': 'RvhFeedbackBanner(it, RvhFeedbackKind.Error)' in progression and 'RvhFeedbackBanner(it, RvhFeedbackKind.Success)' in progression,
    'existing grading validation is retained': 'candidate.validationError()' in grading and 'Complete or remove every unfinished row' in grading,
    'progression success and error state logic is retained': 'successMessage = null' in progression and 'errorMessage = null' in progression,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)}/{len(checks)} feedback UI checks passed')
