"""Regression checks for whole-restore rollback, including failure after partial replacement."""
from pathlib import Path
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
restore = source[source.index('fun restoreJson('):source.index('private fun canonicalJson(')]
assert restore.index('validateDomainRows(tableObject, backupSchemaVersion)') < restore.index('db.beginTransaction()')
assert restore.index('validateBackupRows(db, tableObject, backupSchemaVersion)') < restore.index('db.beginTransaction()')
assert restore.index('AcademicDatabaseIntegrity.requireHealthy(db)') < restore.index('db.setTransactionSuccessful()')
assert 'finally {\n                db.endTransaction()' in restore

# SQLite integration-style fixture: restore deletes multiple tables and inserts some new rows,
# then fails on a foreign-key violation. The entire transaction must preserve the old dataset.
db = sqlite3.connect(':memory:')
db.execute('PRAGMA foreign_keys=ON')
db.executescript('''
CREATE TABLE school(id TEXT PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE learner(id TEXT PRIMARY KEY, school_id TEXT NOT NULL REFERENCES school(id), name TEXT NOT NULL);
CREATE TABLE assessment(id TEXT PRIMARY KEY, learner_id TEXT NOT NULL REFERENCES learner(id));
CREATE TABLE mark(id TEXT PRIMARY KEY, assessment_id TEXT NOT NULL REFERENCES assessment(id), value INTEGER NOT NULL);
INSERT INTO school VALUES ('old-school','Existing School');
INSERT INTO learner VALUES ('old-learner','old-school','Existing Learner');
INSERT INTO assessment VALUES ('old-assessment','old-learner');
INSERT INTO mark VALUES ('old-mark','old-assessment',87);
''')
tables = ['school', 'learner', 'assessment', 'mark']
def snapshot():
    return {t: db.execute(f'SELECT * FROM {t} ORDER BY 1').fetchall() for t in tables}
before = snapshot()
try:
    db.execute('BEGIN')
    for table in ['mark', 'assessment', 'learner', 'school']:
        db.execute(f'DELETE FROM {table}')
    db.execute("INSERT INTO school VALUES ('new-school','Replacement School')")
    db.execute("INSERT INTO learner VALUES ('new-learner','new-school','Replacement Learner')")
    db.execute("INSERT INTO assessment VALUES ('new-assessment','new-learner')")
    # Simulate a late malformed row after replacement rows were already written.
    db.execute("INSERT INTO mark VALUES ('bad-mark','missing-assessment',55)")
    db.commit()
    raise AssertionError('Foreign-key violation was expected')
except sqlite3.IntegrityError:
    db.rollback()
assert snapshot() == before, 'Failed restore changed at least one pre-existing table'
assert db.execute('PRAGMA foreign_key_check').fetchall() == []
db.close()
print('PASS - restore preflight precedes transaction')
print('PASS - integrity audit precedes transaction success')
print('PASS - late failure rolls back all replaced tables to exact prior rows')
print('PASS - database has no foreign-key violations after rollback')
