# RVH Edu V19 — Ranking Integrity

## Existing components inspected

- `RankingEngine` — existing ranking and tie-position logic.
- `AcademicResult` and `RankedResult` — existing result models.
- `RankingIntegrityRegression` and `RvhEduV1Regression` — existing pure regression coverage.

## Changes

- Kept the existing ranking engine; no duplicate engine or screen was added.
- Removed the misleading `includeIncomplete` argument from `rank`. The dedicated `rankIncludingIncomplete` method remains the single explicit way to show incomplete learners, and those learners stay unranked at position `0`.
- Added input-boundary checks for duplicate learner results, mixed assessments, blank IDs, invalid totals, invalid averages, and disagreement between the completeness flag and missing-subject list.
- Preserved competition ranking for ties (for example, 1, 1, 3) and deterministic learner-ID ordering within tied results.

## Verification

- Targeted Kotlin compilation and ranking regression execution: see handoff status.
- Full Android build and physical-device testing are not claimed unless separately performed.
