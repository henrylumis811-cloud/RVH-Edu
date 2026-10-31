# RVH Edu V37 — Historical policy consumption

- Historical assessment calculations now resolve the persisted assessment record and use its saved subject list; complete snapshots also supply the grading scale.
- Draft mark validation uses the assessment's saved subject codes rather than rejecting valid historical subjects merely because the active catalog changed.
- Report-card subject rows and completeness checks now use the assessment snapshot when available, preserving saved subject labels and order.
- Class analytics use the selected assessment's captured pass threshold and subject set by default. An explicitly supplied pass threshold remains supported for deliberate overrides.
- Legacy assessments without a snapshot continue to fall back to the current catalog/scale; their missing original policy cannot be reconstructed.

## Verification limitation

Source-level checks and archive integrity can be run in this environment. No full Android Gradle build or physical-device test was run.
