from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()
assert "private const val BACKUP_VERSION = 2" in source
assert "put(\"contentSha256\", sha256(canonicalJson(tableObject)))" in source
assert "private fun canonicalJson(value: Any): String" in source
assert "private fun sha256(value: String): String" in source
assert "private fun canonicalNumber(value: Number): String" in source
assert "BackupChecksumPrimitives.canonicalNumber(value)" in source
assert "BackupChecksumPrimitives.sha256(value)" in source
helper = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/BackupChecksumPrimitives.kt").read_text()
assert "stripTrailingZeros()" in helper
assert "normalized.toString()" in helper
assert "Non-finite or invalid number in backup checksum." in helper
assert "is Boolean -> value.toString()" in helper
assert "is Number -> canonicalNumber(value)" in helper
assert "BackupChecksumPrimitives.canonicalJson(toChecksumTree(value))" in source
assert "backupVersion == 1 || backupVersion == BACKUP_VERSION" in source
assert 'if (backupVersion >= 2)' in source
assert 'expectedDigest == actualDigest' in source
assert source.index('expectedDigest == actualDigest') < source.index('validateDomainRows(tableObject, backupSchemaVersion)')
assert 'existing database has not been changed' in source
print("Backup checksum versioning checks passed")
