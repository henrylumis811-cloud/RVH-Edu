"""V129 behavioral SQLite fixture for backup/restore row preservation.

This is a Python SQLite model plus source-contract checks, not Android instrumentation.
It exercises linked records, restore ordering, transaction rollback, and school-local status.
"""
from pathlib import Path
import json
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
restore_source = source[source.index('fun restoreJson('):source.index('private fun canonicalJson(')]
checks = {
    'restore validates domain rows before opening destructive transaction': restore_source.index('validateDomainRows(tableObject, backupSchemaVersion)') < restore_source.index('db.beginTransaction()'),
    'restore validates database column shapes before deleting records': restore_source.index('validateBackupRows(db, tableObject, backupSchemaVersion)') < restore_source.index('db.beginTransaction()'),
    'restore marks transaction successful only after integrity audit': restore_source.index('AcademicDatabaseIntegrity.requireHealthy(db)') < restore_source.index('db.setTransactionSuccessful()'),
    'restore ends transaction in finally for rollback on failure': 'finally {\n                db.endTransaction()' in restore_source,
    'restoration includes all current domain and audit tables': all(f'"{t}"' in source for t in ['school','teacher','teacher_school_membership','teaching_assignment','learner','school_admission_number','enrollment','assessment','mark','legacy_identity_review','learner_status_history','school_learner_status']),
}

# Compact relational fixture mirrors the important current-schema relationships.
db = sqlite3.connect(':memory:')
db.execute('PRAGMA foreign_keys=ON')
db.executescript('''
CREATE TABLE school(id TEXT PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE teacher(id TEXT PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE teacher_school_membership(id TEXT PRIMARY KEY, teacher_id TEXT NOT NULL REFERENCES teacher(id), school_id TEXT NOT NULL REFERENCES school(id));
CREATE TABLE teaching_assignment(id TEXT PRIMARY KEY, teacher_id TEXT NOT NULL REFERENCES teacher(id), school_id TEXT NOT NULL REFERENCES school(id));
CREATE TABLE learner(id TEXT PRIMARY KEY, full_name TEXT NOT NULL, admission_number TEXT, status TEXT NOT NULL);
CREATE TABLE school_admission_number(id TEXT PRIMARY KEY, school_id TEXT NOT NULL REFERENCES school(id), learner_id TEXT NOT NULL REFERENCES learner(id), admission_number TEXT NOT NULL, normalized_admission_number TEXT NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL);
CREATE TABLE enrollment(id TEXT PRIMARY KEY, school_id TEXT NOT NULL REFERENCES school(id), learner_id TEXT NOT NULL REFERENCES learner(id), academic_year INTEGER NOT NULL, school_class TEXT NOT NULL);
CREATE TABLE assessment(id TEXT PRIMARY KEY, enrollment_id TEXT NOT NULL REFERENCES enrollment(id), term TEXT NOT NULL, section TEXT NOT NULL, status TEXT NOT NULL);
CREATE TABLE mark(id TEXT PRIMARY KEY, assessment_id TEXT NOT NULL REFERENCES assessment(id), subject_code TEXT NOT NULL, mark INTEGER NOT NULL, max_mark INTEGER NOT NULL);
CREATE TABLE legacy_identity_review(id TEXT PRIMARY KEY, learner_id TEXT NOT NULL REFERENCES learner(id), related_learner_id TEXT NOT NULL REFERENCES learner(id), normalized_name TEXT NOT NULL, reason TEXT NOT NULL, resolved INTEGER NOT NULL, created_at INTEGER NOT NULL);
CREATE TABLE learner_status_history(id TEXT PRIMARY KEY, school_id TEXT NOT NULL REFERENCES school(id), learner_id TEXT NOT NULL REFERENCES learner(id), from_status TEXT NOT NULL, to_status TEXT NOT NULL, reason TEXT NOT NULL, changed_at INTEGER NOT NULL);
CREATE TABLE school_learner_status(school_id TEXT NOT NULL REFERENCES school(id), learner_id TEXT NOT NULL REFERENCES learner(id), status TEXT NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, PRIMARY KEY(school_id,learner_id));
''')
# Parents before children; deliberately shared learner has independent school status.
db.executemany('INSERT INTO school VALUES (?,?)', [('s-a','School A'),('s-b','School B')])
db.execute('INSERT INTO teacher VALUES (?,?)', ('t-1','Teacher One'))
db.executemany('INSERT INTO teacher_school_membership VALUES (?,?,?)', [('mem-a','t-1','s-a'),('mem-b','t-1','s-b')])
db.executemany('INSERT INTO teaching_assignment VALUES (?,?,?)', [('assign-a','t-1','s-a'),('assign-b','t-1','s-b')])
db.executemany('INSERT INTO learner VALUES (?,?,?,?)', [('l-shared','Shared Learner',None,'ARCHIVED'),('l-unenrolled','Unenrolled Learner',None,'ACTIVE'),('l-related','Related Learner',None,'ACTIVE')])
db.executemany('INSERT INTO school_admission_number VALUES (?,?,?,?,?,?,?)', [('adm-a','s-a','l-shared','A-10','A-10',100,110),('adm-b','s-b','l-shared','B-20','B-20',101,111)])
db.executemany('INSERT INTO enrollment VALUES (?,?,?,?,?)', [('e-a','s-a','l-shared',2026,'P5'),('e-b','s-b','l-shared',2026,'P5')])
db.executemany('INSERT INTO assessment VALUES (?,?,?,?,?)', [('as-a','e-a','TERM1','MID','FINAL'),('as-b','e-b','TERM1','MID','DRAFT')])
db.executemany('INSERT INTO mark VALUES (?,?,?,?,?)', [('m-a','as-a','MATH',78,100),('m-b','as-b','ENG',64,100)])
db.execute('INSERT INTO legacy_identity_review VALUES (?,?,?,?,?,?,?)', ('review-1','l-shared','l-related','shared learner','NAME_SIMILARITY',0,120))
db.execute('INSERT INTO learner_status_history VALUES (?,?,?,?,?,?,?)', ('hist-1','s-a','l-shared','ACTIVE','ARCHIVED','Moved out',130))
db.executemany('INSERT INTO school_learner_status VALUES (?,?,?,?,?)', [('s-a','l-shared','ARCHIVED',100,130),('s-b','l-shared','ACTIVE',100,125),('s-a','l-unenrolled','ACTIVE',115,115),('s-a','l-related','ACTIVE',100,100)])

