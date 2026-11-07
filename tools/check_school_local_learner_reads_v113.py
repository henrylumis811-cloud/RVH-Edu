from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt'
s = repo.read_text()
checks = {
    'school admission lookup requires explicit school id': 'findByAdmissionNumberInSchool(admissionNumber: String, schoolId: String)' in s and 'require(schoolId.isNotBlank())' in s,
    'admission lookup uses explicit school-local assignment': 'school_admission_number san JOIN learner l ON l.id = san.learner_id' in s and 'san.school_id = ?' in s,
    'enrolled roster is distinct per school with school-local admission display': 'getEnrolledLearnersForSchool(schoolId: String)' in s and 'SELECT DISTINCT l.id, COALESCE(san.admission_number, l.admission_number)' in s and 'WHERE e.school_id = ?' in s,
    'existing global lookup retained for compatibility': 'fun findByAdmissionNumber(admissionNumber: String)' in s,
    'admission numbers are written to school-local table': 'school_admission_number' in s and 'normalized_admission_number' in s,
    'learner persistence transaction retained': 'db.beginTransaction()' in s and 'db.setTransactionSuccessful()' in s,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'{len(checks)-len(failed)}/{len(checks)} checks passed')
raise SystemExit(bool(failed))
