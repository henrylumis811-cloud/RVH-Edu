# RVH Edu V166 — Backup/restore responsiveness

Version: 1.4.6  
Build: 132

## Changes
- Run backup export and database restore work on `Dispatchers.IO` rather than the Compose UI thread.
- Show a clear progress status during backup and restore.
- Disable export/import and restore confirmation controls while a backup operation is active to reduce accidental duplicate operations.
- Keep success/failure feedback and refresh academic data only after a successful restore.

## Verification
- Source-level assertions and ZIP integrity checks only.
- Gradle compilation, automated tests, and device testing were not run in this environment.
