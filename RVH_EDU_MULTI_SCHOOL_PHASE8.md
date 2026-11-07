# Multi-school integrity — Phase 8 (V109)

## Change
Added explicit school-verified read APIs to `AcademicRepository` for assessment lookup, assessment history, and marks. These APIs require a nonblank school ID and verify the enrollment/assessment relationship against `enrollment.school_id` before exposing academic data.

## Important scope note
These are additive APIs. Existing callers have not all been migrated to them yet, and the existing unscoped APIs remain for compatibility with current code paths and legacy migration. Therefore this phase reduces risk for callers that adopt the new APIs but does not claim complete school isolation.

## Validation
Run `python3 tools/check_multi_school_v109.py`. These are source-contract checks only. Android compilation, database integration tests, and device testing remain required.

## Next
Migrate `AcademicDataService` and report/export paths to school-verified APIs, then add school-aware write operations with transactional ownership checks. Admission-number uniqueness and historical backup/restore fixtures remain separate open work.
