# RVH Edu V16 — Make incomplete subject coverage visible

## Existing components inspected
- `AcademicAnalytics.subjectAnalytics`
- `ClassAnalyticsCalculator.summarize`
- `ClassAnalyticsScreen`
- `AcademicDataService.classAnalytics`

V15 already aligned the configured pass threshold between class and subject analytics. V16 extends those same components; it does not add a duplicate analytics system.

## Problem addressed
Subject averages and pass rates correctly used only marks that had actually been recorded, but the subject card did not show how many learners had no mark for that subject. A high pass rate could therefore look more representative of the class than it was.

## Change
- Subject analytics now expose assessed count, missing count and class coverage percentage.
- The class calculator supplies the whole class size as the denominator for coverage.
- Average and pass rate remain explicitly calculated among assessed learners only. Missing marks are never converted to zero and do not silently count as failures.
- The existing subject analytics UI now shows assessed, missing, coverage, average and pass rate among assessed learners.
- Regression tests cover a two-learner class where only one learner has a subject mark.

## Verification
- Targeted Kotlin compilation of the model + analytics code and regression test: passed.
- `SubjectPassThresholdRegression.kt`: passed (`SUBJECT_PASS_THRESHOLD_REGRESSION_OK`).
- ZIP integrity: checked after packaging.
- Full Android build and phone testing: not performed.
