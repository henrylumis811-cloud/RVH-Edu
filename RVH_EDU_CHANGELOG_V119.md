# RVH Edu V119 — Backup and Recovery Validation Hardening

## Scope
V119 strengthens semantic preflight validation for database backups before the restore transaction can delete or replace any installed rows.

## Changes
- Rejects enrollment rows whose `school_id` does not refer to a school included in the backup.
- Validates school-local learner lifecycle rows for existing school and learner references, supported status values, valid timestamps, and duplicate `(school_id, learner_id)` keys.
- Validates school references on learner lifecycle audit rows.
- Adds compatibility normalization for legacy lifecycle-history entries that predate `school_id`; those records are retained and assigned to `legacy-school`, without guessing a school attribution.
- Keeps restore transactional and preserves the existing post-restore school-status backfill.

## Safety notes
- Backup checksum validation still occurs before compatibility normalization for versioned backups, so normalization does not conceal changes to the source backup payload.
- The installed database is not changed by semantic preflight failures, because validation occurs before the destructive restore transaction.
- These source checks do not replace a full Android build, SQLite migration test, or physical-device restore test.
