# RVH Edu V150 — Database Query Indexes

- Version: 1.3.0 (versionCode 116).
- Database schema version: 11.
- Added an index supporting school-scoped learner-status joins by learner ID and school ID.
- Added a composite index supporting latest archive-reason lookups by school, archive status, and descending event time/ID.
- Added the same indexes to fresh database creation and an additive upgrade migration for existing installations.
- This migration changes indexes only; it does not rewrite or delete learner, enrollment, assessment, mark, or history records.

## Verification status

- Static source checks and ZIP integrity can be performed locally.
- Android compilation, Gradle tests, SQLite migration execution on Android, and physical-device performance profiling have not been run as part of this source preparation.
