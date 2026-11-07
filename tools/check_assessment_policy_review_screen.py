from pathlib import Path
root = Path(__file__).resolve().parents[1]
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/AssessmentPolicyReviewScreen.kt').read_text()
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
nav = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/Screen.kt').read_text()
for text, token in [(service, 'fun policyReview('), (service, 'INVALID POLICY'), (service, 'CONFLICTING POLICY'), (service, 'MIXED POLICY SET'), (screen, 'Assessment Policy Review'), (screen, 'does not alter marks or historical policies'), (main, 'Screen.POLICY_REVIEW -> AssessmentPolicyReviewScreen'), (nav, 'POLICY_REVIEW("Assessment Policy Review")')]:
    assert token in text, f'Missing policy review integration: {token}'
assert 'replaceMarks' not in screen and 'upsertAssessment' not in screen
print('PASS: policy review diagnostics, navigation, and read-only screen are wired')
