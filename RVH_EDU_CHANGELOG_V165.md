# RVH Edu V165 — Backup Preview Semantic Validation

- Backup preview now runs the same database-independent domain-row validation used by restore, after checksum verification and legacy-shape normalization.
- Malformed learner identifiers, lifecycle statuses, and other semantic row problems caught by the shared validator can be reported before the user confirms a destructive restore.
- Database-specific column compatibility, row limits, SQLite constraints, and transaction-time integrity checks remain part of restore; preview does not claim to replace them.
- Version: 1.4.5 (versionCode 131).

## Verification

Source-level assertions and ZIP integrity checks only. Gradle compilation, JUnit execution, and Android device tests were not run in this environment.
