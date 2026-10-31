#!/usr/bin/env python3
"""V106 guards against accidental legacy-default access to school-scoped enrollments."""
from pathlib import Path
import re, sys
ROOT = Path(__file__).resolve().parents[1]
repo_path = ROOT/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt'
repo = repo_path.read_text()
service = (ROOT/'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
roster = (ROOT/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/ClassRosterRepository.kt').read_text()
checks = {
 'enrollment upsert requires explicit school ID': bool(re.search(r'fun upsertEnrollment\(enrollment: Enrollment, schoolId: String\)', repo)),
 'atomic progression requires explicit school ID': bool(re.search(r'fun insertEnrollmentsAtomically\([\s\S]*?schoolId: String\s*\)\s*:\s*List<Enrollment>', repo)),
 'learner history query requires explicit school ID': 'fun getEnrollmentsForLearner(learnerId: String, schoolId: String):' in repo,
 'class roster query requires explicit school ID': 'fun getEnrollments(academicYear: Int, schoolClass: SchoolClass, schoolId: String):' in repo,
 'legacy-school fallback removed from repository API': 'LEGACY_SCHOOL_ID' not in repo,
 'service resolves current school from management repository': 'private fun activeSchoolId(): String = SchoolManagementRepository.activeId(appContext)' in service,
 'service passes school ID for enrollment creation': 'schoolId = activeSchoolId()' in service and 'upsertEnrollment(' in service,
 'service scopes learner history': 'getEnrollmentsForLearner(learnerId, activeSchoolId())' in service,
 'class roster scopes school query': 'SchoolManagementRepository.activeId(context)' in roster,
}
for label, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' ' + label)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
sys.exit(0 if all(checks.values()) else 1)
