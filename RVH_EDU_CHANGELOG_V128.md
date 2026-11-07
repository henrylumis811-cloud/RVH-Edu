# RVH Edu V128 — Unenrolled Learner Lifecycle Coverage

- Extended current-schema backup preflight so every learner must carry at least one school-local status record, including learners created before enrollment. Current backups can no longer silently omit lifecycle state for those learners.
- Extended the database integrity audit with the same invariant, so backup export and post-restore validation detect learners that have no lifecycle status record in any school.
- Kept enrolled learners school-scoped: every enrollment still requires its exact `(school_id, learner_id)` status, and distinct schools may retain different states for one shared learner identity.
- Added `tools/check_unenrolled_lifecycle_coverage_v128.py`, combining source-contract checks with a SQLite fixture for missing status, repair, and independent multi-school statuses.

Validation is limited to the listed Python/SQLite checks and selected source regression checks. Android compilation, Android SQLiteOpenHelper migration from a real schema-v9 database, app-level backup/restore, and Tecno Spark 30C verification remain outstanding.
