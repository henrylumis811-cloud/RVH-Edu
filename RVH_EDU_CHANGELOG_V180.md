# RVH Edu V180 — SQLite busy/learner-save reliability fix

- App version: 1.5.9 (version code 145).
- Enables SQLite write-ahead logging for the shared RVH Edu database helper so reads can continue during writes.
- Configures a 5-second SQLite busy timeout on each database connection so brief overlapping writes wait rather than immediately failing with `SQLITE_BUSY` / “database is locked”.
- Keeps the existing learner creation transaction and school-scoped admission-number checks intact.
- This change does not delete, reset, or migrate user records; the database schema version is unchanged.

Validation note: source/regression checks and ZIP integrity can be run in the handoff environment. Android compilation and physical-device confirmation must be performed by the release workflow and on-device testing; do not treat this source patch as already device-verified.
