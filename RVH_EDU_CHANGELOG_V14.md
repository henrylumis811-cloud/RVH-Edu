# RVH Edu V14 — Persisted Assessment Write Protection

## Existing components inspected

- `AcademicDataService` — the existing manual marks and finalization service.
- `AcademicRepository` — the existing SQLite persistence boundary.
- `RvhEduDatabase` — existing schema, constraints, and transaction support.

No second marks service, repository, or assessment screen was introduced.

## Changes

- Both manual draft saves and finalization re-read the persisted assessment associated with the supplied enrollment and verify that the assessment ID matches.
- Draft mark replacement checks the stored assessment status **inside the same SQLite transaction** that replaces marks. A stale UI object cannot write to an assessment that has since been finalized.
- Finalization now replaces the marks and changes the persisted status to `FINAL` inside one transaction. If any write or status transition fails, the transaction rolls back.
- Finalization uses a conditional `UPDATE ... WHERE status = 'DRAFT'` and checks the affected-row count.
- Mark replacement still rejects cross-assessment marks, duplicate subject codes, negative marks, non-positive maxima, and marks above their maxima.
- Assessment status updates now fail clearly if the target assessment does not exist.

## Verification

Static source assertions verified the persisted-status guard, conditional status update, transaction boundaries, rollback-safe transaction pattern, and that the old unguarded `replaceMarks` entry point is no longer called. ZIP integrity was checked after packaging.

This is not a substitute for Android/SQLite integration tests on a device. Those remain a release-gate item.
