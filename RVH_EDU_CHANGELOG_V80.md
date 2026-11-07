# RVH Edu V80 — Consistent Form Feedback

## UI changes
- Added `RvhFeedbackBanner`, a reusable inline feedback component for success, validation errors, and informational messages.
- Uses the shared Material theme's semantic container colors and shape tokens so feedback remains coherent in light and dark themes.
- Adds a polite accessibility live region so status updates can be announced without forcing focus changes.
- Applied the component to school grading settings and class progression feedback.

## Behaviour preserved
- Grading threshold validation and grading-table validation remain in place.
- Class progression success/error state handling and existing callbacks remain unchanged.
- This is a presentation and accessibility refinement; it does not change grading formulas or persistence.

## Validation limits
- Focused UI regression checks are included in `tools/check_ui_feedback_consistency.py`.
- Android compilation and physical-device rendering have not been verified in this environment.
