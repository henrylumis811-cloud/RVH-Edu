# RVH Edu V197 — Mark-entry keyboard visibility hotfix

## Scope

This maintenance change is limited to the existing Bulk Marks and individual Mark Sheet screens. It does not change the app identity, version, database schema, grading rules, saved marks, school ownership rules, or unrelated features.

## Root cause addressed

The previous change applied IME padding and allowed the screen to resize, but left the large fixed header and metadata sections in place while the keyboard was open. This left the independently scrolling learner/subject list with too little vertical space. The focused field also had no explicit bring-into-view request.

## Changes

- Detect the visible IME using Compose window insets.
- Collapse nonessential header, assessment-selection, search, and status content while the keyboard is visible so the mark list receives the available height.
- Attach a `BringIntoViewRequester` to every bulk learner mark field and individual subject mark field.
- Request bring-into-view after focus settles, including when moving to the next field.
- Retain IME padding and the existing save/finalize actions.
- Restore the full existing screen content when the keyboard closes.

## Verification

- `tools/check_v197_keyboard_focus_visibility.py`: source-level regression checks.
- Existing bulk-mark UI/layout checks: run separately.
- Android Gradle compilation and physical-device validation: required; this environment does not contain a Gradle wrapper or installed Gradle executable.
