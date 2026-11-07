# V99 — School-scoped enrollment uniqueness

- Advanced database schema to version 8.
- Removed global learner/year/class uniqueness from enrollment.
- Added school/learner/year/class uniqueness and school foreign-key enforcement.
- Added transactional rebuild migration that preserves enrollment, assessment and mark IDs and values.
- Updated regression checks for the new schema version and migration contract.
- Added phase 2 migration notes and explicit remaining scope.
