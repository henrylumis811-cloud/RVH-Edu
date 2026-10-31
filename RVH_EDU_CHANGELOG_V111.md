# RVH Edu V111 — School-aware assessment writes

## Changes
- Added school-aware assessment upsert with enrollment ownership validation.
- Added school-aware draft-mark replacement and finalization entry points.
- Added school-aware assessment status changes.
- Updated `AcademicDataService` to use these entry points for assessment creation, mark saves, finalization, and reopening.
- Ownership checks are performed within a SQLite transaction around the write operation.

## Validation
- Run `python3 tools/check_multi_school_v111.py` for source-contract checks.
- ZIP integrity is checked at packaging time.
- Android compilation, instrumentation/database integration, upgrade migration, and physical-device tests were not run for this handoff.

## Remaining work
This release improves the service's write paths but does not establish complete application-wide multi-school isolation. Audit remaining direct repository callers, report/export flows, school-local admission-number uniqueness, and fixture-based backup/restore and migration behavior before treating the feature as production-complete.
