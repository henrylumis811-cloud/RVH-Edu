# RVH Edu V31 — Navigation State and School Pass Policy

## Changes
- Extracted the shared screen route and academic workspace selections (class, term, section, year) from `MainActivity` into `ui/AppNavigationState.kt`.
- Added a persisted school-specific class pass threshold setting (0–100%).
- Connected the threshold to class analytics, where both overall class pass rate and subject pass statistics are calculated.
- Kept the threshold distinct from mark-to-aggregate/division tables; changing it does not rewrite marks or grades.
- Added source-level regression checks for navigation state ownership and pass-threshold persistence/wiring.

## Boundaries
- This is an incremental architecture step, not a claim that `MainActivity` has been fully decomposed into feature modules.
- The threshold currently affects analytics only. It does not automatically redefine grading aggregates, divisions, or ranking rules.
- Class/subject catalog configuration, assessment schedules, and ranking policy remain future school-specific setup work.
- No OCR screen or OCR mark-entry workflow has been reintroduced.
