"""V122 legacy backup compatibility checks (source contracts + SQLite behavior fixture).

The SQLite fixture validates the compatibility rules independently; it is not an Android
instrumentation test and does not execute Kotlin directly.
"""
from pathlib import Path
import sqlite3

root = Path(__file__).resolve().parents[1]
backup = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
checks = {
    'preview normalizes legacy payload only after checksum verification': backup.index('expected == sha256(canonicalJson(tableObject))') < backup.index('normalizeLegacyBackupTables(tableObject, schema)') ,
    'restore normalizes legacy payload only after checksum verification': backup.index('expectedDigest == actualDigest') < backup.index('normalizeLegacyBackupTables(tableObject, backupSchemaVersion)'),
    'schema 4 and 5 backups without status history receive an empty audit table': 'if (schemaVersion < 6 && !tableObject.has("learner_status_history"))' in backup,
    'pre-multischool backup can be normalized with a school table': 'if (schemaVersion < 7 && !tableObject.has("school"))' in backup,
    'pre-multischool enrollments are assigned to legacy-school': 'row.put("school_id", "legacy-school")' in backup and 'if (schemaVersion < 7)' in backup,
    'legacy school record is added when old schemas need a stable school': 'if (requiresLegacySchool && !legacySchoolExists)' in backup and 'requiresLegacySchool = true' in backup,
    'old history rows are assigned legacy-school before domain validation': backup.index('normalizeLegacyBackupTables(tableObject, backupSchemaVersion)') < backup.index('validateDomainRows(tableObject, backupSchemaVersion)'),
    'restore passes source backup schema into row-shape validation': 'validateBackupRows(db, tableObject, backupSchemaVersion)' in backup,
    'current-schema backups still require exact column sets': 'if (backupSchemaVersion >= SCHEMA_VERSION)' in backup and 'require(keys == schema.keys)' in backup,
    'legacy backups may omit only columns with safe defaults or nullable definitions': 'Legacy backup omits required field $table.$column, which has no safe database default.' in backup,
    'missing columns use SQLite defaults by remaining omitted from ContentValues': 'db.insertOrThrow(table, null, values)' in backup,
    'checksum verification remains before compatibility mutations': backup.index('expectedDigest == actualDigest') < backup.index('normalizeLegacyBackupTables(tableObject, backupSchemaVersion)'),
    'restore still validates domain data before transaction starts': backup.index('validateDomainRows(tableObject, backupSchemaVersion)') < backup.index('db.beginTransaction()'),
    'restore still performs integrity check before commit': backup.index('AcademicDatabaseIntegrity.requireHealthy(db)') < backup.index('db.setTransactionSuccessful()'),
}

# Real SQLite fixture modelling representative old schema payloads and safe default filling.
conn = sqlite3.connect(':memory:')
conn.execute('PRAGMA foreign_keys=ON')
conn.executescript('''
CREATE TABLE school(id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL DEFAULT '', motto TEXT NOT NULL DEFAULT '', created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL);
CREATE TABLE learner(id TEXT PRIMARY KEY NOT NULL, full_name TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'ACTIVE', created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL);
CREATE TABLE enrollment(id TEXT PRIMARY KEY NOT NULL, school_id TEXT NOT NULL DEFAULT 'legacy-school', learner_id TEXT NOT NULL REFERENCES learner(id), academic_year INTEGER NOT NULL, school_class TEXT NOT NULL, stream TEXT, enrolled_at INTEGER NOT NULL, FOREIGN KEY(school_id) REFERENCES school(id));
CREATE TABLE assessment(id TEXT PRIMARY KEY NOT NULL, enrollment_id TEXT NOT NULL REFERENCES enrollment(id), term TEXT NOT NULL, section TEXT NOT NULL, recorded_at INTEGER NOT NULL, status TEXT NOT NULL DEFAULT 'DRAFT', finalized_at INTEGER, policy_snapshot_json TEXT);
CREATE TABLE mark(id TEXT PRIMARY KEY NOT NULL, assessment_id TEXT NOT NULL REFERENCES assessment(id), subject_code TEXT NOT NULL, mark INTEGER NOT NULL, max_mark INTEGER NOT NULL DEFAULT 100, aggregate INTEGER, source TEXT NOT NULL DEFAULT 'MANUAL', updated_at INTEGER NOT NULL);
CREATE TABLE learner_status_history(id TEXT PRIMARY KEY NOT NULL, learner_id TEXT NOT NULL REFERENCES learner(id), school_id TEXT NOT NULL DEFAULT 'legacy-school', from_status TEXT NOT NULL, to_status TEXT NOT NULL, reason TEXT NOT NULL, note TEXT, changed_at INTEGER NOT NULL);
''')
now = 1_800_000_000_000
conn.execute('INSERT INTO school(id,name,motto,created_at,updated_at) VALUES(?,?,?,?,?)', ('legacy-school','','',now,now))
conn.execute('INSERT INTO learner(id,full_name,status,created_at,updated_at) VALUES(?,?,?,?,?)', ('l1','Amina','ACTIVE',now,now))
# Legacy row shape omits later-added school_id, status/finalization snapshot fields and nullable fields.
conn.execute('INSERT INTO enrollment(id,learner_id,academic_year,school_class,enrolled_at) VALUES(?,?,?,?,?)', ('e1','l1',2026,'P5',now))
conn.execute('INSERT INTO assessment(id,enrollment_id,term,section,recorded_at) VALUES(?,?,?,?,?)', ('a1','e1','TERM2','MID',now))
conn.execute('INSERT INTO mark(id,assessment_id,subject_code,mark,updated_at) VALUES(?,?,?,?,?)', ('m1','a1','MATH',87,now))
conn.execute('INSERT INTO learner_status_history(id,learner_id,from_status,to_status,reason,changed_at) VALUES(?,?,?,?,?,?)', ('h1','l1','ACTIVE','ARCHIVED','moved',now))

behavior = {
    'SQLite applies legacy-school default to old enrollment rows': conn.execute("SELECT school_id FROM enrollment WHERE id='e1'").fetchone() == ('legacy-school',),
    'SQLite applies assessment defaults to old assessment rows': conn.execute("SELECT status, finalized_at, policy_snapshot_json FROM assessment WHERE id='a1'").fetchone() == ('DRAFT', None, None),
    'SQLite applies mark defaults to old mark rows': conn.execute("SELECT max_mark, source, aggregate FROM mark WHERE id='m1'").fetchone() == (100, 'MANUAL', None),
    'legacy learner, enrollment, assessment, and mark IDs remain stable': conn.execute("SELECT id FROM learner").fetchone() == ('l1',) and conn.execute("SELECT id FROM enrollment").fetchone() == ('e1',) and conn.execute("SELECT id FROM assessment").fetchone() == ('a1',) and conn.execute("SELECT id FROM mark").fetchone() == ('m1',),
    'legacy audit record remains present and is attributable to legacy school': conn.execute("SELECT school_id, reason FROM learner_status_history WHERE id='h1'").fetchone() == ('legacy-school','moved'),
    'legacy round-trip retains marks and audit history counts': conn.execute('SELECT COUNT(*) FROM mark').fetchone()[0] == 1 and conn.execute('SELECT COUNT(*) FROM learner_status_history').fetchone()[0] == 1,
}
for name, ok in {**checks, **behavior}.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()) and all(behavior.values()), 'V122 legacy backup compatibility checks failed'
print(f'V122: {len(checks) + len(behavior)}/{len(checks) + len(behavior)} checks passed (source contracts + SQLite fixture)')
