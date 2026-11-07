# RVH Edu V25 — Protect Unsaved Bulk Marks

## Existing components inspected

- `BulkMarkEntryScreen` — existing bulk marks entry UI and per-learner dirty tracking.
- Existing `saveDraft` callback — explicit persistence of changed marks.
- Existing term, section, subject and back navigation handlers.

## Problem addressed

The bulk marks screen already tracked changes by comparing the current input with `savedValues`. However, changing subject, term, section, or leaving the screen triggered the existing reload effect, which cleared the current values. Unsaved marks could therefore be lost without a warning. The status text also said changes were saved "locally," which could be mistaken for automatic persistence even though saving requires the Save button.

## Changes

- Added a confirmation dialog before changing subject, term, section, or leaving the screen when edits are unsaved.
- The dialog offers **Keep editing** or **Discard changes**; it does not silently discard marks.
- When there are no edits, navigation proceeds normally without prompting.
- Changed the no-dirty-state message to `No unsaved changes.`
- Reused the existing dirty-state tracking and save workflow; no duplicate state manager or persistence layer was added.

## Validation

- `tools/check_bulk_marks_unsaved_guard.py`: passed (`BULK_MARKS_UNSAVED_GUARD_OK`).
- Existing class-progression, enrollment, learner-creation, learner-edit and assessment-write integrity checks: passed.
- ZIP integrity: checked after packaging.

## Validation limits

No full Android build or device interaction test was performed in this environment. The checks validate source structure and expected guards, not runtime Compose behaviour.
