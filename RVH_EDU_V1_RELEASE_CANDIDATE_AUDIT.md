# RVH Edu V1 Release-Candidate Audit

## Verified in source
- Name-first learner creation; admission number is optional.
- New academic SQLite model is authoritative for dashboard saves, class results, history, imports, OCR-reviewed marks, analytics and academic exports.
- Final assessments are locked; reopening explicitly returns them to DRAFT.
- Incomplete results are visible but unranked.
- Missing marks are represented as missing, never silently converted to zero.
- Duplicate-name ambiguity is surfaced rather than silently merged.
- Legacy gradebook is retained only for migration/compatibility code and old unused UI/export classes.
- SQLite backup/restore validates backup/schema versions and performs restore transactionally.
- Database integrity audit checks foreign keys, orphans, invalid marks/enums and finalized incomplete assessments.
- Report card and certificate document models are wired to PDF generation.
- Certificate generation rejects incomplete results.
- Spreadsheet import has a review-before-write workflow.
- Historical analytics are cut off at the selected assessment; later results cannot affect an earlier snapshot.

## Known release blockers
1. A full Android Gradle build has not been executed in this environment because the project has no `gradlew` launcher and no system Gradle installation is available.
2. Physical-device testing is still required, including database backup/restore, file sharing, camera/OCR, PDF rendering, navigation and process-death recovery.
3. OCR must be stress-tested against real troublesome mark sheets. Synthetic parser checks cannot establish field-level OCR accuracy on camera images.
4. Legacy compatibility classes remain in the source tree. They are not authoritative, but should be retired after device validation confirms the new path.
5. Final UI polish/accessibility review remains required before declaring V1 qualified.

## Qualification rule
RVH Edu V1 is **not qualified yet**. Qualification requires the blockers above to be cleared and the resulting APK to pass end-to-end device testing with data-integrity verification.

## Final adversarial pass — 2026-09-03

- Manual dashboard final-save path now validates all required marks and mark ranges before creating a learner/enrollment. A failed submission therefore cannot leave a half-created learner caused solely by validation failure.
- Duplicate same-name candidates remain blocked rather than silently merged.
- Blank marks remain distinct from zero.
- Finalization remains gated on complete required subjects.

## Release-gate hardening pass — 2026-09-03 (continued)
- Removed the unrouted legacy `HistoryScreen` implementation.
- Removed legacy academic export generators; `ExportManager` now only performs file sharing while `AcademicExportManager` / `AcademicDocumentExportManager` generate V1 documents.
- Added a visible dismissible global status banner for import/report/certificate outcomes.
- Corrected the release-candidate pure test to the current `Mark` constructor.
- Pure regression checks pass after these changes.
