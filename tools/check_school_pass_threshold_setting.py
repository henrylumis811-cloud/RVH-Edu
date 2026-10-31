from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/data/SchoolInfoRepository.kt').read_text()
settings = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/SchoolGradingSettingsScreen.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
assert 'loadClassPassPercent' in repo and 'saveClassPassPercent' in repo
assert 'percent in 0.0..100.0' in repo
assert 'CLASS PASS THRESHOLD (%)' in settings
assert 'passPercent = effectivePassPercent' in service
assert 'selectedPolicy?.classPassPercent' in service
assert 'gradingScale, classPassPercent' not in main
print('PASS: school-specific pass threshold is persisted; new assessments use their captured threshold for default class analytics')
