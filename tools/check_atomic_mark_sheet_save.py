#!/usr/bin/env python3
"""Source-level regression checks for atomic individual mark-sheet draft saves."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
service = (root / "app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt").read_text()
activity = (root / "app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt").read_text()
repo = (root / "app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt").read_text()

start = service.index("fun saveDraftMarkSet(")
end = service.index("fun reopenAssessment(", start)
method = service[start:end]
assert "persisted.id == assessment.id" in method
assert "persisted.status == AssessmentStatus.DRAFT" in method
assert "supplied.size == expected.size && supplied.toSet() == expected" in method
assert "AssessmentWorkflow.toMarks(assessment.id, inputs)" in method
assert "academicRepository.replaceDraftMarksForSchool(assessment.id, marks, activeSchoolId())" in method
assert "saveDraftMarkSet(assessment, entry.enrollment, inputs)" in activity
assert "AssessmentWorkflow.subjectsFor(entry.enrollment.schoolClass).forEach { code ->" not in activity
assert "database.beginTransaction()" in repo[repo.index("fun replaceDraftMarks("):repo.index("fun replaceMarksAndFinalize(")]
print("PASS: individual mark-sheet draft saves validate the full subject set and replace marks atomically.")