tables = ['school','teacher','teacher_school_membership','teaching_assignment','learner','school_admission_number','enrollment','assessment','mark','legacy_identity_review','learner_status_history','school_learner_status']
def snapshot():
    result = {}
    for table in tables:
        cols = [r[1] for r in db.execute(f'PRAGMA table_info({table})')]
        order = ','.join(cols)
        result[table] = {'columns': cols, 'rows': [list(r) for r in db.execute(f'SELECT {order} FROM {table} ORDER BY {order}')]} 
    return result

before = snapshot()
# Exercise JSON serialization/deserialization like the portable backup envelope.
payload = json.loads(json.dumps(before, sort_keys=True))
# Delete child-first and restore parent-first, as the Android manager intends.
for table in reversed(tables):
    db.execute(f'DELETE FROM {table}')
for table in tables:
    cols = payload[table]['columns']
    marks = ','.join('?' for _ in cols)
    names = ','.join(cols)
    for row in payload[table]['rows']:
        db.execute(f'INSERT INTO {table} ({names}) VALUES ({marks})', row)
checks['round-trip preserves every row and primary identifier across all 12 tables'] = snapshot() == before
checks['round-trip retains both school-specific admission numbers'] = db.execute('SELECT id,school_id,admission_number FROM school_admission_number ORDER BY id').fetchall() == [('adm-a','s-a','A-10'),('adm-b','s-b','B-20')]
checks['round-trip retains marks linked to original assessment IDs'] = db.execute('SELECT id,assessment_id,mark FROM mark ORDER BY id').fetchall() == [('m-a','as-a',78),('m-b','as-b',64)]
checks['round-trip retains identity review and audit history IDs'] = db.execute('SELECT id FROM legacy_identity_review').fetchall() == [('review-1',)] and db.execute('SELECT id FROM learner_status_history').fetchall() == [('hist-1',)]
checks['school-specific archived/active states remain independent'] = db.execute("SELECT status FROM school_learner_status WHERE school_id='s-a' AND learner_id='l-shared'").fetchone() == ('ARCHIVED',) and db.execute("SELECT status FROM school_learner_status WHERE school_id='s-b' AND learner_id='l-shared'").fetchone() == ('ACTIVE',)
checks['learner-before-enrollment status survives round-trip'] = db.execute("SELECT status FROM school_learner_status WHERE school_id='s-a' AND learner_id='l-unenrolled'").fetchone() == ('ACTIVE',)
checks['foreign-key audit is clean after round-trip'] = db.execute('PRAGMA foreign_key_check').fetchall() == []

# Simulate an insert failure after deletion and prove transaction rollback restores old data.
pre_failure = snapshot()
db.commit()
try:
    db.execute('BEGIN')
    db.execute('DELETE FROM mark')
    db.execute("INSERT INTO mark VALUES ('broken','missing-assessment','MATH',10,100)")
    db.commit()
except sqlite3.IntegrityError:
    db.rollback()
checks['failed mid-restore write rolls back to exact prior rows'] = snapshot() == pre_failure
checks['fixture has no dangling foreign keys after rollback'] = db.execute('PRAGMA foreign_key_check').fetchall() == []
db.close()

for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V129 backup round-trip checks failed'
print(f'V129: {len(checks)}/{len(checks)} checks passed')
