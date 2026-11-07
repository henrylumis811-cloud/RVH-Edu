"""Guard the production-to-pure-JVM checksum canonicalization path and its runtime test."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
manager = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()
helper = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/BackupChecksumPrimitives.kt").read_text()
test = (ROOT / "tools/pure-tests/BackupChecksumPrimitivesRegression.kt").read_text()
assert "BackupChecksumPrimitives.canonicalJson(toChecksumTree(value))" in manager
assert "is JSONObject" in manager and "is JSONArray" in manager
assert "JSONObject.NULL -> null" in manager
assert "value.keys.map { it as String }.sorted()" in helper
assert "quoteJsonString" in helper and "is Boolean -> value.toString()" in helper
assert "is Number -> canonicalNumber(value)" in helper
assert "listOf(1, 2)" in test and "listOf(2, 1)" in test
assert "reorderedTree" in test and "x\\ny" in test
assert "canonicalJson(true) != BackupChecksumPrimitives.canonicalJson(1)" in test
assert "canonicalJson(null) == \"null\"" in test
print("Backup canonical JSON runtime coverage guards passed (12 assertions).")
