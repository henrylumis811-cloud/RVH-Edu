from pathlib import Path
root = Path(__file__).resolve().parents[1]
export = (root / 'app/src/main/java/com/henrylumis/rvhgrader/export/AcademicExportManager.kt').read_text()
activity = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
assert 'policySnapshotJsons: Collection<String?>? = null' in export
snapshot = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
assert 'AssessmentPolicySnapshot.classCompatibilityError(policySnapshotJsons)' in export
assert 'pinned.size != parsed.size' in snapshot, 'Mixed pinned and legacy policies must be rejected'
assert 'pinned.map(::signature).distinct().size > 1' in snapshot, 'Different policies must not be combined'
assert 'Class results were stopped to protect accuracy' in snapshot
assert 'policySnapshotJsons = results.map' in activity
assert 'policySnapshotJson = roster.firstNotNullOfOrNull' not in activity
print('PASS: combined class exports reject mixed legacy/pinned policies and differing saved policies; call sites provide per-result snapshots')
