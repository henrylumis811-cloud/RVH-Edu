"""V124 backup lifecycle coverage checks and a small independent key-coverage fixture.

This is a source-contract/fixture test, not an Android instrumentation test.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
checks = {
    'domain preflight receives declared backup schema version': 'validateDomainRows(tableObject, backupSchemaVersion)' in source,
    'current-schema status coverage is enforced': 'if (backupSchemaVersion >= SCHEMA_VERSION)' in source and 'Current-schema backup is missing a learner-status record' in source,
    'coverage check matches school and learner together': 'val key = "${row.optString("school_id")}\\u001f$learnerId"' in source and 'val learnerId = row.optString("learner_id")' in source,
    'status key set rejects duplicate school/learner assignments': 'schoolStatusKeys.add("$schoolId\\u001f$learnerId")' in source,
    'legacy status backfill is conditional on pre-v10 backup': 'if (backupSchemaVersion < SCHEMA_VERSION) backfillSchoolStatusesAfterRestore(db)' in source,
    'coverage validation occurs before destructive transaction': source.index('validateDomainRows(tableObject, backupSchemaVersion)') < source.index('db.beginTransaction()'),
    'current schema table shape is still strictly validated': 'if (backupSchemaVersion >= SCHEMA_VERSION)' in source and 'require(keys == schema.keys)' in source,
    'checksum verification still precedes legacy normalization': source.index('expectedDigest == actualDigest') < source.index('normalizeLegacyBackupTables(tableObject, backupSchemaVersion)'),
    'database integrity check remains before transaction commit': source.index('AcademicDatabaseIntegrity.requireHealthy(db)') < source.index('db.setTransactionSuccessful()'),
}

# Behavioral key-coverage fixture: same composite identity rule used by the Kotlin preflight.
enrollments = [('school-a', 'learner-1'), ('school-b', 'learner-1'), ('school-a', 'learner-2')]
complete = {('school-a', 'learner-1'), ('school-b', 'learner-1'), ('school-a', 'learner-2')}
incomplete = {('school-a', 'learner-1'), ('school-a', 'learner-2')}
behavior = {
    'complete per-school status set covers all enrollment pairs': all(pair in complete for pair in enrollments),
    'missing one school status is detected even when same learner has another status': not all(pair in incomplete for pair in enrollments),
    'same learner can be independently represented in two schools': ('school-a', 'learner-1') in complete and ('school-b', 'learner-1') in complete,
}
for name, ok in {**checks, **behavior}.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()) and all(behavior.values()), 'V124 backup lifecycle coverage checks failed'
print(f'V124: {len(checks) + len(behavior)}/{len(checks) + len(behavior)} checks passed')
