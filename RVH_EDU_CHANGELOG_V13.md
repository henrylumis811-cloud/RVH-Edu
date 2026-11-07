# RVH Edu V13 — Backup/Restore Integrity

## Existing components inspected

The active Backup & Restore screen already uses `AcademicDatabaseBackupManager` for the authoritative SQLite database. The older `data.BackupManager` and `GradebookRepository` remain in the source because the legacy gradebook migration reads those records; V13 does not introduce a second backup UI or remove migration compatibility code.

## Changes

- Validate backup schema version exactly before restore. The previous `<= current` check implied older schemas were accepted even though missing columns were not migrated safely.
- Validate required table arrays, table set, export timestamp, row object structure, column names, required fields, numeric types, finite floating-point values and a maximum row count before deleting current data.
- Preserve SQL defaults for omitted nullable/defaulted columns by only supplying fields present in a row.
- Encode binary values with Base64 rather than lossy text conversion.
- Ensure the SQLite helper is closed if export, validation or restore fails.
- Retain the transaction and post-restore database integrity audit so a failed restore rolls back.
- Extend the existing integrity policy regression to guard these behaviours.

## Validation limits

Static policy checks and ZIP integrity can be run here. Full Android compilation and on-device backup/restore testing are not claimed in this handoff.
