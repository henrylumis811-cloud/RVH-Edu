# RVH Edu V12 — Report Integrity

## Existing components reviewed

V12 extends the existing `ReportCardBuilder` and `CertificateBuilder`. It does not add another reporting subsystem.

## Changes

- Report cards reject results linked to a different learner or assessment.
- Report cards reject marks from another assessment, duplicate subject marks, unexpected subjects, and invalid mark ranges.
- Report completeness and missing-subject metadata must match the supplied marks.
- Report totals, maximum totals, and rounded average must match the supplied marks (missing subjects use the same 100-point default maximum as `AcademicEngine`).
- Certificates reject mismatched learner results and invalid school/year/title metadata; incomplete results remain ineligible.
- Expanded the existing pure-Kotlin regression test to cover duplicate marks, mismatched learner identity, and inconsistent result totals.

## Validation

- Targeted pure-Kotlin regression compiled with `kotlinc` and ran successfully: `RVH_EDU_V1_REGRESSION_OK`.
- ZIP integrity checked after packaging.
- Full Android Gradle build and physical-device testing were not performed in this environment.
