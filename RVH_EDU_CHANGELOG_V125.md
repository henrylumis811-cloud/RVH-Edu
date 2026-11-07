# RVH Edu V125 — School-local lifecycle integrity

## Change
- Extended `AcademicDatabaseIntegrity` to verify every enrollment has a `school_learner_status` row for the exact `(school_id, learner_id)` pair.
- Added lifecycle timestamp validation: `created_at` and `updated_at` must be positive, and `updated_at` cannot precede `created_at`.
- This makes backup export and restore integrity checks detect incomplete or inconsistent lifecycle state instead of silently accepting it.
- Added `tools/check_database_status_integrity_v125.py`, a repeatable SQLite fixture and source-contract check for cross-school status isolation and timestamp invariants.

## Validation limits
The V125 checker is a standalone SQLite fixture plus source-contract checks. It is not an Android instrumentation test, does not invoke the private `SQLiteOpenHelper.onUpgrade` migration, and does not establish that the app compiles. Full Gradle build, real schema-v9 migration, in-app backup/restore, and physical-device testing remain required.
