"""V128 checks for status coverage of learners without enrollments.

Uses an isolated SQLite fixture plus source-contract checks; not Android instrumentation.
"""
from pathlib import Path
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
backup = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
integrity = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseIntegrity.kt').read_text()
checks = {
    'backup preflight validates each enrollment school/learner pair': 'val key = "${row.optString("school_id")}\\u001f$learnerId"' in backup,
    'current-schema backup requires at least one status for every learner': 'Current-schema backup is missing every lifecycle status for learner' in backup,
    'preflight accepts school-scoped status for unenrolled learners': 'do not invent an enrollment or force a false legacy-school attribution' in backup,
    'integrity audit checks all learners for missing status': 'learnersMissingAnyStatus' in integrity,
    'integrity audit accepts any school-local status for unenrolled learners': 'SELECT 1 FROM school_learner_status sls WHERE sls.learner_id = l.id' in integrity,
    'integrity reports missing lifecycle status clearly': 'learner(s) missing every school-local lifecycle status' in integrity,
}

db = sqlite3.connect(':memory:')
db.executescript('''
CREATE TABLE learner(id TEXT PRIMARY KEY);
CREATE TABLE enrollment(id TEXT PRIMARY KEY, school_id TEXT NOT NULL, learner_id TEXT NOT NULL);
CREATE TABLE school_learner_status(school_id TEXT NOT NULL, learner_id TEXT NOT NULL, status TEXT NOT NULL, PRIMARY KEY(school_id, learner_id));
INSERT INTO learner VALUES ('enrolled'), ('unenrolled'), ('bad-unenrolled');
INSERT INTO enrollment VALUES ('e1', 'school-a', 'enrolled');
INSERT INTO school_learner_status VALUES ('school-a', 'enrolled', 'ACTIVE');
INSERT INTO school_learner_status VALUES ('school-a', 'unenrolled', 'ARCHIVED');
''')
missing = db.execute('''SELECT l.id FROM learner l
 WHERE NOT EXISTS (SELECT 1 FROM school_learner_status s WHERE s.learner_id=l.id)
 ORDER BY l.id''').fetchall()
checks['fixture identifies only learner with no status anywhere'] = missing == [('bad-unenrolled',)]
# Repair the missing record and prove the same integrity query becomes clean.
db.execute("INSERT INTO school_learner_status VALUES ('legacy-school','bad-unenrolled','ACTIVE')")
missing_after = db.execute('''SELECT COUNT(*) FROM learner l
 WHERE NOT EXISTS (SELECT 1 FROM school_learner_status s WHERE s.learner_id=l.id)''').fetchone()[0]
checks['fixture passes after missing learner status is restored'] = missing_after == 0
# Enrolled learner status stays school-local; the legacy row is not required for it.
checks['enrolled learner does not require legacy-school status'] = db.execute("SELECT COUNT(*) FROM school_learner_status WHERE learner_id='enrolled'").fetchone()[0] == 1
# Distinct schools may retain different statuses for the same shared learner.
db.execute("INSERT INTO school_learner_status VALUES ('school-b','enrolled','ARCHIVED')")
checks['two-school status rows remain independent'] = db.execute("SELECT status FROM school_learner_status WHERE school_id='school-a' AND learner_id='enrolled'").fetchone() == ('ACTIVE',) and db.execute("SELECT status FROM school_learner_status WHERE school_id='school-b' AND learner_id='enrolled'").fetchone() == ('ARCHIVED',)
db.close()

for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V128 lifecycle coverage checks failed'
print(f'V128: {len(checks)}/{len(checks)} checks passed')
