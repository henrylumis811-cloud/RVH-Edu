"""V127 schema-v9 -> v10 migration fixture using realistic linked school records.

This is a Python SQLite fixture plus source-contract checks, not Android instrumentation.
"""
from pathlib import Path
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
database_source = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text()
checks = {
    'v10 migration invokes post-backfill verification': 'verifySchoolLearnerStatusMigration(db)' in database_source,
    'migration verifies school-local status coverage for every enrollment': 'LEFT JOIN school_learner_status sls' in database_source and 'sls.school_id = e.school_id AND sls.learner_id = e.learner_id' in database_source,
    'migration verifies un-enrolled learners retain legacy status': "sls.school_id = 'legacy-school' AND sls.learner_id = l.id" in database_source,
    'migration verifies history school references': 'historyWithoutSchool == 0L' in database_source,
    'verification is called after history school_id migration and index creation': database_source.index('ALTER TABLE learner_status_history ADD COLUMN school_id') < database_source.index('verifySchoolLearnerStatusMigration(db)') and database_source.index('verifySchoolLearnerStatusMigration(db)') < database_source.index('private fun createCoreTables'),
}

db = sqlite3.connect(':memory:')
db.execute('PRAGMA foreign_keys=ON')
db.executescript('''
CREATE TABLE school(id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL DEFAULT '', motto TEXT NOT NULL DEFAULT '', created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL);
CREATE TABLE learner(id TEXT PRIMARY KEY NOT NULL, admission_number TEXT UNIQUE, full_name TEXT NOT NULL, age_years INTEGER, gender TEXT, date_of_birth TEXT, photo_uri TEXT, status TEXT NOT NULL DEFAULT 'ACTIVE', created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL);
CREATE TABLE enrollment(id TEXT PRIMARY KEY NOT NULL, school_id TEXT NOT NULL, learner_id TEXT NOT NULL, academic_year INTEGER NOT NULL, school_class TEXT NOT NULL, stream TEXT, enrolled_at INTEGER NOT NULL, FOREIGN KEY(school_id) REFERENCES school(id), FOREIGN KEY(learner_id) REFERENCES learner(id));
CREATE TABLE assessment(id TEXT PRIMARY KEY NOT NULL, enrollment_id TEXT NOT NULL, term TEXT NOT NULL, section TEXT NOT NULL, recorded_at INTEGER NOT NULL, status TEXT NOT NULL DEFAULT 'DRAFT', finalized_at INTEGER, policy_snapshot_json TEXT, UNIQUE(enrollment_id,term,section), FOREIGN KEY(enrollment_id) REFERENCES enrollment(id));
CREATE TABLE mark(id TEXT PRIMARY KEY NOT NULL, assessment_id TEXT NOT NULL, subject_code TEXT NOT NULL, mark INTEGER NOT NULL, max_mark INTEGER NOT NULL DEFAULT 100, aggregate INTEGER, source TEXT NOT NULL DEFAULT 'MANUAL', updated_at INTEGER NOT NULL, UNIQUE(assessment_id,subject_code), FOREIGN KEY(assessment_id) REFERENCES assessment(id));
CREATE TABLE learner_status_history(id TEXT PRIMARY KEY NOT NULL, learner_id TEXT NOT NULL, from_status TEXT NOT NULL, to_status TEXT NOT NULL, reason TEXT NOT NULL, note TEXT, changed_at INTEGER NOT NULL, FOREIGN KEY(learner_id) REFERENCES learner(id));
INSERT INTO school VALUES ('school-a','School A','','100','100');
INSERT INTO school VALUES ('school-b','School B','','100','100');
INSERT INTO school VALUES ('legacy-school','','','100','100');
INSERT INTO learner(id, admission_number, full_name, status, created_at, updated_at) VALUES ('learner-shared','A-1','Shared Learner','ARCHIVED',100,100);
INSERT INTO learner(id, admission_number, full_name, status, created_at, updated_at) VALUES ('learner-unenrolled',NULL,'Unenrolled Learner','ACTIVE',100,100);
INSERT INTO enrollment VALUES ('enrol-a','school-a','learner-shared',2026,'P5',NULL,100);
INSERT INTO enrollment VALUES ('enrol-b','school-b','learner-shared',2026,'P5',NULL,100);
INSERT INTO assessment VALUES ('assessment-a','enrol-a','TERM1','MID',101,'FINAL',102,'{}');
INSERT INTO mark VALUES ('mark-a','assessment-a','MATH',78,100,2,'MANUAL',103);
INSERT INTO learner_status_history VALUES ('history-old','learner-shared','ACTIVE','ARCHIVED','Transferred/archived',NULL,104);
''')

