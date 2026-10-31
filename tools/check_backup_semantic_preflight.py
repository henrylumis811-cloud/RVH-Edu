from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text()
assert "private fun validateDomainRows(tableObject: JSONObject, backupSchemaVersion: Int)" in source
assert source.index("validateDomainRows(tableObject, backupSchemaVersion)") < source.index("val helper = RvhEduDatabase(context)", source.index("fun restoreJson"))
for required in [
    "Backup contains a blank or duplicate learner ID",
    "Backup contains an unsupported learner status",
    "Backup contains an enrolment for a missing learner",
    "Backup contains an invalid academic year",
    "Backup contains an assessment for a missing enrolment",
    "Backup contains an unsupported assessment status",
    "Backup contains a mark for a missing assessment",
    "Backup contains an invalid mark value",
    "Backup contains status history for a missing learner",
    "Backup contains a learner status-history entry without a reason",
]:
    assert required in source, required
assert "db.beginTransaction()" in source
assert "db.setTransactionSuccessful()" in source
print("Backup semantic preflight checks passed")

# Every exported row must carry every schema column, including nullable fields;
# otherwise restore could silently replace omitted historical values with defaults.
assert "require(keys == schema.keys)" in source
assert "missing one or more database columns" in source
assert source.index("require(keys == schema.keys)") < source.index("schema.forEach { (column, definition) ->")
