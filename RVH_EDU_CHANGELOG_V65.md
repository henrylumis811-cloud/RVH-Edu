# RVH Edu V65

## Strict backup row-shape validation

- Backup restore now requires each row to contain the exact column set defined by the installed database schema.
- Unknown columns continue to be rejected, and missing nullable columns are now rejected too. Exported rows include all columns, so silently defaulting an omitted nullable field could otherwise discard historical data.
- The check runs during preflight, before the restore transaction deletes or inserts database rows.
- Existing backup format versions and checksum behavior remain unchanged.

## Validation scope

Source-level regression checks and ZIP integrity are run for this handoff. A full Android Gradle build and device-level backup/restore test are not claimed unless separately completed.
