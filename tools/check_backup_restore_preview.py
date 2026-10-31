"""Regression guards for the read-only backup summary shown before restore confirmation."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
manager = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()
screen = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/BackupScreen.kt").read_text()
preview_start = manager.index("fun previewFrom(context: Context, uri: Uri): BackupPreview")
preview_end = manager.index("fun restoreFrom(context: Context, uri: Uri)", preview_start)
preview = manager[preview_start:preview_end]
assert "readBackupBytes(context, uri)" in preview
assert "backupVersion" in preview and "schemaVersion" in preview
assert "expected == sha256(canonicalJson(tableObject))" in preview
assert "BackupPreview(version, schema, exportedAt" in preview
assert "RvhEduDatabase(context)" not in preview
assert "beginTransaction" not in preview
assert "previewFrom(context, uri)" in screen
assert "Learners:" in screen and "Enrolments:" in screen
assert "Assessments:" in screen and "Marks:" in screen
assert "Integrity checksum: verified" in screen
assert "Full academic-data and database-schema validation runs before restoration" in screen
# The final restore path must still perform full domain and installed-schema validation.
restore_start = manager.index("fun restoreJson(context: Context, json: String)")
restore = manager[restore_start:]
assert "validateDomainRows(tableObject, backupSchemaVersion)" in restore
assert "validateBackupRows(db, tableObject, backupSchemaVersion)" in restore
print("Backup restore preview regression checks passed (12 assertions).")
