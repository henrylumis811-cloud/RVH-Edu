# RVH Edu V64

## Backup integrity checksum

- New backups use backup format version 2 and include a SHA-256 digest over canonicalized table data.
- Import validates the digest before semantic row validation and before opening the destructive restore transaction.
- Canonical JSON sorts object keys, so harmless JSON whitespace and object-key ordering changes do not invalidate the checksum.
- Version 1 backups remain importable for backward compatibility; they are explicitly not checksum-verified.
- A checksum mismatch blocks restoration before the existing database is modified.

## Validation scope

Source-level regression checks and ZIP integrity are run for this handoff. A full Android Gradle build and device-level backup/restore test are not claimed unless separately completed.
