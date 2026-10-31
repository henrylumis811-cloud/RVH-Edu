from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()
assert 'val keys = row.keys().asSequence().toSet()' in source
assert 'require(keys.all { it in schema })' in source
assert 'require(keys == schema.keys)' in source
assert source.index('validateBackupRows(db, tableObject, backupSchemaVersion)') < source.index('db.beginTransaction()')
assert 'missing one or more database columns' in source
print("Backup row-shape preflight checks passed")
