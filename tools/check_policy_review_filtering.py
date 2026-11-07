from pathlib import Path
root = Path(__file__).resolve().parents[1]
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/AssessmentPolicyReviewScreen.kt').read_text()
for token in ['PolicyReviewFilter', 'ALL("All records")', 'NEEDS_REVIEW("Needs review")', 'SAVED("Saved policy")', 'val needsReview = entries.count', 'FilterChip(', 'Next step: compare this assessment', 'does not alter marks or historical policies']:
    assert token in screen, f'Missing policy review workflow element: {token}'
assert 'upsertAssessment' not in screen and 'replaceMarks' not in screen and 'saveMarks(' not in screen
print('PASS: policy review filtering, summary counts, teacher guidance, and read-only boundary')
