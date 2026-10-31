# RVH Edu V36 — Complete assessment scoring-policy snapshots

- Upgraded assessment snapshot schema to v2.
- Newly opened assessments capture their subject list, aggregate bands, division bands, and configured class pass threshold.
- Saving marks for a v2 assessment calculates using its saved subject list and grading scale rather than the currently active catalog/scale.
- Schema v1 snapshots remain readable as subject-only legacy snapshots; their scoring calculation continues to use the explicitly supplied current scale because the original scoring rules were never stored.
- The assessment database migration from v4 to v5 remains unchanged; policy details are stored in the existing JSON snapshot field.

## Verification limitation

Source-level Python checks and archive integrity checks are available. No full Android Gradle build or device test was run in this environment. Pass threshold is captured for policy history, but historical analytics/reporting have not yet been fully switched to consume it.
