from pathlib import Path
p = Path(__file__).resolve().parents[1] / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt'
s = p.read_text()
checks = {
    'assessment lookup uses active school boundary': 'getAssessment(enrollment.id, term, section)' not in s and 'getAssessmentForSchool(enrollment.id, term, section, activeSchoolId())' in s,
    'assessment history uses school-scoped listing': 'getAssessmentsForEnrollmentInSchool(enrollment.id, activeSchoolId())' in s,
    'mark loading checks active school': 'getMarksForSchool(assessment.id, activeSchoolId())' in s,
    'calculation mark reads are school-scoped': 'getMarksForSchool(canonical.id, activeSchoolId())' in s,
    'class analytics mark reads are school-scoped': 'getMarksForSchool(it.id, activeSchoolId())' in s,
    'class results assessment lookup is scoped': 'getAssessmentForSchool(enrollment.id, term, section, activeSchoolId())' in s,
    'legacy unscoped assessment lookup absent in service': '.getAssessment(' not in s,
    'legacy unscoped mark read absent in service': '.getMarks(' not in s,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ' ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)}/{len(checks)} checks passed')
