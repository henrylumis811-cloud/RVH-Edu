from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
service = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
repo = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt').read_text()
ui = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/LearnerManagementScreen.kt').read_text()
main = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()

checks = {
    'atomic creation method exists': 'fun createAndEnrollLearner(' in service,
    'transaction starts before learner creation': service.index('db.beginTransaction()', service.index('fun createAndEnrollLearner')) < service.index('createLearner(name, ageYears = ageYears, admissionNumber = admissionNumber)', service.index('fun createAndEnrollLearner')),
    'transaction only marked successful after enrolment': service.index('enroll(learner.id, academicYear, schoolClass, stream)', service.index('fun createAndEnrollLearner')) < service.index('db.setTransactionSuccessful()', service.index('fun createAndEnrollLearner')),
    'transaction always ends': 'finally {\n            db.endTransaction()' in service,
    'admission numbers normalized': '?.takeIf { it.isNotEmpty() }?.uppercase()' in service,
    'duplicate admission lookup uses canonical normalization': 'findByAdmissionNumberInSchool(cleanAdmissionNumber, activeSchoolId())' in service and 'school_id = ? AND normalized_admission_number = ?' in repo,
    'add learner dialog accepts optional admission number': 'Admission number (optional)' in ui,
    'screen callback passes admission number': 'onAddLearner(name, age, admissionNumber)' in ui,
    'main screen uses atomic create-and-enroll': 'academicService.createAndEnrollLearner(' in main and 'academicService.createLearner(name, ageYears = age)' not in main,
}
for label, passed in checks.items():
    print(('PASS' if passed else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print('LEARNER_CREATION_INTEGRITY_OK')
