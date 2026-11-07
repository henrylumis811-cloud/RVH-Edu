#!/usr/bin/env python3
"""V115 school-local admission-number migration and isolation regression checks."""
from pathlib import Path
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
db_source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt").read_text(encoding="utf-8")
repo = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt").read_text(encoding="utf-8")
service = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt").read_text(encoding="utf-8")
backup = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").read_text(encoding="utf-8")
integrity = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseIntegrity.kt").read_text(encoding="utf-8")

checks = {
    "database schema increments to v10": "private const val DB_VERSION = 10" in db_source,
    "school-local admission table has learner and school foreign keys": "CREATE TABLE IF NOT EXISTS school_admission_number" in db_source and "FOREIGN KEY(school_id) REFERENCES school(id) ON DELETE CASCADE" in db_source and "FOREIGN KEY(learner_id) REFERENCES learner(id) ON DELETE CASCADE" in db_source,
    "legacy admission values are migrated before clearing shared column": "backfillSchoolAdmissionNumbersFromLegacyColumn(db)" in db_source and db_source.index("backfillSchoolAdmissionNumbersFromLegacyColumn(db)") < db_source.index('UPDATE learner SET admission_number = NULL'),
    "repository duplicate guard is scoped to school": 'school_id = ? AND normalized_admission_number = ? AND learner_id <> ?' in repo,
    "repository persists the local admission assignment transactionally": 'db.insertOrThrow("school_admission_number", null, assignment)' in repo and 'db.beginTransaction()' in repo and 'db.setTransactionSuccessful()' in repo,
    "school roster reads local admission value": 'COALESCE(san.admission_number, l.admission_number)' in repo and 'san.school_id = e.school_id' in repo,
    "create and edit flows check active-school admission values": 'findByAdmissionNumberInSchool(cleanAdmissionNumber, activeSchoolId())' in service,
    "backup enrollment uniqueness is scoped by school": 'listOf(row.optString("school_id"), row.optString("learner_id"), row.optInt("academic_year"), schoolClass)' in backup,
    "backup includes local admission table and old-backup migration": '"school_admission_number"' in backup and 'migrateLegacyAdmissionsInRestoredBackup(db)' in backup,
    "database integrity audit includes admission assignments": 'school_admission_number' in integrity and 'invalid school-local admission assignment' in integrity,
}

# Execute the migration semantics against SQLite: existing IDs/history survive, a learner shared
# by two schools receives a school-local assignment in each, and two schools may reuse a number.
con = sqlite3.connect(":memory:")
con.execute("PRAGMA foreign_keys=ON")
con.executescript("""
CREATE TABLE school(id TEXT PRIMARY KEY);
CREATE TABLE learner(id TEXT PRIMARY KEY, admission_number TEXT UNIQUE, full_name TEXT NOT NULL);
CREATE TABLE enrollment(id TEXT PRIMARY KEY, school_id TEXT NOT NULL REFERENCES school(id), learner_id TEXT NOT NULL REFERENCES learner(id), academic_year INTEGER, school_class TEXT);
CREATE TABLE assessment(id TEXT PRIMARY KEY, enrollment_id TEXT NOT NULL REFERENCES enrollment(id));
CREATE TABLE mark(id TEXT PRIMARY KEY, assessment_id TEXT NOT NULL REFERENCES assessment(id), mark INTEGER NOT NULL);
INSERT INTO school VALUES ('school-a'), ('school-b'), ('legacy-school');
INSERT INTO learner VALUES ('shared', 'old-007', 'Shared learner'), ('old-a', 'old-008', 'Old A'), ('old-b', NULL, 'Old B');
INSERT INTO enrollment VALUES ('en-a', 'school-a', 'shared', 2026, 'P7'), ('en-b', 'school-b', 'shared', 2026, 'P7'), ('en-a2', 'school-a', 'old-a', 2026, 'P7');
INSERT INTO assessment VALUES ('assess-1', 'en-a');
INSERT INTO mark VALUES ('mark-1', 'assess-1', 75);
CREATE TABLE school_admission_number (
 id TEXT PRIMARY KEY, school_id TEXT NOT NULL REFERENCES school(id) ON DELETE CASCADE,
 learner_id TEXT NOT NULL REFERENCES learner(id) ON DELETE CASCADE,
 admission_number TEXT NOT NULL, normalized_admission_number TEXT NOT NULL,
 created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, UNIQUE(school_id, learner_id));
""")
legacy = con.execute("SELECT id, admission_number FROM learner WHERE admission_number IS NOT NULL AND TRIM(admission_number) <> ''").fetchall()
for learner_id, raw in legacy:
    admission = " ".join(raw.strip().split())
    normalized = admission.upper()
    schools = [r[0] for r in con.execute("SELECT DISTINCT school_id FROM enrollment WHERE learner_id=?", (learner_id,))]
    if not schools:
        schools = ["legacy-school"]
    for school_id in schools:
        con.execute("INSERT OR IGNORE INTO school_admission_number VALUES (?, ?, ?, ?, ?, ?, ?)", (f"migrated-{school_id}-{learner_id}", school_id, learner_id, admission, normalized, 1, 1))
con.execute("UPDATE learner SET admission_number=NULL WHERE admission_number IS NOT NULL")
checks["SQLite migration preserves learner, enrollment, assessment and mark IDs"] = (
    con.execute("SELECT id FROM learner ORDER BY id").fetchall() == [("old-a",), ("old-b",), ("shared",)] and
    con.execute("SELECT id FROM enrollment ORDER BY id").fetchall() == [("en-a",), ("en-a2",), ("en-b",)] and
    con.execute("SELECT id FROM assessment").fetchall() == [("assess-1",)] and
    con.execute("SELECT id, mark FROM mark").fetchall() == [("mark-1", 75)]
)
checks["SQLite migration assigns a shared learner's number to both enrolled schools"] = con.execute(
    "SELECT COUNT(*) FROM school_admission_number WHERE learner_id='shared' AND normalized_admission_number='OLD-007'"
).fetchone()[0] == 2
checks["SQLite migration clears the shared legacy admission column"] = con.execute(
    "SELECT COUNT(*) FROM learner WHERE admission_number IS NOT NULL"
).fetchone()[0] == 0
# Reusing the same normalized admission number is permitted across schools.
con.execute("INSERT INTO school_admission_number VALUES ('new-a', 'school-a', 'old-b', 'A-001', 'A-001', 2, 2)")
con.execute("INSERT INTO school_admission_number VALUES ('new-b', 'school-b', 'old-b', 'A-001', 'A-001', 2, 2)")
checks["SQLite allows the same admission number in different schools"] = con.execute(
    "SELECT COUNT(*) FROM school_admission_number WHERE learner_id='old-b' AND normalized_admission_number='A-001'"
).fetchone()[0] == 2
# But same-school duplicate checking finds a collision.
checks["SQLite school-scoped duplicate query detects an existing number"] = con.execute(
    "SELECT learner_id FROM school_admission_number WHERE school_id=? AND normalized_admission_number=? AND learner_id<>?",
    ("school-a", "A-001", "shared")
).fetchone() == ("old-b",)
con.close()

for label, passed in checks.items():
    print(f"{'PASS' if passed else 'FAIL'}: {label}")
print(f"{sum(checks.values())}/{len(checks)} checks passed")
raise SystemExit(0 if all(checks.values()) else 1)
