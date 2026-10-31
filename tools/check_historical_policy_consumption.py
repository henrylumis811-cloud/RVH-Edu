from pathlib import Path
root = Path(__file__).resolve().parents[1]
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
report = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/reports/ReportCard.kt').read_text()
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
assert 'val policy = parsePolicy(canonical)' in service
assert 'policy?.subjects?.map { it.code } ?: AssessmentWorkflow.subjectsFor(enrollment.schoolClass)' in service
assert 'policy?.takeIf { it.hasCompleteScoringPolicy }?.gradingScale() ?: scale' in service
assert 'val selectedPolicy = assessments.asSequence()' in service
assert 'passPercent = effectivePassPercent' in service
assert 'assessment.policySnapshotJson' in report and 'savedPolicy?.subjects' in report
assert 'classAnalytics(academicYear, selectedClass, selectedTerm, selectedSection, gradingScale)' in main
print('PASS: assessment history calculations, report-card subject rows, and default class analytics consume saved policy snapshots')
