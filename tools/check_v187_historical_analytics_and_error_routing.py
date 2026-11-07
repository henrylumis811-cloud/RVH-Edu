#!/usr/bin/env python3
"""Regression contracts for V187 historical analytics, exports, and error routing."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
service = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
snapshot = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
activity = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
tests = (ROOT / 'app/src/test/java/com/henrylumis/rvhgrader/core/assessment/HistoricalAssessmentPolicyTest.kt').read_text()
checks = {
    'class analytics selects immutable saved grading scale': 'analyticsGradingScale(selectedPolicy, scale)' in service and 'gradingScale = effectiveGradingScale' in service,
    'legacy policy fallback remains explicit': 'policy?.takeIf { it.hasCompleteScoringPolicy }?.gradingScale() ?: currentScale' in snapshot,
    'unassessed rows are excluded while persisted legacy rows remain represented': 'assessments.filterNotNull().map { it.policySnapshotJson }' in snapshot,
    'all class export call sites use persisted assessment filtering': activity.count('policySnapshotsForExistingAssessments(results.mapNotNull') == 4,
    'policy conflicts have a dedicated exception': 'class AssessmentPolicyCompatibilityException' in service and service.count('throw AssessmentPolicyCompatibilityException(policyError)') == 2,
    'only policy conflicts route to policy review': activity.count('catch (e: AssessmentPolicyCompatibilityException)') == 2 and 'COULD NOT LOAD:' in activity,
    'transient operation banner expires': 'LaunchedEffect(importStatus)' in activity and 'delay(8_000)' in activity,
    'regression tests cover saved scale and unassessed learners': 'completeSavedPolicyOverridesLaterCurrentGradingScaleForAnalytics' in tests and 'absentAssessmentIsExcludedButActualLegacyAssessmentRemainsAsNullPolicy' in tests,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'V187 HISTORICAL REGRESSION CONTRACTS PASSED: {len(checks)}/{len(checks)}')
