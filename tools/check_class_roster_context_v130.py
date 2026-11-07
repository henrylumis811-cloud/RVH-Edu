#!/usr/bin/env python3
"""Regression check for the V130 ClassRosterRepository Kotlin compile fix."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/repository/ClassRosterRepository.kt"
text = source.read_text()
checks = [
    ("Context is retained as a property", "class ClassRosterRepository(private val context: Context) : AutoCloseable" in text),
    ("Database helper uses constructor context", "RvhEduDatabase(context)" in text),
    ("Roster enrollment query remains school-scoped", "academics.getEnrollments(year, schoolClass, SchoolManagementRepository.activeId(context))" in text),
    ("Learner lookup remains school-scoped", "learners.getById(enrollment.learnerId, SchoolManagementRepository.activeId(context))" in text),
    ("Repository still closes owned resources", "override fun close() { academics.close(); db.close() }" in text),
]
for label, ok in checks:
    print(f"{'PASS' if ok else 'FAIL'}: {label}")
print(f"{sum(ok for _, ok in checks)}/{len(checks)} checks passed")
raise SystemExit(0 if all(ok for _, ok in checks) else 1)
