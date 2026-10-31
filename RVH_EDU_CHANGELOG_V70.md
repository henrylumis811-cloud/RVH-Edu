# RVH Edu V70 — executable canonical JSON regression coverage

- Extracted canonical JSON serialization into the same pure-JVM checksum helper that production backup checksum generation and verification call.
- The Android database manager now adapts `JSONObject` and `JSONArray` into a JSON-shaped tree; the tested helper sorts object keys, preserves array order, distinguishes booleans from numbers, normalizes numeric forms, and escapes strings.
- Expanded the executable Kotlin regression suite to cover object-key ordering, numeric equivalence within nested JSON, array-order sensitivity, booleans versus numbers, null, escaped strings, and SHA-256 behavior.
- Added `tools/check_backup_canonical_json_runtime_coverage.py` to guard the production adapter and test coverage.

## Validation

- Pure-JVM Kotlin checksum/canonical JSON regression: 16 assertions passed.
- Python source regression scripts and Python tool compilation run during packaging.
- ZIP integrity and root-level project layout checked during packaging.
- Full Android Gradle build, Android `org.json` runtime integration, and SQLite restore/rollback on device remain unverified.
