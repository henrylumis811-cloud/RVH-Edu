from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt').read_text()
matcher = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerDuplicateMatcher.kt').read_text()
checks = {
    'repository uses shared canonical admission normalization': 'LearnerDuplicateMatcher.normalizeAdmission(learner.admissionNumber)' in repo,
    'duplicate scan is scoped to school and canonical admission value': 'school_id = ? AND normalized_admission_number = ? AND learner_id <> ?' in repo,
    'canonical normalized value is persisted': 'put("normalized_admission_number", cleanAdmission.uppercase(java.util.Locale.ROOT))' in repo,
    'self record excluded during update': 'learner_id <> ?' in repo,
    'transaction begins before duplicate scan': repo.index('db.beginTransaction()') < repo.index('db.query(', repo.index('db.beginTransaction()')),
    'transaction commits only after learner write': repo.index('db.setTransactionSuccessful()') > repo.index('db.insertOrThrow("learner"'),
    'transaction always ends': 'finally {\n            db.endTransaction()' in repo,
    'canonical normalization handles whitespace and case': 'normalizeAdmission(value: String?)' in matcher and 'normalizeName(it).uppercase(Locale.ROOT)' in matcher,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print('ATOMIC_NORMALIZED_ADMISSION_UNIQUENESS_OK')
