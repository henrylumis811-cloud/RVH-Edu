# RVH Edu — Multi-school boundary pass (V112)

## Scope
V112 adds a repeatable cross-layer source guard for the assessment service and repository school boundary. It checks that the service routes assessment and mark reads/writes through school-aware entry points and that repository code retains enrollment/assessment ownership checks, transactional write structure, and the school-scoped mark query join.

## What this establishes
These checks are useful regression guards for source structure. They do not prove database correctness, concurrency safety, complete coverage of every report/export entry point, or runtime behavior on Android.

## Remaining multi-school work
- Audit report/export and UI paths beyond `AcademicDataService` for direct unscoped repository calls.
- Design and test school-local admission-number uniqueness without corrupting shared learner identities or legacy data.
- Add fixture-based SQLite migration and backup/restore round-trip tests.
- Run Android compilation, integration tests, and device tests before calling the boundary complete.

No records are moved, merged, or deleted by this increment.
