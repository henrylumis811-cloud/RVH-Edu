#!/usr/bin/env python3
"""Source-level regression contracts for V188 bulk mark entry layout."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/BulkMarkEntryScreen.kt').read_text()
gradle = (root / 'app/build.gradle.kts').read_text()
workflow = (root / '.github/workflows/android-build.yml').read_text()
checks = {
    'expanded selector replaced by compact current-selection summary': 'CURRENT MARKING' in screen and 'Assessment period' not in screen.split('CURRENT MARKING')[1].split('Row(Modifier.fillMaxWidth(), verticalAlignment')[0],
    'marking setup opens a Material 3 modal bottom sheet': 'ModalBottomSheet(' in screen and 'showAssessmentPicker = true' in screen,
    'term, assessment period, and subject are selectable in sheet': all(x in screen for x in ['Term.entries.forEach', 'TermSection.entries.forEach', 'subjects.forEach', 'FilterChip(']),
    'responsive selector chips wrap on narrow screens': 'FlowRow(' in screen and 'ExperimentalLayoutApi::class' in screen,
    'selection is explicitly applied together': 'Text("Apply selection")' in screen and 'onTermChange(pickerTerm)' in screen and 'onSectionChange(pickerSection)' in screen and 'selectedSubject = pickerSubject' in screen,
    'unsaved mark confirmation guard remains active': 'requestAction {' in screen and 'if (changedCount > 0) pendingAction = action else action()' in screen,
    'learner mark input width is consistent': 'Modifier.width(112.dp)' in screen,
    'release identity advances without changing application ID': 'applicationId = "com.henrylumis.rvhedu"' in gradle and 'versionCode = 160' in gradle and 'versionName = "1.7.4"' in gradle,
    'release artifact matches V188': 'RVHEdu-1.7.4-release.apk' in workflow and 'v1.7.4-build160' in workflow,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'V188 BULK MARK LAYOUT CONTRACTS PASSED: {len(checks)}/{len(checks)}')
