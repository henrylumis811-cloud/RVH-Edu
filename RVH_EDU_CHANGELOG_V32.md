# RVH Edu V32 — Academic catalog consistency

## Changes
- Centralized the subject-code and short-label source used by `GradingLogic` in `SubjectCatalog`.
- Kept the existing `GradingLogic` properties as compatibility aliases, reducing the risk of changing all callers at once.
- Added catalog integrity validation for empty subject groups, missing labels/codes, and duplicate codes within a class-mode list.
- Added a source-level regression check to prevent the old duplicated subject lists from returning.

## Important scope note
This is an architectural preparation step, not yet a school-configurable subject editor. The current subject catalog remains built-in and the lower/upper primary computation modes remain fixed. Before enabling arbitrary subjects or subject counts, the calculation engine, completeness checks, reports, imports, analytics, and historical records must be migrated together so existing results are not silently reinterpreted.

## Verification
Run `python3 tools/check_subject_catalog_single_source.py` and all `python3 tools/check_*.py` checks. A full Android Gradle build/device test is not asserted by this changelog.
