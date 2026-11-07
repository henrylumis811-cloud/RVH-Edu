#!/usr/bin/env python3
"""Source-contract checks for RVH Edu V105 school-context propagation."""
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
service = (ROOT/'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
repo = (ROOT/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt').read_text()
roster = (ROOT/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/ClassRosterRepository.kt').read_text()
checks = {
 'service has one active-school context accessor': 'private fun activeSchoolId(): String = SchoolManagementRepository.activeId(appContext)' in service,
 'enrollment creation passes active school': 'schoolClass = schoolClass, stream = cleanStream), schoolId = activeSchoolId())' in service,
 'bulk progression passes active school': 'schoolId = activeSchoolId()' in service,
 'roster query passes active school': 'academics.getEnrollments(year, schoolClass, SchoolManagementRepository.activeId(context))' in roster,
 'learner history explicitly scopes enrollment lookup': 'getEnrollmentsForLearner(learnerId, schoolId)' in service and 'val schoolId = activeSchoolId()' in service,
 'learner history no longer calls legacy-default overload': 'getEnrollmentsForLearner(learnerId).map' not in service,
 'repository retains explicit school filter for learner enrollments': 'getEnrollmentsForLearner(learnerId: String, schoolId: String)' in repo and 'school_id = ?' in repo and 'schoolId: String = LEGACY_SCHOOL_ID' not in repo,
}
for label, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' ' + label)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
sys.exit(0 if all(checks.values()) else 1)
