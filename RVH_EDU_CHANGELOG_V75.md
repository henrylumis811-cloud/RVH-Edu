# RVH Edu V75 — Mark-entry interface refinement

## UI changes
- Reorganized bulk mark entry around a clear class/year heading and learner-count summary.
- Grouped term, assessment section, and subject selectors in a compact assessment-period card; selector rows scroll horizontally on narrow screens.
- Made entered/missing counts and unsaved-state messaging easier to scan.
- Presented learner rows as consistent cards with initials avatars and aligned numeric mark fields.
- Kept blank marks distinct from zero and retained the 0–100 input constraint.

## Behaviour preserved
- Unsaved mark draft recovery and discard confirmation.
- Subject/term/section switching safeguards.
- Save-only-changed-rows behaviour and per-learner error reporting.
- Existing academic calculation and storage logic was not changed.

## Validation
- Added `tools/check_ui_mark_entry_polish.py` for source-level UI regression checks.
- Full Android build and physical-device visual testing have not been verified in this release.
