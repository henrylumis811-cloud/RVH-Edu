# RVH Edu V42 — Class policy compatibility guard

- Added one reusable compatibility validator for class-wide assessment operations.
- Class rankings and analytics now stop when saved policies are malformed, mixed with legacy/unpinned assessments, or materially different.
- Equivalent policies captured at different times remain compatible; capture timestamps do not affect the signature.
- Class export now uses the same validator as rankings and analytics.
- Main-screen results, analytics, and export actions surface a readable blocked-operation status instead of allowing the policy conflict to crash the action.
- Added a source regression check for the shared validator and runtime wiring.

Validation: source-level checks and ZIP integrity only. A full Android Gradle build and physical-device test were not available for this handoff.
