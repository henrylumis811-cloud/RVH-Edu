# RVH Edu V50

## Learner archive/restore transaction safety

- Wrapped archive and restore status changes in SQLite transactions so each state transition is atomic.
- Validate that learner status is one of the supported states before changing it; unknown/corrupt status values now require review rather than being overwritten.
- Made repeated archive/restore requests idempotent: if a learner is already in the requested state, the app does not perform an unnecessary update or bump the updated timestamp.
- Preserve enrolments, assessments, marks, reports, and historical records; no delete operation is used.
- Added a source regression check for transaction boundaries, status validation, idempotency, and history preservation.

## Validation boundary

These are source-level checks only. A full Android Gradle build and physical-device test were not performed for this handoff.
