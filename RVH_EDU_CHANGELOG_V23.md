# RVH Edu V23 — Enrollment Integrity

## Existing components inspected

- `AcademicDataService.enroll`
- `AcademicDataService.createAndEnrollLearner`
- `AcademicRepository.upsertEnrollment`
- The existing SQLite enrollment uniqueness constraint

## Changes

- Enrollment upsert is transactional and idempotent for an existing learner/year/class combination.
- Repeated enrollment no longer resets the original `enrolled_at` timestamp or replaces the persisted enrollment ID.
- A conflicting stream request is rejected instead of silently moving the learner between streams.
- A previously unspecified stream can be filled in; existing stream values are preserved when the caller omits a stream.
- Archived learners cannot be enrolled until they are restored.
- Existing unique constraint on learner/year/class remains in place.

## Validation limits

The enrollment integrity source-check script passes. ZIP integrity is checked separately. A full Android build and runtime SQLite test have not been performed in this environment.
