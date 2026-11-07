#!/usr/bin/env python3
"""Source-contract checks for school-verified assessment read APIs."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
repo = ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt'
s = repo.read_text(encoding='utf-8')
checks = {
    'school-scoped assessment lookup requires school ID': 'fun getAssessmentForSchool(enrollmentId: String, term: Term, section: TermSection, schoolId: String)' in s,
    'assessment lookup validates enrollment ownership': 'requireEnrollmentBelongsToSchool(enrollmentId, schoolId)' in s,
    'school-scoped history lookup exists': 'fun getAssessmentsForEnrollmentInSchool(enrollmentId: String, schoolId: String)' in s,
    'school-scoped mark lookup exists': 'fun getMarksForSchool(assessmentId: String, schoolId: String)' in s,
    'mark lookup joins assessment to enrollment': 'JOIN enrollment e ON e.id = a.enrollment_id WHERE a.id = ? AND e.school_id = ?' in s,
    'foreign school assessment is rejected': 'Assessment does not belong to the selected school' in s,
    'blank school ID is rejected': 'A school must be selected before reading marks' in s,
}
for name, ok in checks.items(): print(f"{'PASS' if ok else 'FAIL'}: {name}")
print(f"Result: {sum(checks.values())}/{len(checks)} checks passed")
raise SystemExit(0 if all(checks.values()) else 1)
