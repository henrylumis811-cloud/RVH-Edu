# RVH Edu V41 — Exact and deterministic class ranking

- Replaced floating-point normalized-score comparison in `RankingEngine` with exact fraction comparisons using `Long` cross-products.
- Equal percentages now reliably share competition ranks (for example, 1, 1, 3), even when marks use different maximum totals.
- Learner ID remains the deterministic display-order tie-breaker without changing the shared rank position.
- Added `tools/check_exact_ranking_ties.py` as a regression guard.

## Verification boundary

All available source-level Python checks and Python tool compilation are run for this handoff. A complete Android Gradle build and physical-device test are not available in this environment; do not treat source checks as an installability guarantee.
