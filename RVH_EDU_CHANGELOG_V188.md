# RVH Edu V188 — Bulk mark entry layout refinement

- Version: 1.6.7, versionCode 153.
- Replaces the permanently expanded assessment-period and subject selector panel with a compact current-selection summary and a dedicated Material 3 modal bottom sheet.
- The marking setup sheet groups Term, Assessment Period, and Subject selection, with an explicit Apply action.
- Selection changes are staged in the sheet and applied together; changing period/subject still passes through the existing unsaved-marks confirmation guard, so unsaved marks are not silently discarded.
- The learner roster receives the vertical space previously occupied by the large selector panel.
- Mark entry fields use a consistent compact width so learner names and mark controls align more predictably across screen sizes.
- No database schema, stored assessment identity, grading, or mark-save semantics are changed.

## Verification status

- V188 bulk-mark layout contracts: 9/9 passed.
- Mark-entry UI contracts: 13/13 passed.
- Bulk-mark recovery checks: 10/10 passed.
- Unsaved-mark guard checks: 8/8 passed.
- Historical analytics/error-routing contracts: 8/8 passed.
- Migration safety checks: 13/13 passed.
- Backup/restore regression suite: 19/19 passed.
- Professional UI design checks: 9/9 passed.
- Shared form design checks: 12/12 passed.
- Shared feedback consistency checks: 8/8 passed.
- Android Gradle compilation and unit tests: not run in this environment because Gradle and the wrapper JAR are unavailable; GitHub Actions is required.
- Physical TECNO Spark 30C verification: pending.
