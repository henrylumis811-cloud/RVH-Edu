from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
repo = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt').read_text()
checks = {
    'repository normalizes learner names and whitespace': 'fullName.trim().replace(Regex("\\\\s+"), " ")' in repo,
    'repository uses shared locale-independent admission normalization': 'LearnerDuplicateMatcher.normalizeAdmission(learner.admissionNumber)' in repo,
    'repository rejects blank learner IDs': 'require(cleanId.isNotEmpty())' in repo,
    'repository rejects blank learner names': 'require(cleanName.isNotEmpty())' in repo,
    'repository validates age range': 'learner.ageYears == null || learner.ageYears in 2..30' in repo,
    'repository rejects unsupported statuses': 'cleanStatus == "ACTIVE" || cleanStatus == "ARCHIVED"' in repo,
    'repository checks normalized admission duplicates excluding same learner': 'school_id = ? AND normalized_admission_number = ? AND learner_id <> ?' in repo,
    'repository stores normalized admission numbers': 'put("admission_number", cleanAdmission)' in repo and 'put("normalized_admission_number", cleanAdmission.uppercase(java.util.Locale.ROOT))' in repo,
}
for label, passed in checks.items():
    print(('PASS' if passed else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print('LEARNER_REPOSITORY_IDENTITY_VALIDATION_OK')
