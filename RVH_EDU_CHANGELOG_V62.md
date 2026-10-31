# RVH Edu V62

## Backup semantic validation

- Added pre-restore duplicate checks for learner/year/class enrolments, enrolment/term/section assessments, and assessment/subject marks.
- Added semantic validation for legacy identity-review records, including learner references, self-links, reasons, resolution flags, timestamps, and duplicate entries.
- Rejected status-history entries that claim no actual status transition.
- These checks run before the destructive restore transaction; SQLite constraints and the existing transaction remain the final safeguards.
- Corrected ZIP packaging so the Android project files and top-level documentation are directly at the archive root.

## Validation limits

Automated source regression checks and ZIP integrity checks are performed for this handoff. A full Android Gradle build and physical-device backup/restore test are not claimed unless separately executed.
