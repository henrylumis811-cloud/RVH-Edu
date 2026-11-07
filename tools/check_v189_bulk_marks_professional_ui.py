#!/usr/bin/env python3
"""Source-level regression contracts for V189 professional bulk-mark UX."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/BulkMarkEntryScreen.kt').read_text()
gradle = (root / 'app/build.gradle.kts').read_text()
workflow = (root / '.github/workflows/android-build.yml').read_text()
checks = {
    'selector remains a Material 3 modal bottom sheet': 'ModalBottomSheet(' in screen and 'rememberModalBottomSheetState(skipPartiallyExpanded = true)' in screen,
    'sheet uses the RVH app surface and brand color scheme': 'containerColor = MaterialTheme.colorScheme.surface' in screen and 'MaterialTheme.colorScheme.primaryContainer' in screen and 'MaterialTheme.colorScheme.secondaryContainer' in screen,
    'sheet has a clear title, explanation and selected-session preview': 'Marking session' in screen and 'Choose what you are entering marks for.' in screen and 'CURRENT SELECTION' in screen,
    'term and subject choices remain responsive wrapping chips': screen.count('FlowRow(') >= 2 and 'Term.entries.forEach' in screen and 'subjects.forEach' in screen,
    'assessment periods are explicit full-width accessible selection rows': 'TermSection.entries.forEach' in screen and 'heightIn(min = 54.dp)' in screen and 'RadioButton(selected = selected' in screen,
    'selection action stays visible below internally scrollable content': 'verticalScroll(pickerScroll)' in screen and 'Text("Apply selection")' in screen and 'HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)' in screen,
    'cancel and apply actions have comfortable touch heights': 'heightIn(min = 52.dp)' in screen,
    'selection is staged and applied together through unsaved-change guard': 'requestAction {' in screen and 'onTermChange(pickerTerm)' in screen and 'onSectionChange(pickerSection)' in screen and 'selectedSubject = pickerSubject' in screen,
    'persistent signing identity and application ID are retained': 'applicationId = "com.henrylumis.rvhedu"' in gradle and 'rvhStable' in gradle,
    'version and CI artifact are advanced consistently': 'versionCode = 160' in gradle and 'versionName = "1.7.4"' in gradle and 'RVHEdu-1.7.4-release.apk' in workflow and 'v1.7.4-build160' in workflow,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'V189 PROFESSIONAL BULK-MARK UI CONTRACTS PASSED: {len(checks)}/{len(checks)}')
