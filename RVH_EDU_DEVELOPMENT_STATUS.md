# RVH Edu Development Status

## Foundation v0.1 — 2026-09-02

### Completed in this checkpoint
- Added a normalized local SQLite schema for RVH Edu.
- Added core academic models: Learner, Enrollment, AcademicAssessment, Mark.
- Added LearnerRepository for durable learner storage.
- Added a one-time migration layer from the existing `gradebook.json` format.
- Wired the migration to run when the app starts.
- Kept the legacy JSON gradebook intact; migration is non-destructive.
- Added database indexes and integrity constraints for marks.

### Legacy systems intentionally preserved
- Existing grading engine
- Existing JSON gradebook
- Legacy OCR pipeline (scheduled for removal; no longer part of RVH Edu v1)
- Existing export pipeline
- Existing backup/restore
- Existing Compose UI

### Next checkpoint
1. Add EnrollmentRepository and AssessmentRepository.
2. Build a database-backed AcademicRepository that can load a complete learner history.
3. Add tests for migration and duplicate/edge cases.
4. Introduce a compatibility service so current screens can gradually read from the new database.
5. Only then begin replacing the old `StudentRecord` flow.

### Important rule
Do not delete or disable the legacy JSON system until database migration, backup/restore compatibility, and result-calculation parity have been verified.

## Foundation checkpoint 2
- Added pure `AcademicEngine` for totals, percentages, completeness, aggregate and division.
- Added pure `RankingEngine` with competition ranking and incomplete-result protection.
- Added `AcademicRepository` for normalized enrollments, assessments and transactional mark replacement.
- Added `AcademicResult` and `RankedResult` models.
- No UI migration yet: legacy screens remain authoritative until the new academic pipeline is verified.


## Identity principle

RVH Edu must work for schools that do not use formal learner/admission IDs. A learner's database identity is internal and invisible to the teacher. The teacher-facing minimum is a name; age, gender, class, stream, guardian details and an admission number are optional supporting information. Admission numbers must never be required to create, enroll, assess, rank, report on, or search for a learner. Where names are ambiguous, the UI should ask the teacher to distinguish learners rather than silently merging records.


## Foundation v0.2 — Inclusive Learner Identity
- Admission numbers are optional; name is the only required teacher-entered identity field.
- Added `LearnerDirectory` for simple name-first learner creation and ambiguous-match lookup.
- Added legacy identity review records so same-named legacy learners are never silently merged.
- Legacy migration now scopes generated identities by normalized name + academic year + class and flags cross-context same-name collisions for review.
- Database version is now 3 with `legacy_identity_review`.
- No full Android build was run; the pure academic engine was Kotlin-compiled successfully.


## Mark-entry redesign — 2026-09-13
- Removed OCR and AI mark-entry code from the active application.
- Removed the AI Vision settings screen and camera-based mark-entry flow.
- Removed the bundled ML Kit OCR dependency and network permission previously used by the optional AI path.
- Preserved manual mark entry, bulk marks, spreadsheet import/review, comments/observations, grading, analytics, reports, certificates, backup/restore and offline operation.
- Mark entry is now deterministic: RVH Edu never guesses academic marks from photographs.
