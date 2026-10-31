# RVH Edu V113 — School-local learner reads

## Source changes
- Added `findByAdmissionNumberInSchool(admissionNumber, schoolId)` to search normalized admission numbers only among learners with an enrollment in the specified school.
- Added `getEnrolledLearnersForSchool(schoolId)` to return a distinct school-enrolled learner roster.
- Both methods reject a blank school ID and use bound SQL parameters.
- Existing APIs are retained for compatibility.

## Important limitation
Admission numbers are still stored on the shared `learner` row, and `LearnerRepository.upsert` still enforces global uniqueness. These additive read methods do **not** enable different schools to assign the same admission number to different learners. Correct school-local write semantics require moving admission numbers to a school-scoped enrollment/admission table and a carefully tested, data-preserving database migration. This release deliberately does not pretend that migration is complete.

## Validation
Run `python tools/check_school_local_learner_reads_v113.py` for source-contract checks. These checks are not Android compilation, SQLite integration tests, migration tests, or physical-device tests.
