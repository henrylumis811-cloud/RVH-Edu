from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()
for required in [
    "val enrollmentKeys = mutableSetOf<String>()",
    "duplicate enrolments for the same learner, year and class",
    "val assessmentKeys = mutableSetOf<String>()",
    "duplicate assessments for the same enrolment, term and section",
    "val markKeys = mutableSetOf<String>()",
    "duplicate marks for the same assessment and subject",
    "val identityReviewKeys = mutableSetOf<String>()",
    "identity review referencing a missing learner",
    "identity review linking a learner to themselves",
    "duplicate identity-review entries",
    "fromStatus != toStatus",
]:
    assert required in source, required
assert source.index("validateDomainRows(tableObject, backupSchemaVersion)") < source.index("db.beginTransaction()")
print("Backup unique relationship and identity-review checks passed")
