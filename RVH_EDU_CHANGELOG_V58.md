# RVH Edu V58 — Atomic normalized admission-number uniqueness

- Learner create/update now uses the same canonical admission-number normalization as duplicate review.
- The repository checks all existing non-null admission numbers after normalization, so legacy values with different casing or whitespace cannot bypass the duplicate guard.
- The normalized uniqueness check and learner write execute in one SQLite transaction; a failed validation/write rolls back.
- Updating a learner excludes that learner's own ID from the duplicate scan.
- This does not merge records, rewrite academic history, or automatically resolve duplicate legacy data.

## Validation
- Automated source regression checks: see `tools/check_atomic_normalized_admission_uniqueness.py` and the existing checks.
- Full Android Gradle build and physical-device testing were not performed for this checkpoint.
