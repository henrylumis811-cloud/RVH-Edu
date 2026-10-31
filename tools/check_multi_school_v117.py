from pathlib import Path
root = Path(__file__).resolve().parents[1]
db = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text()
repo = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt').read_text()
svc = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
backup = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
integrity = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseIntegrity.kt').read_text()
checks = {
 'database version bumped to 10': 'private const val DB_VERSION = 10' in db,
 'school-local status table has composite primary key': 'PRIMARY KEY(school_id, learner_id)' in db,
 'migration backfills each enrollment school': 'SELECT e.school_id, l.id, l.status' in db,
 'migration adds school scope to lifecycle history': 'ALTER TABLE learner_status_history ADD COLUMN school_id' in db,
 'school-scoped learner lookup reads local status': 'COALESCE(sls.status, l.status) AS status' in repo,
 'roster status joins on active enrollment school': 'sls.school_id = e.school_id' in repo,
 'upsert avoids overwriting shared identity status': 'val updatedValues = ContentValues(values).apply { remove("status") }' in repo,
 'status history event records school': 'put("school_id", schoolId)' in svc,
 'archive reason query is school-scoped': "h.school_id = ?" in svc,
 'backup includes local status table': '"school_learner_status")' in backup,
 'backup schema bumped': 'private const val SCHEMA_VERSION = 10' in backup,
 'old backup restore backfills school statuses': 'backfillSchoolStatusesAfterRestore(db)' in backup,
 'integrity audit checks local statuses': 'school_learner_status' in integrity,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V117 checks failed'
print(f'V117: {len(checks)}/{len(checks)} checks passed')
