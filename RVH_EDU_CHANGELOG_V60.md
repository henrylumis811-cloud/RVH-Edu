# RVH Edu V60 — Safer Backup Import and Export

- Added a 64 MiB bound to generated and imported JSON backups.
- Replaced unbounded backup file reads with a bounded streaming read, so oversized files are rejected before JSON parsing or database changes.
- Backup export is now generated before opening the chosen destination, preventing a generation error from truncating the destination file.
- Added an explicit required-table presence check before restore begins.
- Existing transaction-based restore and post-restore integrity checks remain in place.

Validation performed: source-level regression scripts and ZIP integrity checks. A full Android Gradle build and device backup/restore test were not available for this checkpoint.
