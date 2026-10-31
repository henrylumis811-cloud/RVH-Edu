# RVH Edu — V28 change log

## Atomic individual mark-sheet draft saves

- Replaced the individual mark sheet's per-subject save loop with a single complete mark-set operation.
- Validates that the submitted subject codes exactly match the configured subjects for the learner's class, rejects duplicate/unknown subjects through the existing workflow validation, and verifies the persisted assessment is still the same draft.
- Converts the complete set into non-blank marks and sends it to the repository's existing SQLite transaction. This prevents a mid-save failure from leaving only some subjects updated.
- Preserved the existing one-subject draft-save API for bulk-entry and reviewed spreadsheet-import flows.
- Added a source-level regression check for the atomic save path.

## Validation

All available `tools/check_*.py` source-level checks were run. These are static checks, not a substitute for a full Android Gradle build or device test. The current environment does not have the project's Gradle wrapper JAR or a system Gradle installation, so a full Android build was not run.
