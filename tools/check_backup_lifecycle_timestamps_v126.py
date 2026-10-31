"""V126 backup lifecycle timestamp preflight regression checks.

The SQLite fixture checks the invariant independently; source checks ensure the
Android restore preflight enforces it before the restore transaction begins.
This is not an Android instrumentation test.
"""
from pathlib import Path
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
backup_source = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
integrity_source = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseIntegrity.kt').read_text()
checks = {
    'restore preflight reads both lifecycle timestamps': 'val createdAt = row.optLong("created_at", 0L)' in backup_source and 'val updatedAt = row.optLong("updated_at", 0L)' in backup_source,
    'restore preflight rejects updated timestamp before creation': 'require(updatedAt >= createdAt)' in backup_source,
    'timestamp-order validation appears before destructive transaction': backup_source.index('validateDomainRows(tableObject, backupSchemaVersion)') < backup_source.index('db.beginTransaction()') and 'require(updatedAt >= createdAt)' in backup_source,
    'post-restore integrity audit retains timestamp ordering check': 'updated_at < created_at' in integrity_source,
    'restore still performs integrity audit before commit': 'AcademicDatabaseIntegrity.requireHealthy(db)' in backup_source and backup_source.index('AcademicDatabaseIntegrity.requireHealthy(db)') < backup_source.index('db.setTransactionSuccessful()'),
}

db = sqlite3.connect(':memory:')
db.execute('CREATE TABLE school_learner_status (school_id TEXT, learner_id TEXT, status TEXT, created_at INTEGER, updated_at INTEGER, PRIMARY KEY(school_id, learner_id))')
db.execute("INSERT INTO school_learner_status VALUES ('school-a','learner-1','ACTIVE',100,100)")
invalid_query = 'SELECT COUNT(*) FROM school_learner_status WHERE created_at <= 0 OR updated_at <= 0 OR updated_at < created_at'
checks['SQLite accepts equal creation/update timestamps'] = db.execute(invalid_query).fetchone()[0] == 0
db.execute("UPDATE school_learner_status SET updated_at=99 WHERE school_id='school-a'")
checks['SQLite fixture detects update timestamp earlier than creation'] = db.execute(invalid_query).fetchone()[0] == 1
db.execute("UPDATE school_learner_status SET created_at=0, updated_at=100 WHERE school_id='school-a'")
checks['SQLite fixture detects non-positive creation timestamp'] = db.execute(invalid_query).fetchone()[0] == 1
db.close()

for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V126 backup lifecycle timestamp checks failed'
print(f'V126: {len(checks)}/{len(checks)} checks passed')
