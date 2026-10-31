# RVH Edu V102 — Active-school academic scoping

## Changes
- `AcademicDataService` resolves the active school through `SchoolManagementRepository` for enrollment creation and class progression.
- Class roster, class results, progress summaries, policy review, and roster reads now explicitly request enrollments for the active school.
- `ClassRosterRepository` uses the selected school rather than relying on the legacy-school default.
- Existing academic records remain assigned to their current school; switching schools does not move or merge records.

## Verification limits
- Source-level checks and archive integrity only; Android compilation and on-device testing have not been performed.
- Learner identity and admission-number uniqueness are still global, and some assessment-by-ID operations rely on enrollment IDs. Complete tenant isolation, school-aware learner management, and backup/restore coverage remain future work.
