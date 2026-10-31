# RVH Edu V51

## Class progression transaction revalidation

- Revalidates each learner's existence and `ACTIVE` status inside the same SQLite transaction that inserts a progression batch.
- Rejects blank learner identities and invalid academic years at the repository boundary, even when the service layer is bypassed.
- Keeps duplicate target-year enrollment detection and atomic all-or-nothing insertion.
- Does not alter prior enrollments, marks, assessments, or historical reports.
- Added `tools/check_progression_transaction_revalidation.py`.

Validation is source-level only. A full Android Gradle build and physical-device test were not performed for this checkpoint.
