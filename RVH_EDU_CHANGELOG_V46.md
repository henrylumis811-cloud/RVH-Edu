# RVH Edu V46 — Fail-closed historical policy handling

- Changed assessment mark saving so a non-null but malformed saved policy snapshot blocks the save with an actionable message. It no longer silently falls back to the currently active subject/grading rules.
- Changed historical result calculation to distinguish a genuinely legacy `null` snapshot from an invalid non-null snapshot. Invalid snapshots now stop calculation instead of being treated as if no policy had ever been saved.
- This protects historical integrity without attempting to repair or rewrite the record automatically.
- Added `tools/check_invalid_snapshot_fails_closed.py`.

Validation is source-level only. A full Android Gradle build and physical-device test have not been performed.
