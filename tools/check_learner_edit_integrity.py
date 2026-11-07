from pathlib import Path
root = Path(__file__).resolve().parents[1]
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
ui = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/LearnerManagementScreen.kt').read_text()
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
checks = {
    'edit uses existing learner identity': 'fun updateLearnerDetails(' in service and 'learnerRepository.getById(learnerId)' in service,
    'update runs in transaction': 'db.beginTransaction()' in service[service.index('fun updateLearnerDetails('):service.index('/** Archive a learner')],
    'name is normalized and required': 'cleanName.isNotEmpty()' in service and 'Regex("\\\\s+")' in service,
    'age range is validated': 'ageYears in 2..30' in service,
    'admission duplicate allows own current ID only': 'duplicate == null || duplicate.id == learnerId' in service,
    'updated timestamp changes': 'updatedAtMillis = System.currentTimeMillis()' in service,
    'existing roster has edit action': 'Edit details' in ui and 'EditLearnerDialog' in ui and 'onEdit()' in ui,
    'screen calls service updater': 'academicService.updateLearnerDetails(entry.learner.id, name, age, admissionNumber)' in main,
    'existing academic history is not replaced': 'existing.copy(' in service[service.index('fun updateLearnerDetails('):service.index('/** Archive a learner')],
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if failed: raise SystemExit(1)
print('LEARNER_EDIT_INTEGRITY_OK')
