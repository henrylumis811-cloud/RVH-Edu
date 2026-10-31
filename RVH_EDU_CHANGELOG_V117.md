# RVH Edu V117 — School-local learner lifecycle

## Changes
- Introduces `school_learner_status`, keyed by `(school_id, learner_id)`, so active/archived state is owned by each school rather than the shared learner identity.
- Database schema advances from v9 to v10. The migration preserves learner and academic IDs, backfills each enrolled school from the previous status, and gives un-enrolled legacy learners a compatibility row.
- School-scoped learner lookups and rosters now read the school-local status.
- Learner edits no longer overwrite the shared learner's legacy status column. Status updates flow into the school-local record through the existing repository upsert.
- Lifecycle audit events now carry a school ID, archive reasons are filtered by that school, and a school-history index is added.
- Backup schema advances to v8, exports/restores school-local statuses, supports older backups without the new table, and backfills status rows after restoring an older backup.
- Database integrity audit now checks school-local status records.

## Compatibility and safety
- Existing IDs, enrollments, assessments, marks, and admission assignments are not intentionally rewritten by the schema migration.
- The old `learner.status` column remains for legacy compatibility; school-facing views use the new school-local status table.
- Legacy lifecycle-history entries migrated from schema v9 receive `legacy-school` as their school attribution because the old schema did not retain the originating school. New archive/restore events are attributed correctly.

## Validation boundary
The V117 source checker verifies key migration, repository, lifecycle, backup, and integrity contracts. It is a source-level check, not an Android compilation or device test. A full Gradle build/device validation remains required before release.
