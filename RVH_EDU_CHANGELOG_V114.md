# RVH Edu V114 — Active-school learner directory

## Implemented
- Changed the service-level learner directory to return only learners with an enrollment in the currently selected school.
- Changed service-level name/age search to use the same school-local roster, so unrelated schools' learners are not surfaced by ordinary directory search.
- Kept the underlying learner identity shared; this change does not duplicate people or move historical records.
- Added a repeatable source-contract checker for the active-school learner-directory boundary.

## Safety boundary
- This is a directory/read-boundary increment, not full school-local admission-number support. The database still stores `admission_number` on the shared learner row and enforces global uniqueness.
- Shared learner-profile edits and archive/restore semantics still need an explicit cross-school policy before school-local admission identifiers can be completed safely.
- Learners with no enrollment in any school are intentionally not shown in the school-local directory until a school association workflow exists.

## Validation
- Source-contract checks are provided in `tools/check_multi_school_v114.py`.
- Android Gradle compilation, SQLite migration fixtures, backup/restore round trips, and device validation were not run in this source handoff.
