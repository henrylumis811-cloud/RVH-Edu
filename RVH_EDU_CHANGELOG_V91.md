# RVH Edu V91 — Live Updates and Input Feedback

## Changes
- Learner Management now reads `academicDataRevision` when creating its roster parameter, so successful add, edit, archive, restore, and other revision-backed changes trigger a fresh roster query and Compose redraw immediately.
- Add/edit learner dialogs reject blank names with a clear explanation rather than passing empty input to the data layer.
- Learner names are trimmed and internal whitespace normalized before duplicate checks and saves; blank optional admission numbers are normalized to null.
- The learner-name helper responds to the current value and explains how the name will be used on reports and certificates.
- The academic-year field filters non-digits, preserves partial typing, and explains/marks the accepted 2000–2100 range instead of silently ignoring an incomplete or invalid value.
- Added focused source regression checks in `tools/check_live_form_feedback_v91.py`.

## Verification
- Focused live-update/input-feedback source checks: 10/10 passed.
- Python syntax and archive integrity: checked at packaging time.
- Full Android build and physical-device interaction testing: not run in this environment; rerun the GitHub Actions `assembleDebug` workflow and test add/edit/archive on the target phone.
