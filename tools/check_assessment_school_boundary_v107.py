#!/usr/bin/env python3
"""Source-contract checks for assessment persistence boundaries (V107)."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt'
service = root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt'
r = repo.read_text(); s = service.read_text()
checks = {
 'assessment lookup is keyed by enrollment identity': '"enrollment_id = ? AND term = ? AND section = ?"' in r,
 'assessment writes preserve enrollment identity': 'put("enrollment_id", assessment.enrollmentId)' in r,
 'mark replacement validates assessment identity': 'require(mark.assessmentId == assessmentId)' in r,
 'marks are replaced transactionally': 'database.beginTransaction()' in r and 'replaceMarksInsideTransaction(database, assessmentId, marks)' in r,
 'finalization uses conditional draft update': '"id = ? AND status = ?"' in r,
 'service checks supplied assessment belongs to enrollment': 'assessment.enrollmentId == enrollment.id' in s,
 'service uses school-verified persisted assessment before saving': 'val persisted = academicRepository.getAssessmentForSchool(enrollment.id, assessment.term, assessment.section, activeSchoolId())' in s,
 'service loads marks through school-verified lookup': 'academicRepository.getMarksForSchool(assessment.id, activeSchoolId())' in s,
}
for name, passed in checks.items(): print(f"{'PASS' if passed else 'FAIL'}: {name}")
print(f"{sum(checks.values())}/{len(checks)} checks passed")
raise SystemExit(0 if all(checks.values()) else 1)
