from pathlib import Path
root = Path(__file__).resolve().parents[1]
backup = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
checks = {
    'backup schema version matches database schema v10': 'private const val SCHEMA_VERSION = 10' in backup,
    'preview accepts schema versions through current v10': 'require(schema in 4..SCHEMA_VERSION)' in backup,
    'restore accepts schema versions through current v10': 'require(backupSchemaVersion in 4..SCHEMA_VERSION)' in backup,
    'schema 9 and older backups receive missing lifecycle table': 'normalizeLegacyBackupTables(tableObject, schema)' in backup and 'if (schemaVersion < 10 && !tableObject.has("school_learner_status"))' in backup,
    'legacy table synthesis is limited to schemas that predate those tables': 'if (schemaVersion < 7) {\n            setOf(\"teacher\", \"teacher_school_membership\", \"teaching_assignment\")' in backup and 'if (schemaVersion < 9 && !tableObject.has(\"school_admission_number\"))' in backup,
    'checksum checked before compatibility normalization during restore': backup.index('expectedDigest == actualDigest') < backup.index('normalizeLegacyBackupTables(tableObject, backupSchemaVersion)'),
    'domain validation occurs before destructive transaction': backup.index('validateDomainRows(tableObject, backupSchemaVersion)') < backup.index('db.beginTransaction()'),
    'installed schema is validated before destructive transaction': backup.index('validateBackupRows(db, tableObject, backupSchemaVersion)') < backup.index('db.beginTransaction()'),
    'restore deletes and restores status table within transaction': 'db.beginTransaction()' in backup and 'db.delete(it, null, null)' in backup and 'restoreTable(db, table, tableObject.getJSONArray(table))' in backup,
    'post-restore status backfill retained': 'backfillSchoolStatusesAfterRestore(db)' in backup,
    'post-restore database integrity check retained': 'AcademicDatabaseIntegrity.requireHealthy(db)' in backup,
    'export checksum generated over canonical table payload': 'put("contentSha256", sha256(canonicalJson(tableObject)))' in backup,
    'bounded backup size remains enforced': 'MAX_BACKUP_BYTES = 64L * 1024L * 1024L' in backup,
    'legacy lifecycle history attribution retained': 'row.put("school_id", "legacy-school")' in backup,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V120 checks failed'
print(f'V120: {len(checks)}/{len(checks)} checks passed')
