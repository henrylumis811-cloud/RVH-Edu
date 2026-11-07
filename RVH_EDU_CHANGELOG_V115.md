# RVH Edu V115 — school-local admission numbers

## Implemented
- Added database schema v9 table `school_admission_number`, assigning admission numbers to a learner within a school rather than treating the number as part of the shared person identity.
- Migrates existing admission numbers to every school where that learner already has an enrollment. Learners without enrollments are retained under the legacy school. Existing learner, enrollment, assessment, and mark IDs are preserved; the old shared column is cleared only after assignments are written.
- Learner creation and edits now validate admission-number uniqueness within the active school. Different schools may reuse the same admission number.
- School roster reads and learner edit/archive/restore paths resolve the admission number in the active school context.
- Backup export now includes school-local admission assignments. Restore accepts older backups that predate this table and migrates their legacy admission values inside the restore transaction.
- Database integrity reporting includes school-local admission assignment rows.
- Added `tools/check_multi_school_v115.py` with source-contract assertions and a SQLite migration/isolation fixture.

## Compatibility and safety
- Learner identity remains shared across schools; this increment does not clone learners or change historical academic record IDs.
- A learner already enrolled in multiple schools receives the old admission number in each of those schools during migration. Teachers can then edit the number independently in each school.
- The legacy `learner.admission_number` column remains nullable for on-disk compatibility but is no longer authoritative for new records.

## Validation limits
- Source checks and SQLite fixture checks are not a substitute for Android compilation or testing a real database upgrade on-device.
- A full Android Gradle build and device-level migration/backup restore test remain release gates. This archive has no Gradle wrapper script, and system Gradle is unavailable in the current environment.
