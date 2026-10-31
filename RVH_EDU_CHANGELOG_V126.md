# RVH Edu V126 — Backup lifecycle timestamp preflight

## Change
- Added explicit `updated_at >= created_at` validation for `school_learner_status` rows in `AcademicDatabaseBackupManager.validateDomainRows`.
- Invalid lifecycle timestamp ordering is rejected during preflight, before opening the destructive restore transaction.
- Retained `AcademicDatabaseIntegrity.requireHealthy(db)` after restore as a second line of defence.
- Added a repeatable SQLite/source-contract regression checker.

## Validation scope
The checker verifies the preflight contract and exercises valid/invalid timestamp ordering in SQLite. It is not an Android instrumentation test, does not execute `SQLiteOpenHelper.onUpgrade`, and does not prove the application compiles. Full Gradle build, real schema migration, in-app backup/restore, and physical-device testing remain required.
