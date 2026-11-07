# RVH Edu V59 — backup integrity and canonical admission lookup

- Database backups now include `learner_status_history`, preserving archive/restore audit events during export and restore.
- Backup schema advances to version 5. Schema 4 backups remain importable; since they predate the audit table, their restored audit history begins empty rather than fabricating past events.
- Restore clears status-history child rows before learners, respecting foreign-key constraints, then restores records in parent-to-child order within the existing transaction.
- Database integrity counts now include learner status-history rows and foreign-key checks already cover their references.
- Admission-number lookup now uses the same canonical normalization as learner writes, including legacy records with whitespace/case differences.
- Added source regression checks for backup coverage/backward compatibility and canonical lookup.
- Validation remains source-level; a full Android Gradle build and device restore test are still required.
