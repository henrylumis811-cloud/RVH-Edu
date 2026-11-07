# RVH Edu V170 — Backup Regression Coverage Expansion

Version: 1.5.0  
Build: 136

## Changes
- Expanded the CI backup/restore regression runner from 7 to 19 maintained checks.
- Added checksum/versioning, canonical JSON, learner status-history, historical integrity policy, lifecycle coverage/timestamp, extreme-number, preview, unique relationship, and learner archive/restore atomicity checks to the standard suite.
- Updated older source-contract tests to follow the current shared compatibility and structured-key helpers rather than obsolete inline implementations.
- Kept the scope limited to test harness and validation assertions; no database schema or restore transaction behavior was changed.

## Verification
- Host-side regression suite is expected to run with Python 3 and SQLite.
- This is not a substitute for Android Gradle/JUnit execution or device testing.
