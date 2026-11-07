from pathlib import Path

root = Path(__file__).resolve().parents[1]
export = (root / 'app/src/main/java/com/henrylumis/rvhgrader/export/AcademicExportManager.kt').read_text()
activity = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
assert 'policySnapshotJsons: Collection<String?>? = null' in export
assert 'AssessmentPolicySnapshot.parse(it)' in export
assert 'savedPolicy?.subjects?.map { it.code } ?: subjectsFor(schoolClass)' in export
snapshot = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
assert 'AssessmentPolicySnapshot.classCompatibilityError(policySnapshotJsons)' in export
assert 'pinned.map(::signature).distinct().size > 1' in snapshot
assert 'pinned.size != parsed.size' in snapshot
assert 'savedPolicy?.subjects?.associate { it.code to it.displayName }' in export
assert 'expectedSubjects, labels' in export
for fmt in ('ExportFormat.PDF', 'ExportFormat.EXCEL', 'ExportFormat.CSV'):
    assert fmt in activity
assert activity.count('policySnapshotJsons = results.map') >= 4
print('PASS: class exports can use saved assessment subject order/names across PDF, Excel, and CSV; call sites pass the assessment snapshot.')
