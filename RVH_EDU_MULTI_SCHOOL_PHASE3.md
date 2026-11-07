# Multi-school Phase 3 — Directory and settings foundation

`SchoolManagementRepository` is the local source for school directory operations. `activeId` validates the persisted selection against the database and falls back to `legacy-school`. `create` creates a distinct school and selects it without moving old records. `update` changes only school branding metadata.

`SchoolInfoRepository` now stores the school name in the selected school row and scopes the pass threshold by active school. The old global threshold is read only as a fallback, so a legacy installation retains its prior setting on first use.

Important remaining work: wire a school-management screen into navigation; propagate active school ID through every academic read/write; add school scope to backup/restore schema; validate upgrade from actual schema versions; and run Gradle + device tests. Do not use school switching with academic workflows until all repository paths enforce the active school.
