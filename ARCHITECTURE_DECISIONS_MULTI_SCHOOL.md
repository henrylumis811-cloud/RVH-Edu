# Architecture Decision: Multi-school identity and teacher mobility

**Status:** Accepted design direction; implementation must proceed through a reviewed database migration.

## Context

RVH Edu is offline-first and currently has a `school` table, but the current schema does not consistently scope learner, enrolment, assessment, mark, and settings data to a school. A school switcher alone would therefore be unsafe: identical class names and subject codes at different schools could collide or leak across contexts.

## Decision

Treat `School`, `Teacher`, and `TeacherSchoolMembership` as separate identities. A teacher may have multiple active memberships, and a membership may end without deleting the school or its academic history. The active school is a UI/session context, never the source of truth for ownership.

### Target relationships

- `school(id, name, ... )`: stable institution identity.
- `teacher(id, display_name, ...)`: stable teacher identity, independent of employment.
- `teacher_school_membership(id, teacher_id, school_id, role, started_at, ended_at, status, ...)`: supports concurrent employment and return visits.
- `class_assignment` and `subject_assignment`: optional time-bounded links from a membership to a class/subject, with school scope enforced.
- Learner identity is distinct from school enrolment. `learner` is a person record; `enrollment` connects that learner to a school/class/academic year. A transfer creates a new enrolment, not a silent move of old assessments.
- Assessments and marks remain reachable through a school-scoped enrolment and retain the grading-policy snapshot used at the time.
- School-specific settings, subjects, grading rules, exports and backups must carry a stable `school_id`.

## Migration requirements

1. Inspect existing production schemas and backup formats before changing constraints.
2. Create/preserve one stable school identity for the existing single-school installation.
3. Backfill every existing learner/enrolment/assessment/settings record to that school in one transaction, using deterministic, reviewed rules.
4. Add foreign keys and indexes only after backfill checks pass.
5. Preserve IDs, marks, timestamps, archived states and policy snapshots. Never recalculate historical results as a migration side effect.
6. If any record cannot be assigned unambiguously, abort and preserve the original database; do not guess.
7. Test migration using a copy of a real prior-version database and verify row counts, foreign-key integrity, and export parity before release.

## Transfer and access rules

- Ending a teacher membership changes access/association state; it never deletes school records.
- Membership alone must not grant access to a former school's data forever. Access follows explicit local authorization until secure multi-user identity is implemented.
- A learner transfer is an explicit, reviewable import/export operation. Do not merge by name alone.
- Backups must declare the school identity and format/schema version. Import previews must detect school-ID collisions and require explicit resolution.
- Multi-device synchronization is a separate future capability; a file backup is not synchronization.

## Implementation sequence

1. Schema inventory + backup compatibility tests.
2. School identity and transactional migration.
3. Repository/query scoping by `school_id`.
4. School selector and per-school settings/export context.
5. Teacher memberships and assignments.
6. Controlled learner transfer, audit history, and permission review.

## Non-goals for the first migration

No automatic cloud accounts, background synchronization, or implicit cross-school learner merging. No claim of secure multi-user authorization until an authentication/permission system is implemented and tested.
