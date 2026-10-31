# RVH Edu V1 Progress

## Latest milestone: persistent academic history

- Added immutable learner academic-history models.
- Added chronological history calculation across academic years, terms, and sections.
- Reuses the central analytics engine for learner progress instead of duplicating calculations.
- Preserves class/enrollment boundaries so promotion does not erase historical results.
- Fixed a repository cursor bug that could omit the first persisted mark when reading an assessment.

Full Android build intentionally deferred until a serious milestone.

## Latest engineering milestone
- Added persisted assessment-history access from the SQLite academic repository.
- Added a pure `ClassAnalyticsCalculator` for class averages, pass rate, extremes, subject analytics and explainable risk aggregation.
- Kept analytics independent from Compose/UI and legacy JSON.
- Full Android build intentionally deferred; pure Kotlin verification remains the current checkpoint.

## Latest milestone — V1 database backup/restore
- Added `AcademicDatabaseBackupManager` for the new SQLite academic store.
- Backup contains school, learners, enrollments, assessments, marks, and legacy identity-review records.
- Backup is versioned and schema-versioned.
- Restore is transactional and validates backup type/version before replacing data.
- Existing legacy JSON backup remains available; the new RVH Edu database backup is separate so the old and new systems can coexist safely during migration.

## OCR reliability milestone — 2026-09-03
- Added camera-image preprocessing (upright load, bounded resize, grayscale/contrast enhancement).
- Offline ML Kit OCR now evaluates both the original and enhanced image and selects the stronger structured-mark-sheet result.
- Added subject-column boundary filtering to prevent serial/admission numbers outside the subject area from becoming marks when headers are detected.
- Review screen now distinguishes **UNREAD** fields from OCR-corrected **CHECK** fields.
- Missing OCR fields are explicitly surfaced to the teacher; they are never silently interpreted as zero.
- Existing teacher review remains mandatory before OCR data can be committed.

## Latest milestone — Class Analytics UI
- Added persisted-service-backed Class Analytics screen.
- Shows class average, pass rate, complete/incomplete counts, high/low performance.
- Shows subject-level average, pass rate, high/low performance.
- Shows explainable learner risk flags from available academic history.
- Term/section selectors refresh analytics.
- Core analytics compilation verified with kotlinc.
- Full Android Gradle build remains pending because this workspace has no gradlew executable/script.

## V1 hardening milestone — dashboard migration + printable documents — 2026-09-03
- Manual dashboard learner/mark commits now write through `AcademicDataService` into the RVH Edu SQLite model instead of the legacy JSON gradebook.
- Dashboard standings are now a compatibility view over the new academic database, preventing migrated legacy records from being shown a second time.
- Name-only workflow remains supported; an exact single existing learner is reused, while same-name ambiguity is blocked for manual entry rather than silently merged.
- Dashboard final save rejects missing/invalid marks and directs incomplete work to the draft-capable mark sheet.
- Added printable report-card PDF generation from the immutable `ReportCard` model.
- Added printable academic certificate PDF generation, with incomplete results explicitly ineligible.
- Added Report and Certificate actions to complete learners in Class Results.
- Fixed class-analytics trend/risk calculations so a selected historical term/section cannot be influenced by assessments that occur later in the academic timeline.
- Added a pure Kotlin V1 regression suite covering missing marks, grading, aggregate/division, invalid marks, duplicate subjects, ranking ties/incomplete exclusion, report-card construction and certificate eligibility.
- Regression result: `RVH_EDU_V1_REGRESSION_OK`.
- Full Android/device build is still pending; this checkpoint is not yet V1-qualified.

## Latest milestone — Import Review Hardening

- Spreadsheet review rows are editable before persistence.
- Confirmation re-resolves edited learner names against the current roster.
- Same-name ambiguity remains blocked.
- Near-name matches generate a warning without auto-merging.
- Import review regression passed: `IMPORT_REVIEW_REGRESSION_OK`.

## 2026-09-03 — release-gate hardening pass
- Removed the dead legacy `HistoryScreen` source; the routed history path is now `AcademicHistoryScreen` backed by SQLite.
- Reduced `ExportManager` to the Android share-sheet adapter; class/individual academic export generation is handled by the authoritative RVH Edu export managers.
- Removed unused legacy CSV/PDF/XLSX exporter implementations so they cannot accidentally become a second export path.
- Added a visible dismissible status banner to the main app shell so import/report/certificate failures and completion messages are no longer silently stored in state.
- Corrected the V1 release-candidate regression test to match the current `Mark` model constructor.
- Pure regression suite currently passes: core V1, import parser, import review, ranking integrity, release-candidate, and integrity-policy checks.
- Full Android build remains blocked in this environment because the Gradle launcher/wrapper JAR and Android SDK are unavailable; real-device testing is still required.
