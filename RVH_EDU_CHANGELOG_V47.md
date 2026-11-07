# RVH Edu V47

## Strict assessment-policy snapshot validation

- Reject snapshots with blank subject codes, short names, or display names.
- Reject duplicate subject codes regardless of letter case.
- Reject unknown school-class identifiers and negative capture timestamps.
- Reject empty aggregate/division band collections and blank division labels.
- Keep the existing fail-closed behavior: malformed non-null snapshots are not silently treated as legacy records and are not replaced with current settings.

## Validation boundary

Source-level regression checks and archive integrity checks are run for this handoff. This changelog does not claim a successful Android Gradle build or physical-device test.
