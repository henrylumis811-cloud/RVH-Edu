# RVH Edu V105 — Academic History School Isolation

## Implemented
- Fixed learner academic history to request enrollments for the currently selected school explicitly.
- Added a repeatable source-contract checker covering school context propagation in enrollment creation, class roster, progression, and learner history.

## Why this matters
The repository method defaults to the legacy school for backward compatibility. A history call that omitted `schoolId` could therefore show an empty or incorrect history when the teacher was working in a newly created school. V105 removes that accidental fallback for the service's learner-history path.

## Validation
- Source-contract checker: run `python3 tools/check_multi_school_v105.py`.
- Android build, database migration, and device-level multi-school isolation tests were not run in this handoff.

## Remaining work
- Audit every screen/report/export path and make school context explicit end-to-end.
- Decide and implement school-local admission-number semantics without silently merging or moving legacy learners.
- Add database-fixture backup/restore and migration tests, then compile and test on device.
