# RVH Edu V110 — Active-School Assessment Read Migration

## Implemented
- Migrated `AcademicDataService` assessment lookups to `getAssessmentForSchool(...)` using the currently selected school.
- Migrated assessment-history listing to `getAssessmentsForEnrollmentInSchool(...)`.
- Migrated service-level mark reads to `getMarksForSchool(...)`, including mark loading, calculation, class analytics, and assessment history.
- Added a source contract checker at `tools/check_multi_school_v110.py`.

## Important limits
This increment hardens read paths in `AcademicDataService`; it does not complete multi-school isolation. Assessment creation, mark writes/finalization, status changes, global learner identity/admission-number uniqueness, and all report/export paths still need a complete audit and scoped transactional enforcement. Existing data is not moved or merged.

## Validation
The V110 source-contract checker is intended as a fast regression guard. Android compilation, SQLite integration tests, migration/backup round-trip tests, and physical-device validation have not been run as part of this handoff.
