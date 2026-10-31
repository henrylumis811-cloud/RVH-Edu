# RVH Edu V83 — Remaining Workflow Form Consistency

## UI changes
- Applied the shared `RvhFormTextField` to archived-learner search, including a clear hint about searchable identifiers.
- Standardized the class-progression target-year input, requesting a numeric keyboard and showing an inline error when the year is outside 2000–2100 or not later than the source year.
- Applied shared fields to learner names and subject marks in spreadsheet-import review.
- Added contextual import guidance, including the 0–100 marks range and the option to leave an unassessed mark blank.

## Preserved behaviour
- No database schema, academic calculations, or record mutation logic was changed.
- Import edits still update the in-memory review rows and remain uncommitted until the teacher confirms the import.
- Class progression continues to use the existing validation and confirmation workflow.

## Validation
- Added `tools/check_remaining_forms_shared_design.py` with 12 source-level checks.
- Full Android compilation and physical-device rendering remain unverified.
