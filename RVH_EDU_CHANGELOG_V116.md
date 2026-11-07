# RVH Edu V116 — school-scoped history visibility

## Implemented
- Learner academic history now confirms that the learner has at least one enrollment in the active school before resolving the learner profile or constructing history.
- The learner profile lookup used by history is school-contextual rather than global, protecting against stale IDs or cross-school navigation paths.
- Archived-learner reason lookup is filtered to learners enrolled in the active school, while preserving deterministic newest-event selection.
- Added `tools/check_multi_school_v116.py` to guard the source-level school ownership contract.

## Important limitation
The current schema still stores lifecycle status and status-history events against the shared learner identity. V116 tightens which school may view history/reasons but does not yet make archive/restore state school-local. A later schema and workflow change must move roster lifecycle state to school enrollment or a school-owned learner-status table before claiming complete lifecycle isolation.

## Validation
The V116 source-contract checker passes. This is not a substitute for Android compilation, database runtime tests, or on-device validation. No full Gradle build was run for this checkpoint.
