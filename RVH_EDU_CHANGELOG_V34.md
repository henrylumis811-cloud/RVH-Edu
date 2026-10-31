# RVH Edu V34 — Subject configuration revision history

- Added a device-local revision history for saved lower-primary and upper-primary subject configurations.
- Every actual configuration change appends a numbered, timestamped revision. Re-saving an unchanged configuration does not create a false change revision.
- Added a history section to School Subjects settings and a restore action. Restoring a prior configuration creates a new revision instead of deleting intervening history.
- Kept the existing `catalog_v1` preference key so installations from earlier versions continue to load their current subject configuration.
- Added validation at the repository boundary before persisting configurations.
- History is capped at the latest 50 revisions to avoid unbounded preference growth.

## Important scope boundary

This is a revision-history foundation, not yet an immutable policy snapshot attached to each assessment. Historical report cards and analytics may still use the currently active catalog. Do not claim old results are fully policy-versioned until assessments store the exact policy revision and calculation rules they used.

## Verification

Source-level integrity checks and ZIP structure checks only. Full Android Gradle build and device testing were not performed in this environment.
