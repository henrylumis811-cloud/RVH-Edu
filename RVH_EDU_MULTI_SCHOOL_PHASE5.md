# RVH Edu — Multi-school integrity audit (V104)

## Verified source contracts

- Enrollment creation rejects blank school IDs and checks duplicates within the specified school.
- Enrollment history and class enrollment queries include school identity in their predicates.
- Class roster retrieval passes the current active school ID.
- School selection validates that the requested school exists; creating a school selects it.
- Backup source includes teacher, teacher-school membership and teaching-assignment tables, and restore remains transactional.

## Known remaining risks (not represented as complete)

- The learner table remains a shared identity table. Admission-number uniqueness and whether admission numbers should be school-local require an explicit data-model decision and migration plan.
- Not every UI/export/assessment/mark/history call path has been demonstrated by this source-contract test to be school-scoped. This check is a targeted regression guard, not a proof of complete isolation.
- The active school is stored in SharedPreferences; it is a local selection, not an authenticated teacher permission boundary.
- School-specific pass threshold preferences need end-to-end verification through assessment creation, historical policy snapshots and every export.
- Backup compatibility needs fixture-based restore tests against real backups from older versions, including round-trip comparison of IDs, relationships, marks and historical policy snapshots.
- Full Android compilation and on-device migration tests remain required before release.

## Safe next step

Audit all call sites and make school ID an explicit dependency for every academic read/write operation, then add migration fixtures and a school A/school B isolation regression test. Do not auto-move or merge legacy records.
