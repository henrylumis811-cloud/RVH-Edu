# RVH Edu V169 — Backup Regression Suite in CI

- Version: 1.4.9 (versionCode 135)
- Added `tools/run_backup_regression_suite.py`, a maintained, bounded runner for current backup/restore regression checks.
- Wired the suite into GitHub Actions before Android unit tests, so backup validation, rollback fixtures, round-trip integrity, size limits, row shape, and schema completeness are checked on pushes and pull requests.
- The suite explicitly labels its host-side SQLite/source-contract scope; it does not claim to replace Android instrumentation tests.
