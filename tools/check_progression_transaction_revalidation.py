from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt').read_text()
method = repo[repo.index('fun insertEnrollmentsAtomically('):repo.index('\n    fun getEnrollment(')]
checks = {
    'progression batch rejects blank learner identities': 'enrollments.all { it.learnerId.isNotBlank() }' in method,
    'progression batch validates target year range': 'enrollments.all { it.academicYear in 2000..2100 }' in method,
    'active status is re-read inside transaction': method.index('database.beginTransaction()') < method.index('val learnerStatus = database.query(') < method.index('database.insertOrThrow("enrollment"'),
    'missing learner blocks progression': 'Learner ${enrollment.learnerId} no longer exists' in method,
    'archived or unknown status blocks progression': 'learnerStatus == "ACTIVE"' in method and 'unsupported status' in method,
    'existing target-year enrollment still blocks duplicates': 'already has an enrollment for ${enrollment.academicYear}' in method,
    'batch remains atomic on failure': 'database.setTransactionSuccessful()' in method and 'database.endTransaction()' in method,
    'duplicate learner batch guard remains': 'enrollments.map { it.learnerId }.distinct().size == enrollments.size' in method,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if failed: raise SystemExit(1)
print('PROGRESSION_TRANSACTION_REVALIDATION_OK')
