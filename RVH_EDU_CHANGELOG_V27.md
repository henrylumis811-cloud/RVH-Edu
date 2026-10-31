# RVH Edu V27 — Mark Sheet Safety

## Changes
- Added unsaved-change tracking to the individual learner mark sheet.
- Warns before leaving the sheet or switching term/section when unsaved edits would be discarded.
- Shows explicit draft completeness and names subjects still missing.
- Prevents finalization from the UI while required subjects are missing; service-side validation remains authoritative.
- Keeps the existing `MarkSheetScreen`, `AssessmentWorkflow`, and `AcademicDataService`; no parallel marks workflow was introduced.

## Validation
- `tools/check_mark_sheet_guard.py` source-level checks pass.
- Existing source integrity scripts are run as part of this handoff.
- Full Android build and device/runtime testing were not available/performed; this changelog does not claim they passed.
