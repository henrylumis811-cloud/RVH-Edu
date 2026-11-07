from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
method = repo[repo.index('fun insertEnrollmentsAtomically('):repo.index('\n    fun getEnrollment(')]
progress = service[service.index('fun progressClass('):service.index('\n    fun findAssessment(')]
checks = {
    'source enrollment identity is passed from the selected roster': 'sourceEnrollmentIds = learnerIds.associateWith' in progress and 'expectedSourceEnrollmentIds = sourceEnrollmentIds' in progress,
    'source year and class are passed for transaction validation': 'sourceYear = sourceYear' in progress and 'sourceClass = sourceClass' in progress,
    'source validation arguments must be supplied together': 'Source enrollment validation requires the source year, class, and enrollment identities together' in method,
    'every selected learner must have a source enrollment identity': 'expectedSourceEnrollmentIds.keys == enrollments.map { it.learnerId }.toSet()' in method,
    'source enrollment is rechecked inside transaction': method.index('database.beginTransaction()') < method.index('val sourceStillMatches = database.query(') < method.index('database.insertOrThrow("enrollment"'),
    'source check matches id learner year and class': 'id = ? AND learner_id = ? AND academic_year = ? AND school_class = ?' in method,
    'stale or missing source enrollment aborts progression': 'source enrollment for learner ${enrollment.learnerId} changed or no longer exists' in method,
    'target insertion remains all-or-nothing': 'database.setTransactionSuccessful()' in method and 'database.endTransaction()' in method,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if failed: raise SystemExit(1)
print('PROGRESSION_SOURCE_ENROLLMENT_REVALIDATION_OK')
