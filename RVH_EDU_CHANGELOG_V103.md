# RVH Edu V103 — Multi-school backup coverage

## Changes
- Added `teacher`, `teacher_school_membership`, and `teaching_assignment` to the portable academic backup payload.
- Advanced the exported database schema marker to version 6.
- Added compatibility normalization for older backups that predate the teacher and teaching-assignment tables; those tables restore as empty rather than inventing records.
- Updated restore deletion order so teaching assignments and memberships are removed before their parents, while all operations remain inside the existing transaction.

## Validation boundaries
- Source-level multi-school backup checks are included in `tools/check_multi_school_v103.py`.
- This change has not been verified by an Android compile, a device run, or a restore round-trip against a real database.
- Older backup row-shape compatibility must still be exercised with representative archived files before declaring backup migration complete.
