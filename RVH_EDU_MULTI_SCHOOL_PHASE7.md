# RVH Edu multi-school phase 7 — assessment boundary audit

## Scope
This phase adds repeatable source-contract checks for the assessment → marks persistence boundary. Assessment records are associated with enrollments; the enrollment is the school-scoped academic identity. Service-level saves compare the supplied assessment and enrollment identities, and repository mark replacement validates every mark's assessment ID.

## Important limitation
This is not yet a complete tenant-security boundary. The local active-school preference is UI context, not authorization. Several repository methods accept globally unique enrollment/assessment IDs without an explicit school ID; this phase audits their existing relationship checks but does not claim they enforce active-school membership against malicious or stale callers. A later implementation should pass a verified school ID through assessment operations and check `enrollment.school_id` inside the same database transaction as each write.

## Required next implementation
1. Add school ID to assessment service/repository boundaries and verify enrollment ownership in SQL.
2. Ensure assessment create/update, finalize/reopen, and mark read/write use that verified enrollment.
3. Add database-backed tests for cross-school IDs, duplicate admission numbers, and backup/restore round trips.
4. Run Android compilation and migration tests on representative existing databases before release.

No learner records are reassigned or merged by this phase. No Android build or physical-device test is claimed.
