from pathlib import Path
root = Path(__file__).resolve().parents[1]
export = (root / 'app/src/main/java/com/henrylumis/rvhgrader/export/AcademicExportManager.kt').read_text()
report = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/reports/ReportCard.kt').read_text()
pdf = (root / 'app/src/main/java/com/henrylumis/rvhgrader/export/AcademicDocumentExportManager.kt').read_text()
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
analytics = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/analytics/AcademicAnalytics.kt').read_text()
analytics_test = (root / 'app/src/test/java/com/henrylumis/rvhgrader/core/analytics/AcademicAnalyticsTest.kt').read_text()
gradle = (root / 'app/build.gradle.kts').read_text()
workflow = (root / '.github/workflows/android-build.yml').read_text()
checks = {
    'class exports derive subject analytics from the full result roster': 'expectedLearnerCount = results.size' in export and 'results.map { ranked -> marksByLearner[ranked.result.learnerId].orEmpty() }' in export,
    'historical exports use saved grading scale and pass threshold': 'savedPolicy?.takeIf { it.hasCompleteScoringPolicy }?.gradingScale()' in export and 'savedPolicy?.classPassPercent ?: currentPassPercent' in export,
    'CSV exports include subject performance metrics': 'SUBJECT PERFORMANCE ANALYSIS' in export and 'PASS RATE %' in export and 'AMGP' in export,
    'Excel exports include subject performance metrics': 'data += listOf("SUBJECT PERFORMANCE ANALYSIS")' in export and 'subject.distinctionCount' in export,
    'PDF exports include a paginated subject analysis appendix': 'A dedicated appendix' in export and 'SUBJECT PERFORMANCE ANALYSIS' in export and 'Pass threshold:' in export and 'subjectAnalytics.chunked(22)' in export,
    'subject analytics include coverage, pass rate, AMGP and grade distribution': all(x in analytics for x in ['coveragePercent =', 'passRatePercent =', 'amgp =', 'distinctionCount = dCount', 'failureCount = fCount']),
    'unit test covers assessed-only pass rate and custom threshold': 'subjectPassRateUsesOnlyAssessedLearnersAndConfiguredThreshold' in analytics_test and 'passPercent = 60.0' in analytics_test and 'assertEquals(50.0, subject.passRatePercent!!' in analytics_test,
    'report card model stores class position and ranked cohort size': 'val classPosition: Int? = null' in report and 'val rankedLearnerCount: Int? = null' in report,
    'report card builder validates rank against complete cohort': 'result.complete && classPosition in 1..rankedLearnerCount' in report,
    'report card PDF visibly prints class position': 'CLASS POSITION' in pdf and 'report.classPosition' in pdf and 'Not ranked' in pdf,
    'report generation derives position from shared class ranking': 'academicService.classResults(academicYear, selectedClass, selectedTerm, selectedSection, gradingScale)' in main and 'classPosition = classPosition' in main,
    'all class export paths pass current school grading settings': main.count('currentPassPercent = classPassPercent') >= 4 and main.count('currentGradingScale = gradingScale') >= 4,
    'release version and artifact advance without changing app identity': 'versionCode = 160' in gradle and 'versionName = "1.7.4"' in gradle and 'RVHEdu-1.7.4-release.apk' in workflow and 'v1.7.4-build160' in workflow and 'applicationId = "com.henrylumis.rvhedu"' in gradle,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + '  ' + name)
print(f'\n{len(checks)-len(failed)}/{len(checks)} checks passed')
if failed:
    raise SystemExit(1)
