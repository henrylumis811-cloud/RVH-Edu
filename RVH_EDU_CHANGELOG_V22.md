# RVH Edu V22 — Learner Details Editing

## Changes
- Added editing of a learner's full name, optional age, and optional admission number from the existing Learners roster.
- Reused the existing learner-management screen and `AcademicDataService`; no parallel learner store or duplicate edit screen was introduced.
- Updates run inside a SQLite transaction and preserve the learner's stable ID, enrollment records, marks, and academic history.
- Names are trimmed and whitespace-normalized; ages are validated; admission numbers are trimmed, whitespace-normalized, uppercased, and checked case-insensitively against other learners.
- The updated timestamp is refreshed when details change.

## Validation performed
- `tools/check_learner_edit_integrity.py` passed.
- `tools/check_learner_creation_integrity.py` passed.
- `tools/check_assessment_write_integrity.py` passed.
- ZIP integrity check passed after packaging.

## Not verified
- Full Android/Gradle build.
- Runtime SQLite transaction tests or physical-device UI testing.

The current environment does not have the Gradle wrapper JAR or system Gradle available, so no APK build success is claimed.
