# RVH Edu V98 — School-scoped academic repository

## Implemented
- Enrollment creation/upsert accepts an explicit `schoolId` (defaults to `legacy-school` for compatibility with the current single-school UI).
- Class-roster enrollment queries and learner enrollment history now filter by school ID.
- Class progression validates the source enrollment and target-year conflicts within the selected school, and writes the school ID on every inserted enrollment.
- Existing call sites remain source-compatible while the school selector/UI is built in a later phase.

## Important scope boundary
This is an incremental data-layer step, not complete multi-school support. The UI does not yet select an active school, the global learner identity and school settings still need deliberate scoping, and the existing enrollment uniqueness constraint still prevents the same learner being enrolled in the same class/year at two schools. That constraint needs a carefully tested migration before claiming full multi-school support. Backups also need a versioned format update before teacher/membership/assignment tables can safely be included.

## Validation
Source guardrails and ZIP integrity checks are run for this handoff. Android compilation, production-database migration testing, and device testing are not claimed unless separately completed.
