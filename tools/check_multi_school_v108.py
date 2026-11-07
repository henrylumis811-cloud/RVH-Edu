#!/usr/bin/env python3
"""Source-contract checks for RVH Edu V108 assessment school-boundary review."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REPO = ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt'
SERVICE = ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt'
repo = REPO.read_text()
service = SERVICE.read_text()
checks = {
    'assessment lookup uses enrollment ID': 'fun getAssessment(enrollmentId: String' in repo and '"enrollment_id = ? AND term = ? AND section = ?"' in repo,
    'assessment history lookup uses enrollment ID': 'fun getAssessmentsForEnrollment(enrollmentId: String)' in repo and '"enrollment_id = ?"' in repo,
    'marks lookup is linked to assessment ID': 'fun getMarks(assessmentId: String)' in repo and '"assessment_id = ?"' in repo,
    'mark replacement rejects foreign assessment IDs': 'require(mark.assessmentId == assessmentId)' in repo,
    'draft replacement checks persisted status': 'requirePersistedDraft(database, assessmentId)' in repo,
    'finalize update is conditional on draft status': '"id = ? AND status = ?"' in repo and 'AssessmentStatus.DRAFT.name' in repo,
    'service validates assessment-enrollment pairing before final save': 'persisted.id == assessment.id && assessment.enrollmentId == enrollment.id' in service,
    'service validates assessment-enrollment pairing before draft save': service.count('persisted.id == assessment.id && assessment.enrollmentId == enrollment.id') >= 3,
    'active school context exists in service': 'private fun activeSchoolId(): String = SchoolManagementRepository.activeId(appContext)' in service,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'RESULT: {len(checks)-len(failed)}/{len(checks)} checks passed')
raise SystemExit(bool(failed))
