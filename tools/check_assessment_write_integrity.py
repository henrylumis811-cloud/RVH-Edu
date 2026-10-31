#!/usr/bin/env python3
"""Static guardrails for RVH Edu's assessment write boundary (not Android integration tests)."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
repo = (root / "app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt").read_text()
service = (root / "app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt").read_text()
checks = {
    "draft save checks persisted state inside transaction": "fun replaceDraftMarks" in repo and "requirePersistedDraft(database, assessmentId)" in repo,
    "finalization writes marks and status together": "fun replaceMarksAndFinalize" in repo and "replaceMarksInsideTransaction(database, assessmentId, marks)" in repo,
    "finalization conditional on persisted draft state": '"id = ? AND status = ?"' in repo,
    "transaction is marked successful only after writes": repo.index("database.setTransactionSuccessful()", repo.index("fun replaceMarksAndFinalize")) > repo.index("val changed = database.update", repo.index("fun replaceMarksAndFinalize")),
    "service re-resolves assessment within active school": "getAssessmentForSchool(enrollment.id, assessment.term, assessment.section, activeSchoolId())" in service,
    "service no longer calls unguarded replaceMarks": "academicRepository.replaceMarks(" not in service,
    "draft writes use school-guarded repository entry point": "academicRepository.replaceDraftMarksForSchool(assessment.id, existing, activeSchoolId())" in service,
    "final writes use school-guarded atomic repository entry point": "academicRepository.replaceMarksAndFinalizeForSchool(assessment.id, marks, activeSchoolId())" in service,
}
for name, ok in checks.items():
    print(("PASS" if ok else "FAIL") + " — " + name)
if not all(checks.values()):
    raise SystemExit(1)
print("ASSESSMENT_WRITE_INTEGRITY_STATIC_CHECKS_OK")
