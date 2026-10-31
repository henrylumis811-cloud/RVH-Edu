# RVH Edu V124 — Current-Backup Lifecycle Coverage

## Change
- Restore preflight now requires a school-local lifecycle-status row for every enrollment when the backup declares the current schema (v10).
- The required key is the composite `(school_id, learner_id)`, so a status for the same learner in a different school does not satisfy the check.
- Automatic status-row backfill is now restricted to pre-v10 backups, where the status table or its complete school-local coverage may legitimately be absent.
- The check runs before opening the destructive restore transaction. Existing checksum verification, schema validation, transactional restore, and post-restore integrity checks remain in place.

## Why
A current-schema backup with a missing status row could previously pass preflight and be silently repaired from the legacy global `learner.status` field. That compatibility repair could conceal incomplete data and lose a school-specific active/archived distinction. V124 rejects that incomplete current-schema backup instead.

## Validation
Run `python3 tools/check_backup_lifecycle_coverage_v124.py`.

These are source-contract checks and a small independent key-coverage fixture. They are not Android instrumentation tests. A Gradle build, real v9→v10 migration test, in-app backup/restore round trip, and Tecno Spark 30C validation remain outstanding.
