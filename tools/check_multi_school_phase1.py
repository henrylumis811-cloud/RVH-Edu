#!/usr/bin/env python3
"""Source-level guardrails for the first offline multi-school schema increment."""
from pathlib import Path
p = Path(__file__).resolve().parents[1] / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt'
s = p.read_text()
checks = {
    'database version advanced to 9': 'private const val DB_VERSION = 9' in s,
    'enrollment stores school identity': 'school_id TEXT NOT NULL DEFAULT \'legacy-school\'' in s,
    'teacher identity table exists': 'CREATE TABLE IF NOT EXISTS teacher (' in s,
    'time-bounded school memberships exist': 'CREATE TABLE IF NOT EXISTS teacher_school_membership (' in s and 'ended_at INTEGER' in s,
    'memberships allow multiple active schools per teacher': 'idx_membership_teacher_status' in s and 'UNIQUE(teacher_id' not in s,
    'teaching assignments link to membership': 'CREATE TABLE IF NOT EXISTS teaching_assignment (' in s and 'FOREIGN KEY(membership_id) REFERENCES teacher_school_membership(id)' in s,
    'legacy school seeded idempotently': "INSERT OR IGNORE INTO school (id, name, motto, created_at, updated_at) VALUES ('legacy-school'" in s,
    'migration rebuild preserves dependent academic rows': 'rebuildAcademicTablesForSchoolScopedUniqueness(db)' in s and 'CREATE TEMP TABLE mark_v7_copy AS SELECT' in s and 'INSERT INTO mark (id, assessment_id' in s,
    'foreign keys enabled': 'db.setForeignKeyConstraintsEnabled(true)' in s,
    'phase scope documented': ((Path(__file__).resolve().parents[1]) / 'RVH_EDU_MULTI_SCHOOL_PHASE1.md').exists(),
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)}/{len(checks)} checks passed')
