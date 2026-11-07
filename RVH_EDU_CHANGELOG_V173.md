# RVH Edu V173 — Backup compatibility test correction

**Version:** 1.5.3  
**Build:** 139

## Fix

- Corrected `BackupCompatibilityRulesTest.rejectsSchemasOutsideSupportedRange` to assert the full, actual validation message emitted by `BackupCompatibilityRules.requireSupportedSchemaVersion`.
- Kept the production compatibility rule unchanged; the failure was caused by a stale test expectation, not by incorrect schema acceptance/rejection.
- Updated the Android version metadata for this corrective release.

## Verification

- Confirmed the expected test message matches the production validator's current message.
- ZIP structure and integrity checked.
- Gradle/JUnit execution was not available locally; rerun `gradle testDebugUnitTest --no-daemon` in GitHub Actions to verify.
