from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
assert 'val snapshot = persisted.policySnapshotJson?.let { json ->' in source
assert 'Marks were not saved; review the policy record before continuing.' in source
assert 'private fun parsePolicy(assessment: AcademicAssessment): AssessmentPolicySnapshot?' in source
assert 'Historical results cannot be calculated safely.' in source
assert 'runCatching { AssessmentPolicySnapshot.parse(it) }.getOrNull()' not in source
print('PASS: malformed non-null assessment policy snapshots fail closed instead of falling back to active rules')
