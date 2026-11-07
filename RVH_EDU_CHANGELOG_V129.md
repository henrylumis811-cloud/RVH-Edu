# RVH Edu V129 — Backup round-trip and rollback verification

## Scope

- Added `tools/check_backup_roundtrip_v129.py`, a deterministic SQLite fixture covering the current backup/restore relationship graph across all 12 exported tables.
- Verifies that row contents and identifiers survive JSON serialization and parent-first restore, including school-specific admission assignments, enrollment/assessment/mark links, teacher-school relationships, identity-review records, and learner-status audit history.
- Verifies independent learner lifecycle states across two schools and retains a learner created before enrollment.
- Simulates a failed write after destructive deletion and verifies transaction rollback returns the database to its exact prior snapshot.
- Adds source-contract checks for preflight ordering, integrity audit before transaction success, and transaction-finally rollback behavior.
- Refreshed four legacy source-check scripts whose assertions still expected database/backup schema versions or audit-call signatures from earlier milestones; their checks now match the current v10 and school-local lifecycle implementation.

## Validation

`python3 tools/check_backup_roundtrip_v129.py` — 14/14 checks passed.

**Important limit:** this is a Python SQLite behavioral fixture and source-contract test, not Android instrumentation. It does not prove the Android app compiles or that a real device's database can be exported/restored. The Gradle build and Tecno Spark 30C verification remain the next step.
