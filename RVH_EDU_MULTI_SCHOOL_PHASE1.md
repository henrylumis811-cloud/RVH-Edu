# RVH Edu multi-school support — Phase 1

## Delivered in schema version 7

- Introduces stable `teacher` identities.
- Introduces time-bounded `teacher_school_membership` records, allowing one teacher to have multiple simultaneous school memberships and to end/rejoin relationships without deleting historical membership rows.
- Introduces `teaching_assignment` records linked to a specific membership, academic year, class and optional subject.
- Adds `enrollment.school_id` and a stable `legacy-school` identity. Existing enrollment IDs and downstream assessment/mark IDs are preserved; existing rows are assigned to this one legacy school during an additive migration.
- Uses foreign keys and indexes for membership and assignment integrity.
- Remains offline-first; no server or account is required for this schema foundation.

## Important scope boundary

This is the **database foundation**, not yet the complete multi-school user experience. The current enrollment uniqueness constraint still uses `(learner_id, academic_year, school_class)` and must be migrated to include `school_id` before a learner can hold equivalent class/year enrollments in multiple schools. Existing repositories and screens also need an explicit active-school context and school-scoped queries before adding the school switcher. Those steps are intentionally not faked in this phase.

The current learner identity remains global so that a learner can potentially have enrolments at different schools without duplicating the person; learner-transfer workflow and per-school admission-number rules require explicit product decisions and tests.

## Migration safeguards

- Migration is additive and does not drop or recreate academic tables.
- Existing assessment/mark identifiers and historical policy snapshots are untouched.
- The stable `legacy-school` row is inserted idempotently.
- The migration should be tested against a copied database from each supported schema version before release.

## Next implementation phase

1. Rebuild the enrollment uniqueness constraint to include school identity without breaking assessment foreign keys.
2. Add a durable active-school context.
3. Thread school ID through enrollment creation, progression, roster and academic queries.
4. Move school name/pass-threshold preferences into school-scoped settings with one-time migration from legacy preferences.
5. Add school management/switching UI only after query isolation tests pass.
6. Add repository tests proving two schools with identical class/subject names cannot see or alter each other's records.
