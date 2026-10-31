# RVH Edu V68 — restore transaction safety regression guards

- Added `tools/check_backup_restore_transaction_safety.py` to guard the ordering of restore preflight, checksum verification, database validation, and the destructive transaction boundary.
- Added checks that database integrity must pass before the transaction is marked successful and that the transaction is always ended through its `finally` path.
- Added regression guards for legacy version-1 compatibility and version-2 checksum enforcement.
- This is a source-structure regression guard, not a simulated SQLite rollback test. End-to-end restore testing remains a separate milestone.

## Validation

- Python source regression suite: run during packaging.
- Pure-JVM checksum helper regression: run during packaging.
- ZIP integrity and archive-root layout: checked during packaging.
- Full Android build and physical-device restore test: not claimed by this release.
