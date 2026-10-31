# RVH Edu V54 — Auditable learner archiving

- Added database schema version 6 and a durable learner status-history table.
- Archiving now requires a reason in the learner-management confirmation dialog; an optional note can be recorded.
- Archive status change and audit event are committed in the same database transaction.
- Restoring an archived learner appends a restore event in the same transaction as the status change.
- Archived Learners screen displays the most recent recorded archive reason by learner ID, not by name.
- Existing databases are upgraded by adding the history table; prior learner, enrolment, assessment, mark and report data is not rewritten.
- Archive reason length is limited to 120 characters and optional note to 500 characters.
- Added a source-level regression check for migration, transaction, audit and UI wiring.

Validation: 36 Python source checks pass; Python tool compilation and ZIP integrity pass. A full Android Gradle build and physical-device test have not been confirmed for this checkpoint.
