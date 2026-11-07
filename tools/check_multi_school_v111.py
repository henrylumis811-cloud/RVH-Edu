from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
checks = {
    'school-aware assessment upsert exists': 'fun upsertAssessmentForSchool' in repo,
    'school-aware draft mark replacement exists': 'fun replaceDraftMarksForSchool' in repo,
    'school-aware finalize exists': 'fun replaceMarksAndFinalizeForSchool' in repo,
    'school-aware status update exists': 'fun setAssessmentStatusForSchool' in repo,
    'writes verify assessment school ownership': 'requireAssessmentBelongsToSchool(database, assessmentId, schoolId)' in repo,
    'service uses school-aware assessment upsert': 'academicRepository.upsertAssessmentForSchool' in service,
    'service uses school-aware draft writes': 'academicRepository.replaceDraftMarksForSchool' in service,
    'service uses school-aware finalization': 'academicRepository.replaceMarksAndFinalizeForSchool' in service,
    'service uses school-aware status changes': 'academicRepository.setAssessmentStatusForSchool' in service,
}
for name, ok in checks.items():
    print(f"{'PASS' if ok else 'FAIL'}: {name}")
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)}/{len(checks)} checks passed')
