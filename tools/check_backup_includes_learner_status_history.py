from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
backup = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()
integrity = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseIntegrity.kt").read_text()
database = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt").read_text()
assert 'private const val SCHEMA_VERSION = 10' in backup
assert '"learner_status_history")' in backup
assert '"legacy_identity_review", "learner_status_history", "school_learner_status"' in backup
assert 'schemaVersion < 6 && !tableObject.has("learner_status_history")' in backup
assert 'tableObject.put("learner_status_history", JSONArray())' in backup
assert 'learner_status_history' in integrity and 'school_learner_status' in integrity
assert 'CREATE TABLE IF NOT EXISTS learner_status_history' in database
print("Backup status-history coverage and v4 compatibility checks passed")
