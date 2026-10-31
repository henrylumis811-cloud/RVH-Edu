# RVH Edu V15 — Consistent subject pass thresholds

## Existing components inspected
- `AcademicAnalytics.subjectAnalytics`
- `ClassAnalyticsCalculator.summarize`

These were already the project's analytics implementation; V15 extends them and does not introduce a second analytics calculator.

## Change
Subject pass rates previously compared every mark against a hard-coded 50%, even when the class summary was called with a different `passPercent`. V15 passes the configured threshold through to subject analytics and validates that the threshold is finite and within 0–100% at both public calculation boundaries.

## Regression coverage
`tools/pure-tests/SubjectPassThresholdRegression.kt` checks default behaviour, a 40% configured threshold, agreement between class and subject pass rates, and invalid threshold rejection.

## Verification limitations
The pure analytics regression is compiled and executed independently of Android. This does not establish that the complete Android application builds or runs on a device.
