from pathlib import Path
root = Path(__file__).resolve().parents[1]
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt').read_text()
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/ClassProgressionScreen.kt').read_text()
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
assert 'fun progressClass(' in service
assert 'targetYear > sourceYear' in service
assert 'learner.status == "ACTIVE"' in service
assert 'academicRepository.insertEnrollmentsAtomically(' in service and 'expectedSourceEnrollmentIds = sourceEnrollmentIds' in service
assert 'fun insertEnrollmentsAtomically(' in repo and 'expectedSourceEnrollmentIds: Map<String, String>? = null' in repo
assert 'database.beginTransaction()' in repo and 'database.setTransactionSuccessful()' in repo
assert 'database.endTransaction()' in repo
assert 'learner_id = ? AND academic_year = ?' in repo
assert 'insertOrThrow("enrollment"' in repo
assert 'fun ClassProgressionScreen(' in screen
assert 'Select all' in screen and 'Clear selection' in screen
assert 'Existing academic records will be preserved' in screen
assert 'Screen.CLASS_PROGRESSION -> ClassProgressionScreen(' in main
assert 'label = { Text(Screen.CLASS_PROGRESSION.label) }' in main
# Rollover only creates new enrollments; it must not mutate source marks or source enrollment rows.
method = service.split('fun progressClass(', 1)[1].split('fun findAssessment(', 1)[0]
assert 'delete(' not in method.lower()
assert 'replaceMarks' not in method
print('CLASS_PROGRESSION_INTEGRITY_OK')
