from pathlib import Path
root = Path(__file__).resolve().parents[1]
backup = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
checks = {
    'backup schema version advances for the lifecycle table': 'private const val SCHEMA_VERSION = 10' in backup,
    'pre-v10 backups without lifecycle table are accepted': 'if (schemaVersion < 10 && !tableObject.has("school_learner_status"))' in backup,
    'school IDs are collected before enrollment validation': backup.index('val schoolIds = mutableSetOf<String>()') < backup.index('val enrollments = tableObject.getJSONArray("enrollment")'),
    'enrollment school reference validated': 'Backup contains an enrolment referencing a missing school' in backup,
    'school-local lifecycle rows are validated': 'val schoolStatuses = tableObject.getJSONArray("school_learner_status")' in backup,
    'status row school and learner references validated': 'school-local learner status referencing a missing school or learner' in backup,
    'status values restricted to ACTIVE and ARCHIVED': 'row.optString("status") in setOf("ACTIVE", "ARCHIVED")' in backup,
    'status timestamps validated': 'val createdAt = row.optLong("created_at", 0L)' in backup and 'val updatedAt = row.optLong("updated_at", 0L)' in backup and 'require(createdAt > 0L && updatedAt > 0L)' in backup,
    'duplicate school/learner status keys rejected': 'schoolStatusKeys.add("$schoolId\\u001f$learnerId")' in backup or 'schoolStatusKeys.add("$schoolId\u001f$learnerId")' in backup,
    'history school references validated': 'learner status history referencing a missing school' in backup,
    'legacy history missing school gets stable legacy attribution': 'row.put("school_id", "legacy-school")' in backup,
    'legacy attribution is applied before domain validation': backup.index('normalizeLegacyBackupTables(tableObject, backupSchemaVersion)') < backup.index('validateDomainRows(tableObject, backupSchemaVersion)'),
    'backup restore remains transactional': 'db.beginTransaction()' in backup and 'db.setTransactionSuccessful()' in backup,
    'legacy status backfill still runs after restore': 'backfillSchoolStatusesAfterRestore(db)' in backup,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V119 checks failed'
print(f'V119: {len(checks)}/{len(checks)} checks passed')
