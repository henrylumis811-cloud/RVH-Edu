from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()
assert "private const val MAX_BACKUP_BYTES = 64L * 1024L * 1024L" in source
assert "while (true)" in source and "total <= MAX_BACKUP_BYTES" in source
assert 'restoreJson(context, readBackupBytes(context, uri).toString(Charsets.UTF_8))' in source
assert "val bytes = buildJson(context).toByteArray(Charsets.UTF_8)" in source
assert source.index("val bytes = buildJson(context).toByteArray(Charsets.UTF_8)") < source.index("context.contentResolver.openOutputStream(uri)")
assert 'The database has not been changed.' in source
assert 'require(tables.all { tableObject.has(it) })' in source
print("Backup size limits, preflight generation, and required-table checks passed")
