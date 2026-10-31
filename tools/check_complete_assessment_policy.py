from pathlib import Path
root = Path(__file__).resolve().parents[1]
snapshot = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
assert 'CURRENT_SCHEMA_VERSION = 2' in snapshot
assert 'GradingScaleRepository.load(context)' in snapshot
assert 'SchoolInfoRepository.loadClassPassPercent(context)' in snapshot
assert 'aggregateBands' in snapshot and 'divisionBands' in snapshot
assert 'version in 1..CURRENT_SCHEMA_VERSION' in snapshot
assert 'snapshot?.hasCompleteScoringPolicy == true' in service
assert 'snapshot.gradingScale()' in service
assert 'subjectCodes = expectedSubjects' in service
assert 'policySnapshotJson = AssessmentPolicySnapshot.capture(appContext, enrollment.schoolClass).toJson()' in service
print('PASS: new assessments pin subjects, grading bands, divisions, and pass threshold; scoring uses pinned bands')
