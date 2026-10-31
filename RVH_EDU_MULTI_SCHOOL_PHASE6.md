# Multi-School Phase 6 — History Context Boundary

## Rule
Academic history is school-scoped: when a teacher selects a school, history queries must use that school's enrollments. A repository compatibility default pointing at `legacy-school` must never be relied on by active-school UI/service flows.

## V105 change
`AcademicDataService.learnerHistory` now passes `activeSchoolId()` into `AcademicRepository.getEnrollmentsForLearner`.

## Deliberate non-change
Learner identity records remain globally stored. This patch does not claim to solve school-local admission-number uniqueness and does not rewrite legacy records. That needs a separately planned migration and explicit duplicate-handling policy.
