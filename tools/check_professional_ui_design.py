from pathlib import Path

root = Path(__file__).resolve().parents[1]
theme = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/Theme.kt').read_text()
home = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/HomeScreen.kt').read_text()
readme = (root / 'README.md').read_text()

checks = {
    'light and dark theme palettes exist': 'lightColorScheme(' in theme and 'darkColorScheme(' in theme,
    'shared shape tokens are defined': 'private val RvhShapes = Shapes(' in theme,
    'professional academic brand palette': 'BrandBlue' in theme and 'BrandTeal' in theme,
    'dashboard shows active learner count': 'Active learners' in home and 'learnerCount' in home,
    'dashboard shows academic year': 'Academic year' in home and 'academicYear' in home,
    'dashboard includes all nine existing workflows': all(label in home for label in ['Learners', 'Enter marks', 'Class mark entry', 'Class results', 'Performance', 'Academic history', 'Import results', 'School settings', 'Backup & restore']),
    'dashboard retains Hellen dedication': 'In dedication to Hellen' in home,
    'no OCR feature added to dashboard': 'OCR' not in home,
    'visual design documented': '## Visual design' in readme,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)} UI design checks passed.')
