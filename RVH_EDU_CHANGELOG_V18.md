# RVH Edu V18 — Academic Calculation Boundary Validation

## Changes

- Strengthened the existing `AcademicEngine.calculate` boundary so malformed mark data cannot be silently collapsed by `associateBy`.
- Rejects blank learner/assessment identifiers and blank or duplicate subject codes.
- Rejects duplicate marks for a subject, marks linked to another assessment, subjects outside the requested subject list, non-positive maximum marks, negative marks, and marks above their maximum.
- Extended the existing `RvhEduV1Regression` pure test with malformed-input cases. No duplicate calculation engine or screen was added.

## Verification

- Targeted Kotlin compilation of the academic engine and its direct model/report dependencies: passed.
- `RvhEduV1Regression`: passed (`RVH_EDU_V1_REGRESSION_OK`).
- Full Android build/device validation: not performed in this environment.
