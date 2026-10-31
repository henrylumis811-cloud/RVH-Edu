# RVH Edu V74 — Learner and settings UI refinement

## Learner directory
- Reworked the learner screen into a clearer directory layout with a class/year summary, searchable roster, count context, and a prominent bulk-mark action.
- Added a designed empty-roster state and a distinct no-search-results state with a clear-search action.
- Replaced the cramped cluster of per-row action buttons with an initial-avatar identity row, a primary mark-entry action, and an overflow menu for profile, edit, and archive actions.
- Kept the existing add/edit duplicate-review, archive-reason, profile, marks, class selection, and year selection callbacks wired.

## School settings
- Grouped settings into School Configuration and Records & Safety sections.
- Added a branded introductory panel, icon surfaces, clearer descriptions, and consistent card geometry.
- Preserved all existing settings navigation callbacks and did not imply unsupported settings exist.

## Validation limits
- Added `tools/check_ui_learner_settings_polish.py` for source-level UI/navigation guards.
- These checks do not replace Android compilation, screenshot inspection, accessibility testing, or real-device interaction testing. No Gradle wrapper is present in this project archive, and no full Android build is claimed for this release.
