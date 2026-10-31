# RVH Edu V122 — Legacy Backup Shape Compatibility

## Problem addressed

The restore pipeline accepted older backup schema numbers, but its installed-schema preflight required every backup row to contain the exact current column set. That strict rule could reject legitimate older backups whose tables predated columns introduced in later database versions. Older pre-multi-school payloads could also lack the `school` table or `enrollment.school_id`, and schema 5 backups could lack the learner-status history table.

## Changes

- Added a single legacy-payload normalization path shared by backup preview and restore.
- The original checksum is verified before any compatibility normalization mutates the parsed payload.
- Pre-multi-school backups receive a stable `legacy-school` record and missing enrollment school IDs are attributed to that school.
- Backups predating learner-status history receive an empty audit table; existing history rows missing `school_id` are preserved and assigned to `legacy-school`.
- Missing school-local lifecycle rows are still backfilled after restoration.
- The installed-schema preflight remains strict for current schema-v10 backups. Older schema payloads may omit only columns that are nullable or have a safe SQLite default; unknown columns and omitted required fields without defaults are rejected before the destructive transaction.
- Updated older backup-recovery checkers to assert the normalized compatibility path.
- Added `tools/check_legacy_backup_compatibility_v122.py`, combining source-contract checks with a real SQLite fixture that verifies defaults and preservation of learner, enrollment, assessment, mark, and audit IDs/data.

## Validation

- V122 compatibility checks: 20/20 passed.
- V121 migration/round-trip fixture: 14/14 passed.
- V120 backup/recovery checks: 14/14 passed.
- V119 backup/recovery checks: 14/14 passed.
- V118 lifecycle checks: 10/10 passed.
- V117 multi-school checks: 13/13 passed.
- V115 multi-school checks: 15/15 passed.
- V114 roster isolation: 8/8 passed.
- V113 school-local learner reads: 6/6 passed.
- V112 school-boundary checks: 10/10 passed.
- V110 multi-school checks: 8/8 passed.
- V105, V104, V102 and school-management UI checks passed.
- Learner status audit checks passed.

## Limitations

These checks are source-contract tests and isolated SQLite fixtures. They do not invoke Android `SQLiteOpenHelper`, execute a real content-resolver backup file, or prove that a legacy export restores through the running app. Android Gradle compilation, real schema-v9 upgrade, backup/restore through the app, and Tecno Spark 30C verification remain release gates.
