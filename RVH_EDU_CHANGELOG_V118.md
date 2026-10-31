# RVH Edu V118 — Lifecycle write isolation

## Changes
- Corrected a cross-workflow risk in `LearnerRepository.upsert`: saving a learner profile now inserts a school-local lifecycle status only if that school/learner status row does not already exist. It cannot silently reactivate an archived learner or overwrite an archived state because a stale form submitted `ACTIVE`.
- Added `setStatusInSchool`, an explicit lifecycle operation scoped by both `school_id` and `learner_id`.
- Archive and restore now call that explicit operation and continue writing their audit event inside the same database transaction.
- Added `tools/check_multi_school_v118.py` to guard these repository/service contracts.

## Validation boundary
The V118 checker validates source-level contracts only. It does not execute Android SQLite transactions, prove migration behavior on a real v9 database, compile the Android project, or replace physical-device tests. Those remain required release gates.
