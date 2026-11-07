# RVH Edu V97 — Multi-school database foundation

- Added schema v7 tables for teacher identities, time-bounded school memberships, and teaching assignments.
- Added `school_id` to enrollments with a stable legacy-school default.
- Added an idempotent migration that preserves existing academic IDs and assigns legacy enrollments to one school.
- Added indexes and foreign-key/check constraints for school memberships and teaching assignments.
- Documented the remaining school-scoped repository and uniqueness work; no school-switcher UI is claimed yet.

Validation performed: source-level assertions and ZIP integrity only. A full Android build and migration test against a real copied database remain outstanding.
