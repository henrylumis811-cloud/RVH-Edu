"""V123: ensure compatibility normalization only fills tables absent from old schemas."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
checks = {
    'teacher tables are synthesized only for pre-v7 backups': 'if (schemaVersion < 7) {\n            setOf("teacher", "teacher_school_membership", "teaching_assignment")' in source,
    'admission table is synthesized only for pre-v9 backups': 'if (schemaVersion < 9 && !tableObject.has("school_admission_number"))' in source,
    'current schema no longer receives unconditional optional-table synthesis': 'tables.filter { !tableObject.has(it) && it in optionalLegacyTables }' not in source,
    'current schema missing required tables is rejected by table count': 'require(tableObject.length() == tables.size)' in source,
    'current schema missing a table is rejected by required table check': 'require(tableObject.has(table) && tableObject.optJSONArray(table) != null)' in source,
    'legacy status table is still added only before schema v10': 'if (schemaVersion < 10 && !tableObject.has("school_learner_status"))' in source,
    'checksum verification precedes normalization during restore': source.index('expectedDigest == actualDigest') < source.index('normalizeLegacyBackupTables(tableObject, backupSchemaVersion)'),
    'domain validation remains before destructive transaction': source.index('validateDomainRows(tableObject, backupSchemaVersion)') < source.index('db.beginTransaction()'),
    'restore transaction still validates final database before commit': source.index('AcademicDatabaseIntegrity.requireHealthy(db)') < source.index('db.setTransactionSuccessful()'),
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V123 backup schema completeness checks failed'
print(f'V123: {sum(checks.values())}/{len(checks)} checks passed')
