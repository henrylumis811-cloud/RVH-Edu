# RVH Edu V43 — Actionable policy-conflict diagnostics

- Added learner-aware policy compatibility diagnostics for class-wide results and analytics.
- When a class operation is blocked, the message now identifies learners with missing/legacy or invalid snapshots, or learners whose saved policies differ from the reference policy.
- Diagnostic output is bounded to eight names plus a remaining-count indicator; raw policy JSON is never exposed.
- No historical assessment policy is modified or automatically migrated.
- Added a source regression check for the diagnostic path.

Validation: source regression checks and ZIP integrity should be run on this package. This change has not been verified by a full Android Gradle build or physical-device test.
