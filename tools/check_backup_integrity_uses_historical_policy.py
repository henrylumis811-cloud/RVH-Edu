from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseIntegrity.kt").read_text()
assert "AssessmentPolicySnapshot.parse(snapshotJson)" in source
assert "policy.schoolClass != enrollmentClass" in source
assert "policy.subjects.map { it.code.trim().uppercase() }.toSet()" in source
assert "a.policy_snapshot_json" in source
assert "Legacy assessments without a snapshot cannot be" in source
assert "SubjectCatalog.forClass(schoolClass).size" not in source
assert "final assessment(s) do not match their saved subject policy" in source
assert "final assessment(s) have an invalid or class-mismatched saved policy" in source
print("Historical-policy backup integrity checks passed")
