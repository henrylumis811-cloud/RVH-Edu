# RVH Edu V24 — Class Progression

## Existing components inspected

- `AcademicDataService.roster`, `enroll`, and learner status handling
- `AcademicRepository` enrollment persistence and transaction boundaries
- `RvhEduDatabase` enrollment schema and uniqueness constraints
- Existing navigation in `MainActivity`

## Changes

- Added a Class Progression screen to the existing navigation drawer.
- Teachers can choose a source class roster, target academic year, destination class, and exactly which active learners to progress.
- Progression creates new enrollment rows; it does not edit or delete historical enrollments, assessments, or marks.
- The target year must be later than the source year and within the supported year range.
- The repository inserts the selected batch inside one SQLite transaction. If any learner already has an enrollment in the target year, or any insert fails, the entire batch is rolled back.
- Duplicate learner IDs in a batch are rejected, and archived learners must be restored before progression.
- Existing enrollment and learner-management systems were reused; no parallel learner database or progression repository was introduced.

## Validation performed

- `check_class_progression_integrity.py`: passed (`CLASS_PROGRESSION_INTEGRITY_OK`).
- Existing enrollment integrity checks: passed.
- Existing learner-creation integrity checks: passed.
- Existing learner-edit integrity checks: passed.
- ZIP integrity: checked after packaging.

## Validation limits

No full Android build or physical-device test was performed in this environment. Runtime SQLite transaction rollback still needs execution on a buildable Android environment/device.
