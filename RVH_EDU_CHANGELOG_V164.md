# RVH Edu V164 — Backup Compatibility Regression Tests

Version: 1.4.4  
Build: 130

- Extracted backup-version and schema-version compatibility gates into a small pure validation helper.
- Backup preview and restore preflight now use the same compatibility rules, reducing the risk that preview accepts a version restore interprets differently.
- Added unit tests for accepted legacy/current versions, unsupported versions, oldest supported schema, current schema, and out-of-range schemas.

## Verification
- Source assertions and ZIP integrity checked.
- Gradle compilation, JUnit execution, and Android/device integration tests were not run in this environment.