# These statements mirror the v10 migration operations in RvhEduDatabase.
db.executescript('''
CREATE TABLE school_learner_status (
 school_id TEXT NOT NULL, learner_id TEXT NOT NULL,
 status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','ARCHIVED')),
 created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL,
 PRIMARY KEY(school_id, learner_id),
 FOREIGN KEY(school_id) REFERENCES school(id) ON DELETE CASCADE,
 FOREIGN KEY(learner_id) REFERENCES learner(id) ON DELETE CASCADE
);
CREATE INDEX idx_school_learner_status ON school_learner_status(school_id, status);
''')
now = 200
# Preserve the actual legacy global status as the migration's starting point for each school.
db.execute('''INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
SELECT e.school_id, l.id, l.status, ?, ? FROM learner l JOIN enrollment e ON e.learner_id=l.id GROUP BY e.school_id,l.id''', (now, now))
db.execute('''INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
SELECT 'legacy-school', l.id, l.status, ?, ? FROM learner l WHERE NOT EXISTS (SELECT 1 FROM enrollment e WHERE e.learner_id=l.id)''', (now, now))
db.execute("ALTER TABLE learner_status_history ADD COLUMN school_id TEXT NOT NULL DEFAULT 'legacy-school'")
db.execute('CREATE INDEX idx_learner_status_history_school ON learner_status_history(school_id, changed_at DESC)')

checks['migration fixture preserves learner IDs'] = db.execute('SELECT id FROM learner ORDER BY id').fetchall() == [('learner-shared',), ('learner-unenrolled',)]
checks['migration fixture preserves enrollment IDs'] = db.execute('SELECT id FROM enrollment ORDER BY id').fetchall() == [('enrol-a',), ('enrol-b',)]
checks['migration fixture preserves assessment and mark IDs'] = db.execute('SELECT id FROM assessment').fetchall() == [('assessment-a',)] and db.execute('SELECT id FROM mark').fetchall() == [('mark-a',)]
checks['shared learner gets one status per enrolled school'] = db.execute("SELECT COUNT(*) FROM school_learner_status WHERE learner_id='learner-shared'").fetchone()[0] == 2
checks['un-enrolled learner receives legacy-school status'] = db.execute("SELECT status FROM school_learner_status WHERE school_id='legacy-school' AND learner_id='learner-unenrolled'").fetchone() == ('ACTIVE',)
checks['legacy audit history receives legacy-school attribution'] = db.execute("SELECT school_id FROM learner_status_history WHERE id='history-old'").fetchone() == ('legacy-school',)
checks['post-migration status coverage query finds no gaps'] = db.execute('''SELECT COUNT(*) FROM enrollment e LEFT JOIN school_learner_status s ON s.school_id=e.school_id AND s.learner_id=e.learner_id WHERE s.learner_id IS NULL''').fetchone()[0] == 0
# Prove that status records are independent after migration.
db.execute("UPDATE school_learner_status SET status='ACTIVE', updated_at=201 WHERE school_id='school-b' AND learner_id='learner-shared'")
checks['school-local status can diverge after migration'] = db.execute("SELECT status FROM school_learner_status WHERE school_id='school-a' AND learner_id='learner-shared'").fetchone() == ('ARCHIVED',) and db.execute("SELECT status FROM school_learner_status WHERE school_id='school-b' AND learner_id='learner-shared'").fetchone() == ('ACTIVE',)
checks['foreign-key check passes after fixture migration'] = db.execute('PRAGMA foreign_key_check').fetchall() == []
db.close()

for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V127 migration/recovery checks failed'
print(f'V127: {len(checks)}/{len(checks)} checks passed')
