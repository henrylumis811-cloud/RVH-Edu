# RVH Edu V1 — Data Integrity Milestone

## Completed
- The authoritative backup screen now operates only on the RVH Edu SQLite database.
- Legacy JSON backup/restore is no longer exposed as the normal school backup path.
- Database backup runs a read-only integrity audit before producing a backup.
- Database restore remains transactional and runs the same integrity audit before commit.
- Legacy assessment migration no longer uses SQLite `REPLACE`, preventing cascade deletion of marks on a unique-key collision.
- Class results now include incomplete learners explicitly while keeping them unranked.
- Dashboard class exports use the authoritative RVH Edu export pipeline.
- Ranking, import-parser and integrity-policy regressions pass in pure Kotlin tests.

## Integrity rules
1. Learner names are sufficient; admission numbers remain optional.
2. Same-name learners are never silently merged when identity is ambiguous.
3. A final assessment must contain every subject expected for its class.
4. Marks must satisfy `0 <= mark <= maxMark` and `maxMark > 0`.
5. Foreign-key relationships must remain valid.
6. Backup restore must either commit completely or leave the existing database unchanged.
7. Assessment migration must never use destructive `REPLACE` semantics.
8. Incomplete results are displayed but never receive a class position.

## Tests
- `RVH_EDU_V1_REGRESSION_OK`
- `IMPORT_PARSER_REGRESSION_OK`
- `RANKING_INTEGRITY_REGRESSION_OK`
- `INTEGRITY_POLICY_REGRESSION_OK`

A full Android/SQLite instrumentation run is still required on a real Android environment before V1 qualification.
