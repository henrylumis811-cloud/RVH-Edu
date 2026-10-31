# RVH Edu V39 — Historical-policy-aware exports

- Class result exports can now receive the immutable policy snapshot captured by an assessment.
- PDF, Excel, and CSV exports use the saved subject ordering and display labels when a valid snapshot is available.
- Legacy or malformed/missing snapshots fall back to the active subject catalog, preserving compatibility with older records.
- Export input validation checks marks against the effective saved subject set rather than silently rejecting subjects removed from today's catalog.
- Added `tools/check_historical_export_policy.py` as a source-level regression check.

## Verification boundary

Source-level checks and ZIP integrity checks are run for this handoff. A successful source check is not a substitute for an Android Gradle build or a device test; those remain outstanding when the build environment lacks the Gradle wrapper JAR/system Gradle.
