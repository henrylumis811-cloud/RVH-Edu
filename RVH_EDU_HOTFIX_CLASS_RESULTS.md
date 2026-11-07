# RVH Edu — isolated class-results hotfix

## Scope

Fix only the false school-ownership failure that could prevent Class Results and related result calculations from loading when a learner had no saved assessment for the selected term/section.

## Root cause

`AcademicDataService.classResults()` intentionally creates an in-memory placeholder `AcademicAssessment` for a learner who has no persisted assessment in the selected sitting. `calculate()` then treated that placeholder as if it were a persisted assessment and called the school-ownership-guarded marks query. Since the placeholder has no corresponding database row, the repository correctly rejected it with `Assessment does not belong to the selected school`—but the rejection was being triggered for a legitimate no-assessment case.

## Fix

`calculate()` now reads marks only when the assessment ID matches the persisted assessment verified for that enrollment and active school. If there is no matching persisted assessment, it calculates an incomplete result using an empty mark list. The school ownership checks remain unchanged for actual persisted assessments.

## Preserved

- No database schema or data migration changes.
- No changes to PIN, authentication, learner management, grading rules, school selection, navigation, or UI.
- No new features or redesign.
- Existing application ID, version name, and version code are unchanged.
