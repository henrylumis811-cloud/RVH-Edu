# RVH Edu V197 — Final Results & School-Scope Hotfix

## Scope
This is a narrowly scoped maintenance patch to the supplied V197 source. It resolves the three edge cases identified in the final class-results/school audit. It does not upgrade the app or change its package/version identity.

## Changes

1. **Stale active-school preference**
   - The app no longer silently redirects a stale multi-school selection to `legacy-school`.
   - A missing preference resolves to the legacy school when present (preserving the original single-school path), or to the only registered school.
   - A stale preference is repaired automatically only when exactly one school exists.
   - If multiple schools exist and the saved selection is stale, academic operations stop with a clear selection-required message instead of reading/writing under the wrong school.
   - Non-academic startup branding reads tolerate an unresolved selection, and the Schools screen remains usable so the teacher can explicitly choose the correct school.

2. **Draft versus official results**
   - Class Results, ranking, and class analytics now use only `FINAL` assessments.
   - A learner with no finalized assessment (including a learner with a saved DRAFT) remains represented as incomplete; draft marks do not leak into official totals, averages, pass rates, or positions.
   - Policy compatibility checks compare only finalized assessments, so an unrelated draft policy cannot block official class results.
   - Drafts remain saved and editable in mark-entry workflows; no marks are deleted or rewritten.

3. **Archived learners**
   - Current class results and class analytics now use only enrollments whose learner is `ACTIVE` in the selected school.
   - Archived learners' historical records remain stored and untouched; they are simply excluded from current active-class calculations.

## Preserved
- Package/application ID and version code/name.
- Database schema and existing learner, enrollment, assessment, and mark rows.
- School ownership verification and assessment-to-enrollment checks.
- Grading snapshots and grading calculations for finalized assessments.
- Existing UI design and unrelated features.

## Validation
- Source-level inspection of changed call paths completed.
- ZIP integrity check completed.
- Android compilation, automated tests, SQLite integration tests, and physical-device testing were not run in this environment (no Gradle executable or Gradle wrapper is present).
