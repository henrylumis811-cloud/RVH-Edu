# RVH Edu V161 — Grading and analytics edge-case tests

Version: 1.4.1 (build 127)

## Changes
- Added tests for percentage normalization when assessments use maximum marks other than 100.
- Added tests that catalogued subjects remain visible when no marks have been entered, with null AMGP/average and zero coverage.
- Added grading-scale validation tests for duplicate mark thresholds and division labels that differ only by case/whitespace.
- Production grading calculations and persisted data formats are unchanged.

## Verification
- Reviewed the added test cases against the current analytics and grading-scale implementations.
- Source assertions and ZIP integrity checks performed.
- Gradle compilation, JUnit execution, and Android device tests were not run in this environment.
