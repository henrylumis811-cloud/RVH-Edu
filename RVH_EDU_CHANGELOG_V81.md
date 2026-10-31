# RVH Edu V81 — Shared Form Components

## UI changes
- Added `RvhFormTextField`, a reusable Material 3 form-field component with consistent full-width sizing, minimum height, helper text, validation styling, configurable keyboard type, and IME action.
- Applied the component to learner creation, learner editing, and archive-reason/note forms.
- Age entry now explicitly requests a numeric keyboard; multiline archive notes retain the 500-character limit.
- Save failures use the existing shared feedback banner rather than unstructured text, while archive-reason validation is associated directly with the reason field.

## Behaviour preserved
- Learner duplicate detection and review remain in place.
- Learner creation/edit callbacks and persistence logic are unchanged.
- Archive reason remains required; archive notes remain optional and capped at 500 characters.
- No grading calculations or academic record schemas were changed.

## Validation limits
- Added `tools/check_ui_shared_form_fields.py` with 11 source-level assertions.
- Existing learner-status audit check passes.
- Python tool compilation passes.
- A full Android Gradle build and physical-device visual review remain outstanding; this archive has no Gradle wrapper and a system Gradle installation is not available.
