"""V121 migration/round-trip regression fixture plus source-contract checks.

Uses Python's real SQLite engine to exercise the additive school-status migration and
backup/restore-shaped row round-trip. This is not an Android instrumentation test.
"""
from pathlib import Path
import sqlite3
import tempfile

root = Path(__file__).resolve().parents[1]
db_source = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text()
backup_source = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()

checks = {
    'database schema remains v10 until migration is explicitly advanced': 'private const val DB_VERSION = 10' in db_source,
    'status migration creates composite school/learner primary key': 'PRIMARY KEY(school_id, learner_id)' in db_source,
    'status migration backfills each enrolled school from legacy status': 'SELECT e.school_id, l.id, l.status' in db_source and 'FROM learner l JOIN enrollment e ON e.learner_id = l.id' in db_source,
    'un-enrolled legacy learners are retained in legacy school': "SELECT 'legacy-school', l.id, l.status" in db_source,
    'status assignment preserves existing row rather than overwriting on insert': 'INSERT OR IGNORE INTO school_learner_status' in db_source,
    'backup exports lifecycle table': '"school_learner_status"' in backup_source,
    'restore backfills missing lifecycle rows': 'backfillSchoolStatusesAfterRestore(db)' in backup_source,
    'restore integrity check occurs before transaction commit': backup_source.index('AcademicDatabaseIntegrity.requireHealthy(db)') < backup_source.index('db.setTransactionSuccessful()'),
}

# Behavioral fixture: exercise the same additive backfill SQL against SQLite.
conn = sqlite3.connect(':memory:')
conn.execute('PRAGMA foreign_keys=ON')
conn.executescript('''
CREATE TABLE school(id TEXT PRIMARY KEY);
CREATE TABLE learner(id TEXT PRIMARY KEY, status TEXT NOT NULL DEFAULT 'ACTIVE');
CREATE TABLE enrollment(id TEXT PRIMARY KEY, school_id TEXT NOT NULL REFERENCES school(id), learner_id TEXT NOT NULL REFERENCES learner(id), academic_year INTEGER NOT NULL, school_class TEXT NOT NULL);
CREATE TABLE assessment(id TEXT PRIMARY KEY, enrollment_id TEXT NOT NULL REFERENCES enrollment(id));
CREATE TABLE mark(id TEXT PRIMARY KEY, assessment_id TEXT NOT NULL REFERENCES assessment(id), mark INTEGER NOT NULL);
CREATE TABLE learner_status_history(id TEXT PRIMARY KEY, learner_id TEXT NOT NULL REFERENCES learner(id), reason TEXT NOT NULL);
INSERT INTO school VALUES ('school-a'), ('school-b'), ('legacy-school');
INSERT INTO learner VALUES ('shared-learner', 'ARCHIVED'), ('legacy-only', 'ACTIVE');
INSERT INTO enrollment VALUES ('en-a','school-a','shared-learner',2026,'P5'), ('en-b','school-b','shared-learner',2026,'P5');
INSERT INTO assessment VALUES ('assess-a','en-a');
INSERT INTO mark VALUES ('mark-a','assess-a',87);
INSERT INTO learner_status_history VALUES ('hist-a','shared-learner','legacy archive reason');
CREATE TABLE school_learner_status (
 school_id TEXT NOT NULL REFERENCES school(id) ON DELETE CASCADE,
 learner_id TEXT NOT NULL REFERENCES learner(id) ON DELETE CASCADE,
 status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','ARCHIVED')),
 created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL,
 PRIMARY KEY(school_id, learner_id));
''')
now = 123456789
conn.execute('''INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
 SELECT e.school_id, l.id, l.status, ?, ? FROM learner l JOIN enrollment e ON e.learner_id = l.id GROUP BY e.school_id, l.id''', (now, now))
conn.execute('''INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
 SELECT 'legacy-school', l.id, l.status, ?, ? FROM learner l WHERE NOT EXISTS (SELECT 1 FROM enrollment e WHERE e.learner_id = l.id)''', (now, now))

behavior = {
 'shared learner receives status row in both schools': conn.execute("SELECT COUNT(*) FROM school_learner_status WHERE learner_id='shared-learner'").fetchone()[0] == 2,
 'legacy status is backfilled without changing either school row': conn.execute("SELECT COUNT(*) FROM school_learner_status WHERE learner_id='shared-learner' AND status='ARCHIVED'").fetchone()[0] == 2,
 'un-enrolled learner receives legacy-school status': conn.execute("SELECT status FROM school_learner_status WHERE school_id='legacy-school' AND learner_id='legacy-only'").fetchone() == ('ACTIVE',),
 'mark, assessment, enrollment and history survive migration': all(conn.execute(f'SELECT COUNT(*) FROM {t}').fetchone()[0] == 1 for t in ('assessment','mark','learner_status_history')) and conn.execute('SELECT COUNT(*) FROM enrollment').fetchone()[0] == 2,
 'archive change in school A leaves school B unchanged': (conn.execute("UPDATE school_learner_status SET status='ACTIVE', updated_at=? WHERE school_id='school-a' AND learner_id='shared-learner'", (now+1,)).rowcount == 1 and conn.execute("SELECT status FROM school_learner_status WHERE school_id='school-b' AND learner_id='shared-learner'").fetchone() == ('ARCHIVED',)),
 'backup-shaped status rows round-trip without collapsing school identity': (lambda rows: (conn.execute('DELETE FROM school_learner_status'), conn.executemany('INSERT INTO school_learner_status VALUES (?,?,?,?,?)', rows), conn.execute("SELECT COUNT(*) FROM school_learner_status WHERE learner_id='shared-learner'").fetchone()[0] == 2)[-1])(conn.execute('SELECT school_id, learner_id, status, created_at, updated_at FROM school_learner_status').fetchall()),
}
for name, ok in {**checks, **behavior}.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()) and all(behavior.values()), 'V121 migration/round-trip checks failed'
print(f'V121: {len(checks)+len(behavior)}/{len(checks)+len(behavior)} checks passed (source contracts + SQLite fixture)')
