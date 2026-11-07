# RVH Edu V171 — Android Backup/Restore Integration Tests

Version: 1.5.1 (versionCode 137)

## Changes
- Added Android instrumentation tests that call the production `AcademicDatabaseBackupManager` against Android SQLite.
- Verifies a checksum-tampered backup is rejected before existing school rows are changed.
- Verifies a valid generated backup can be restored and the resulting database passes `AcademicDatabaseIntegrity`.
- Configured the Android instrumentation runner and required test dependencies.
- Updated GitHub Actions to run `connectedDebugAndroidTest` on an API 35 emulator after JVM unit tests.

## Verification status
- Source and workflow assertions can be checked locally.
- Gradle build, Android unit tests, emulator instrumentation tests and device tests have not been executed in this workspace. CI must confirm these results before this version is treated as verified.
