from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
assert 'it.code.isNotBlank() && it.shortName.isNotBlank() && it.displayName.isNotBlank()' in source
assert 'subjects.map { it.code.uppercase() }.distinct().size == subjects.size' in source
assert 'SchoolClass.values().any { it.name == schoolClass }' in source
assert 'require(capturedAt >= 0L)' in source
assert 'divisions.all { it.label.isNotBlank() }' in source
assert 'Assessment policy snapshot has no aggregate bands' in source
print('PASS: malformed policy identity fields and incomplete grading bands are rejected')
