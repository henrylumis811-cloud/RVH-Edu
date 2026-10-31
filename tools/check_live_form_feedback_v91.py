#!/usr/bin/env python3
"""Focused source checks for live learner roster updates and form feedback."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
main = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
learners = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/LearnerManagementScreen.kt').read_text()
checks = {
    'learner roster reads Compose revision': 'roster = run { academicDataRevision; academicService.roster(academicYear, selectedClass) }' in main,
    'successful add increments revision': 'academicService.createAndEnrollLearner(' in main and 'academicDataRevision++' in main[main.index('onAddLearner ='):main.index('onUpdateLearner =')],
    'successful edit increments revision': 'academicDataRevision++' in main[main.index('onUpdateLearner ='):main.index('onFindDuplicates =')],
    'add rejects blank learner name with feedback': 'Full name is required. Enter the learner\'s name before saving.' in learners,
    'edit rejects blank learner name with feedback': learners.count('Full name is required. Enter the learner\'s name before saving.') == 2,
    'names are trimmed and whitespace normalized': learners.count('name.trim().replace(Regex("\\\\s+"), " ")') >= 2,
    'admission number is trimmed and optional blanks normalized': learners.count('admissionNumber.trim().ifBlank { null }') >= 4,
    'name field explains required purpose and feedback': learners.count('Required — the learner\'s name appears on reports and certificates.') == 2,
    'year field communicates accepted range': 'Enter a year from 2000 to 2100' in learners,
    'year field marks invalid completed value': 'isError = yearDraft.length == 4 && (yearDraft.toIntOrNull() !in 2000..2100)' in learners,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)}/{len(checks)} focused checks passed')
