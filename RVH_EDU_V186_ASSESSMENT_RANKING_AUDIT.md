# RVH Edu V185 assessment ranking audit

## Finding

The class-ranking validator compared `AcademicResult.assessmentId` values and required them all to be identical. In the app's data model, an `AcademicAssessment` belongs to one enrollment (`AcademicAssessment.enrollmentId`), so each learner's row is expected to have its own primary-key ID. `AcademicDataService.classResults()` correctly fetches one assessment row per learner for the same selected school/year/class/term/section, but those row IDs are different by design. The validator therefore rejected a valid class sitting and raised `Ranking results must belong to the same assessment`.

The same `classResults()` path feeds class analytics, so analytics inherited the false block. `AcademicExportManager.exportClassResults()` repeated the same incorrect distinct-assessment-ID check and could block class exports too.

## Secondary UI finding

`MainActivity` stored all success/failure notices in one global `importStatus` state and rendered it above every screen. A class-results failure therefore remained visible on the learner directory, marks sheet and policy-review screen until manually dismissed, even when those screens were unrelated. Successful class-results and analytics loads now clear that stale notice.

## V186 correction

- Add a runtime-only `assessmentGroupKey` to calculated results.
- For class results, derive that key from active school ID, academic year, class, term and assessment section.
- Ranking accepts distinct per-learner assessment row IDs only when every result carries the same nonblank group key.
- Ranking still rejects mixed group keys. Callers without group keys retain the previous strict same-assessment-ID validation.
- Class export applies the same group-key validation, with backward-compatible strict validation for older callers.
- Add unit regression tests for valid per-learner assessment IDs, mixed group rejection and legacy fallback.
- Clear stale global block notices after successful class-results/analytics loads.

## Evidence from supplied V185 source

- `core/grading/RankingEngine.kt`: old check required one distinct `assessmentId`.
- `core/assessment/AcademicDataService.kt`: class results are built from one assessment row per enrollment.
- `core/model/AcademicModels.kt`: each assessment stores one `enrollmentId`.
- `export/AcademicExportManager.kt`: repeated the same invalid class-wide assessment-ID check.
- `MainActivity.kt`: the error banner was global and persisted until dismissed.

## Verification

- Backup/restore regression suite: 19/19 passed.
- Migration safety suite: 13/13 passed.
- Isolated Kotlin compilation of the changed result model and ranking engine: passed.
- Ranking smoke test: accepted separate learner assessment IDs under one shared group and rejected mixed groups.
- Full Android Gradle build/unit test and device test: pending CI/device verification. The supplied archive has no Gradle wrapper JAR and this environment has no Gradle executable.
