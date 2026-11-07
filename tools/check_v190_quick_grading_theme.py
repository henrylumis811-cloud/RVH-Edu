#!/usr/bin/env python3
"""Source contracts for RVH Edu Quick Grading visual consistency."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/Dashboard.kt').read_text()
gradle = (root / 'app/build.gradle.kts').read_text()
workflow = (root / '.github/workflows/android-build.yml').read_text()
checks = {
    'quick grading uses the RVH EDU app-bar identity': 'Text("RVH EDU"' in screen and 'Text("QUICK GRADING"' in screen and 'Icons.Filled.School' in screen,
    'screen uses shared Material theme colours and shapes': 'MaterialTheme.colorScheme.background' in screen and 'MaterialTheme.colorScheme.surface' in screen and 'MaterialTheme.shapes.large' in screen,
    'class, term and assessment period are grouped in a session card': 'SessionSelectionCard(' in screen and 'Marking session' in screen and 'Assessment period' in screen,
    'quick entry has clear hierarchy and instructional text': 'Quick mark entry' in screen and 'Enter one learner\'s marks.' in screen and 'Learner name' in screen and 'Subject marks' in screen,
    'primary save action has accessible minimum height': 'heightIn(min = 52.dp)' in screen and 'Save quick-entry result' in screen,
    'standings cards use RVH theme surfaces and summary typography': 'containerColor = MaterialTheme.colorScheme.surface' in screen and 'SummaryValue("AGGREGATE"' in screen,
    'missing marks are not rendered as zero': 'score?.mark?.toString() ?: "—"' in screen and 'score?.mark ?: 0' not in screen,
    'existing save and export callbacks are retained': 'onCommit = onCommit' in screen and 'onExportClassList(format)' in screen and 'onClassChange' in screen and 'onSectionChange' in screen,
    'persistent app identity and signing remain unchanged': 'applicationId = "com.henrylumis.rvhedu"' in gradle and 'rvhStable' in gradle,
    'version and release artifact advance for install-over update': 'versionCode = 160' in gradle and 'versionName = "1.7.4"' in gradle and 'RVHEdu-1.7.4-release.apk' in workflow and 'v1.7.4-build160' in workflow,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'V190 QUICK GRADING UI CONTRACTS PASSED: {len(checks)}/{len(checks)}')
