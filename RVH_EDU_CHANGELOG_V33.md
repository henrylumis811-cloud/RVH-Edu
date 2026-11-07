# RVH Edu V33 — School-specific subject lists

- Added a School Subjects screen reachable from Settings & School Setup.
- Teachers can edit lower-primary (P1–P3) and upper-primary (P4–P7) subject names, short labels and stable subject codes; each group must retain at least one subject.
- Added validation for required fields, code format, duplicate codes within a group, and duplicate display names within a group.
- Subject configuration is stored locally in SharedPreferences JSON and loaded before the academic screens are rendered.
- Added a load-defaults action and a warning to back up before changing subject lists because the active subject policy affects completeness checks for historical assessments.
- OCR remains excluded from the active workflow.

## Verification limits

Source-level integrity checks are available under `tools/`. A full Android Gradle build and device test were not performed for this change. Subject policy is still one active lower/upper catalog per installation; dated/versioned subject-policy snapshots and arbitrary class structures are not implemented yet.
