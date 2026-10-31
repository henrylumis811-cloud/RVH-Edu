# RVH Edu architecture map

## Application layers

- `app/src/main/java/com/henrylumis/rvhgrader/core/model/` — domain entities and academic result types.
- `core/grading/` and `core/assessment/` — grading, assessment workflows, validation and data-service operations.
- `core/repository/` and `data/` — persistence, learner directories, school profile and backup-related data access.
- `core/analytics/` — class and subject analytics calculations.
- `core/history/` — learner academic history.
- `core/importer/` — spreadsheet parsing and review models; imported rows are reviewed before being committed.
- `core/reports/` and `export/` — report/certificate models and file export.
- `ui/` — focused Jetpack Compose screens and visual theme.
- `tools/` — static regression checks and pure regression tests.

## Navigation and screen responsibilities

- `HomeScreen.kt` is the teacher-facing landing page and links directly to the most common workflows.
- `SettingsHubScreen.kt` is the single settings entry point, linking to school/grading settings, class progression, archived learners, and backup/restore.
- `SchoolGradingSettingsScreen.kt` edits the school name and currently supported grading tables.
- `LearnerManagementScreen.kt`, `BulkMarkEntryScreen.kt`, and `MarkSheetScreen.kt` handle learner registration and manual mark entry.
- `MainActivity.kt` currently owns screen routing and connects screens to the academic data service. This is a known orchestration hotspot; future refactoring should move routing/state coordination into a dedicated navigation/controller layer without duplicating business rules.

## Data and product constraints

- Offline-first operation; no OCR/AI-vision workflow.
- Learners have stable internal identities and academic history; archive/restore is preferred to destructive deletion.
- Mark validation, draft writes, and finalization must go through the existing domain service/repository guards.
- School-specific academic rules are only partially configurable. Do not silently present built-in class/subject/ranking defaults as universal policy.

## Change discipline

1. Inspect and extend existing services before introducing a new one.
2. Keep domain rules out of composable UI code where practical.
3. Add focused regression checks for important workflow invariants.
4. Run all available source-level checks for each handoff.
5. Report Android build/device verification only when it was actually performed.
