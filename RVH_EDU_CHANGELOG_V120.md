# RVH Edu V120 — Backup Schema Version Alignment

## Why this update exists

The app database schema had advanced to version 10 for school-local learner lifecycle status, while the backup envelope still advertised schema version 9. That mismatch made the backup format's declared schema inaccurate and complicated compatibility decisions.

## Changes

- Advances the backup schema marker to version 10, matching the current app database schema.
- Preview and restore accept compatible backup schemas from version 4 through version 10.
- Treats backups older than schema 10 as eligible for compatibility insertion of a missing `school_learner_status` array. Existing schema-9 backups that already contain that table are preserved as-is.
- Retains checksum verification before restore-time compatibility normalization.
- Retains semantic and installed-schema validation before opening the destructive restore transaction.
- Retains transactional restore, post-restore status backfill, and database integrity validation.

## Validation boundary

`tools/check_backup_recovery_v120.py` is a source-level regression checker. It does not execute Android SQLite migrations or perform a full backup/restore round trip against a real device database. Full Gradle compilation and device-level migration/restore testing remain outstanding.
