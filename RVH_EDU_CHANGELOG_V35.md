# RVH Edu V35 — Assessment policy snapshot foundation

- Added a versioned, JSON-serializable assessment policy snapshot model.
- New assessments opened through the normal academic service capture the class and exact subject codes, short names, and display names available at creation time.
- Added SQLite schema migration from database v4 to v5 with a nullable `policy_snapshot_json` column.
- Existing databases are upgraded without fabricating snapshots for old assessments; a null snapshot means historical policy is unknown/unpinned.
- Repository upserts preserve an existing snapshot instead of silently replacing it when the active subject catalog changes.
- Added a source-level regression check for the model, capture path, persistence, and migration.

## Deliberate limitation

This is the subject-catalog snapshot foundation, not full historical grading-policy replay yet. Pass thresholds, aggregate/division tables, ranking rules and report/analytics calculation paths are not yet all captured and resolved from assessment snapshots. Legacy records remain explicitly unpinned. Full Android Gradle/device testing was not available in this environment.
