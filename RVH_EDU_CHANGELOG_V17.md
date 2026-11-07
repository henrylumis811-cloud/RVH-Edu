# RVH Edu V17 — Complete Subject Coverage

## Existing components inspected
- `SubjectCatalog` already defines the default expected subjects per class.
- `AcademicAnalytics.subjectAnalytics` already calculates per-subject statistics.
- `ClassAnalyticsCalculator` already aggregates class-level analytics.
- `AcademicDataService.classAnalytics` already knows the selected class and retrieves the current roster's marks.
- `ClassAnalyticsScreen` already displays subject-performance cards.

No duplicate subject catalog, analytics calculator, service, or screen was introduced.

## Changes
- Pass the existing `SubjectCatalog` subjects into the existing class analytics calculation.
- Show expected subjects even when no learner has a mark recorded for them.
- Represent average, high, low, and pass rate as unavailable (`null`) when no marks exist instead of displaying fabricated zero performance or producing `NaN` for a zero-denominator pass rate.
- Display “No marks yet” / “Not available” in the existing analytics screen for wholly unassessed subjects.
- Reject duplicate subject marks inside an individual learner's analytics mark list, avoiding inflated coverage and misleading counts.
- Extend the existing subject analytics regression test for a subject with zero marks and duplicate marks.

## Verification
- Targeted pure-Kotlin compilation: passed.
- `SubjectPassThresholdRegression.kt`: passed (`SUBJECT_PASS_THRESHOLD_REGRESSION_OK`).
- Full Android build/device test: not run in this environment.
