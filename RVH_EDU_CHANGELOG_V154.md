# RVH Edu — V154

## Bulk mark loading: roster batch query

- Replaced per-learner assessment lookup and mark reads with a school-scoped batch query for the roster.
- Kept bulk mark initialization on the IO dispatcher and publish the resulting input values after the batch completes.
- Preserved read-only browsing: missing assessments are not created by opening the bulk-entry screen or changing subjects.
- The batch query initializes empty results for roster members with no assessment or marks and chunks enrollment IDs to respect SQLite bind-variable limits.

Version: 1.3.4 (build 120)

## Verification status

- Source-level checks and ZIP integrity are checked during packaging.
- Gradle compilation, automated test execution, emulator/device testing, and measured frame-time profiling have not been performed in this environment.
