from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ui = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/LearnerManagementScreen.kt").read_text()
activity = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt").read_text()
service = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt").read_text()
matcher = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerDuplicateMatcher.kt").read_text()

assert "onFindDuplicates: (String, Int?, String?, String?) -> List<PossibleLearnerDuplicate>" in ui
assert "onFindDuplicates = onFindDuplicates" in ui
assert "Possible existing learner" in ui
assert "Different learner — continue" in ui
assert "No existing record will be merged or changed" in ui
assert "pendingReview" in ui and "candidates.take(6)" in ui
assert "academicService.findPossibleLearnerDuplicates(name, age, admissionNumber, excludingId)" in activity
assert "fun findPossibleLearnerDuplicates(" in service
assert "learnerRepository.findPossibleDuplicates(proposed, activeSchoolId())" in service
assert "LearnerDuplicateMatcher.findCandidates(proposed, getEnrolledLearnersForSchool(schoolId))" in (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt").read_text()
assert "it.id != proposed.id" in matcher
print("Duplicate review UI wiring checks passed")
