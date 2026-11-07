# RVH Edu V52

## Source-enrollment revalidation during progression

- Class progression now passes the exact source enrollment IDs represented by the teacher's selected roster.
- The repository rechecks each source enrollment's identity, learner, academic year, and class inside the same SQLite transaction that creates destination enrollments.
- If a learner's source enrollment was removed or changed after the roster was loaded, the whole batch is rejected and no destination enrollments are committed.
- Existing active-status checks, target-year duplicate prevention, and atomic batch behavior remain in place.
- Added `tools/check_progression_source_enrollment_revalidation.py`.

Validation is source-level only. A full Android Gradle build and physical-device test were not performed for this checkpoint.
