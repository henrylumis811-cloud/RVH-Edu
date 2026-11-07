"""V125 school-local lifecycle integrity checks.

Uses SQLite to exercise the same SQL invariants added to AcademicDatabaseIntegrity.kt.
This is not an Android instrumentation test and does not compile the app.
"""
from pathlib import Path
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseIntegrity.kt').read_text()
checks = {
    'integrity audit requires a matching school/learner status pair': 'sls.school_id = e.school_id AND sls.learner_id = e.learner_id' in source,
    'integrity audit reports missing school-local statuses': 'enrollment(s) missing school-local learner status' in source,
    'integrity audit rejects non-positive lifecycle timestamps': 'created_at <= 0 OR updated_at <= 0' in source,
    'integrity audit rejects update timestamps before creation': 'updated_at < created_at' in source,
    'status coverage audit runs before orphan checks': source.index('enrollmentsMissingSchoolStatus') < source.index('val orphanLearnerEnrollments'),
}

db = sqlite3.connect(':memory:')
db.executescript('''
CREATE TABLE school(id TEXT PRIMARY KEY);
CREATE TABLE learner(id TEXT PRIMARY KEY);
CREATE TABLE enrollment(id TEXT PRIMARY KEY, school_id TEXT NOT NULL, learner_id TEXT NOT NULL);
CREATE TABLE school_learner_status(
  school_id TEXT NOT NULL, learner_id TEXT NOT NULL, status TEXT NOT NULL,
  created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL,
  PRIMARY KEY(school_id, learner_id));
INSERT INTO school VALUES ('school-a'), ('school-b');
INSERT INTO learner VALUES ('learner-1');
INSERT INTO enrollment VALUES ('enrol-a', 'school-a', 'learner-1'), ('enrol-b', 'school-b', 'learner-1');
INSERT INTO school_learner_status VALUES ('school-a', 'learner-1', 'ARCHIVED', 100, 200);
''')
missing_query = '''SELECT COUNT(*) FROM enrollment e
LEFT JOIN school_learner_status sls
 ON sls.school_id = e.school_id AND sls.learner_id = e.learner_id
WHERE sls.school_id IS NULL'''
checks['fixture detects missing status for second school even when first exists'] = db.execute(missing_query).fetchone()[0] == 1
db.execute("INSERT INTO school_learner_status VALUES ('school-b', 'learner-1', 'ACTIVE', 100, 200)")
checks['fixture accepts independent active and archived status across schools'] = db.execute(missing_query).fetchone()[0] == 0
bad_timestamp_query = 'SELECT COUNT(*) FROM school_learner_status WHERE created_at <= 0 OR updated_at <= 0 OR updated_at < created_at'
checks['fixture detects invalid lifecycle timestamps'] = db.execute(bad_timestamp_query).fetchone()[0] == 0
db.execute("UPDATE school_learner_status SET updated_at=50 WHERE school_id='school-b'")
checks['fixture detects update timestamp earlier than creation'] = db.execute(bad_timestamp_query).fetchone()[0] == 1

db.close()
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V125 school-local status integrity checks failed'
print(f'V125: {len(checks)}/{len(checks)} checks passed')
