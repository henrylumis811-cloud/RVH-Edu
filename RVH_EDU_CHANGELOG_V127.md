# RVH Edu V127 — Migration recovery postflight

- Added post-migration verification after the schema-v10 school-local lifecycle backfill.
- Verifies every enrollment has a matching `(school_id, learner_id)` lifecycle status.
- Verifies learners without enrollments retain a `legacy-school` status record.
- Verifies status-history events point to a school present in the migrated database.
- Failed postflight checks throw inside the SQLiteOpenHelper upgrade transaction so the migration is not silently accepted as complete.
- Added `tools/check_migration_recovery_v127.py`, a realistic linked-record SQLite fixture covering learners, two-school enrollments, assessment, mark, and legacy audit history.

Validation is limited to the targeted Python/SQLite fixture and source checks. Android compilation, true Android SQLiteOpenHelper migration from an on-device schema-v9 database, and Tecno Spark 30C verification remain outstanding.
