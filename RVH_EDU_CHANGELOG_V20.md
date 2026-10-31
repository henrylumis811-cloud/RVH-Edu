# RVH Edu V20 — Export Integrity

## Existing components inspected

- `AcademicExportManager` — existing CSV, XLSX and PDF class-results exporter.
- `AcademicEngine` — source of truth for expected-subject totals, missing-subject lists and rounded averages.
- `SubjectCatalog` — existing class-specific subject list.

## Changes

- Validates class-export learner uniqueness and prevents mixing results from different assessments.
- Rejects duplicate subject marks, marks from the wrong assessment, unexpected subjects, and out-of-range marks before creating an export.
- Reconciles each export row's totals, maximum total, average, completeness and missing-subject list against the supplied marks using the existing subject catalog and calculation conventions.
- CSV-encodes text with normal quoting and neutralizes spreadsheet formula prefixes in untrusted text values.
- XLSX status now includes missing-subject names for incomplete learners instead of only saying `INCOMPLETE`.
- Added a pure regression test for CSV quoting and formula neutralization. No duplicate export manager or subject catalog was introduced.

## Verification

- `CsvCellEncoder.kt` plus `ExportCellIntegrityRegression.kt` compiled with `kotlinc` and the regression executable passed (`CSV_EXPORT_CELL_INTEGRITY_OK`).
- ZIP integrity checked after packaging.
- Full Android build and physical-device export testing have not been performed in this environment.
