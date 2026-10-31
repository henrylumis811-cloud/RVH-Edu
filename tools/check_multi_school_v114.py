#!/usr/bin/env python3
"""Source-contract regression guard for the active-school learner directory after V115."""
from pathlib import Path
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
service = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt").read_text(encoding="utf-8")
repo = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt").read_text(encoding="utf-8")
checks = {
    "learner directory uses active school roster": "fun allLearners(): List<Learner> =\n        learnerRepository.getEnrolledLearnersForSchool(activeSchoolId())" in service,
    "name search uses active school roster": "learnerRepository.getEnrolledLearnersForSchool(activeSchoolId()).filter" in service,
    "name search retains optional age filter": "ageYears == null || learner.ageYears == ageYears" in service,
    "repository roster requires explicit school ID": "fun getEnrolledLearnersForSchool(schoolId: String)" in repo and 'require(schoolId.isNotBlank())' in repo,
    "repository roster query is school filtered and distinct": "SELECT DISTINCT l.id, COALESCE(san.admission_number, l.admission_number)" in repo and "LEFT JOIN school_admission_number san ON san.learner_id = l.id AND san.school_id = e.school_id" in repo and "WHERE e.school_id = ? ORDER BY l.full_name COLLATE NOCASE ASC" in repo,
    "global learner identity APIs retained for compatibility": "fun getAll(): List<Learner>" in repo and "fun findPossibleDuplicates" in repo,
}

# Exercise the same school-filtered roster query against a tiny SQLite fixture. This is not an
# Android instrumentation test, but it verifies distinctness and school isolation semantics.
connection = sqlite3.connect(":memory:")
connection.executescript("""
CREATE TABLE learner (id TEXT PRIMARY KEY, full_name TEXT NOT NULL);
CREATE TABLE enrollment (id TEXT PRIMARY KEY, learner_id TEXT NOT NULL, school_id TEXT NOT NULL);
INSERT INTO learner VALUES ('shared', 'Shared learner'), ('only-a', 'School A learner'), ('only-b', 'School B learner');
INSERT INTO enrollment VALUES ('e1', 'shared', 'school-a'), ('e2', 'shared', 'school-a'),
                             ('e3', 'only-a', 'school-a'), ('e4', 'shared', 'school-b'),
                             ('e5', 'only-b', 'school-b');
""")
query = "SELECT DISTINCT l.* FROM learner l INNER JOIN enrollment e ON e.learner_id = l.id WHERE e.school_id = ? ORDER BY l.full_name COLLATE NOCASE ASC"
rows_a = [row[0] for row in connection.execute(query, ("school-a",))]
rows_b = [row[0] for row in connection.execute(query, ("school-b",))]
checks["SQLite fixture returns distinct school-A roster without school-B-only learner"] = rows_a == ["only-a", "shared"]
checks["SQLite fixture returns school-B roster independently"] = rows_b == ["only-b", "shared"]
connection.close()

for label, ok in checks.items():
    print(f"{'PASS' if ok else 'FAIL'}: {label}")
print(f"{sum(checks.values())}/{len(checks)} checks passed")
raise SystemExit(0 if all(checks.values()) else 1)
