"""Source regression checks for the backup restore transaction boundary."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()

# The checksum and all semantic/schema preflight checks must happen before the destructive transaction.
checksum = source.index("expectedDigest == actualDigest")
domain = source.index("validateDomainRows(tableObject, backupSchemaVersion)")
helper = source.index("val helper = RvhEduDatabase(context)", source.index("fun restoreJson"))
row_validation = source.index("validateBackupRows(db, tableObject, backupSchemaVersion)")
begin = source.index("db.beginTransaction()", row_validation)
assert checksum < domain < helper < row_validation < begin

# A failed insert or final integrity check must not mark the transaction successful.
transaction = source[begin:source.index("} finally {\n            helper.close()", begin)]
assert transaction.index("AcademicDatabaseIntegrity.requireHealthy(db)") < transaction.index("db.setTransactionSuccessful()")
assert "try {" in transaction and "finally {" in transaction
assert transaction.index("db.setTransactionSuccessful()") < transaction.index("db.endTransaction()")

# Compatibility and defensive boundaries remain explicit.
assert "backupVersion == 1 || backupVersion == BACKUP_VERSION" in source
assert 'if (backupVersion >= 2)' in source
assert "validateBackupRows(db, tableObject, backupSchemaVersion)" in source
assert "Backup integrity check failed" in source
assert "The existing database has not been changed" in source

print("Backup restore transaction safety checks passed (11 assertions).")
