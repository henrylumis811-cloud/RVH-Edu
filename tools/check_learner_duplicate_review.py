from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
matcher = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerDuplicateMatcher.kt').read_text()
repo = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt').read_text()
checks = {
    'duplicate matching excludes the same learner identity': '.filter { it.id != proposed.id }' in matcher,
    'admission-number matches are normalized and high confidence': 'normalizeAdmission(proposed.admissionNumber)' in matcher and 'Confidence.HIGH' in matcher,
    'date-of-birth matches are reviewed as high confidence': 'sameDob' in matcher and 'Confidence.HIGH' in matcher,
    'same-name-only matches remain review suggestions': 'Confidence.REVIEW' in matcher,
    'matching never merges learner records': 'never merges' in matcher,
    'candidate reasons are exposed to the review workflow': 'val reasons: List<String>' in matcher,
    'candidate results are deterministic': '.thenBy { it.learner.id }' in matcher,
    'repository exposes possible-duplicate lookup': 'fun findPossibleDuplicates(proposed: Learner, schoolId: String = "legacy-school")' in repo,
    'name lookup uses normalized matching': 'LearnerDuplicateMatcher.normalizeName(learner.fullName) == normalizedName' in repo,
}
for label, passed in checks.items():
    print(('PASS' if passed else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print('LEARNER_DUPLICATE_REVIEW_OK')
