# RVH Edu V82 — Settings Form Consistency

## UI changes
- Migrated school name and class pass-threshold inputs to the shared `RvhFormTextField` component.
- Migrated aggregate and division grading-band fields to the shared form component, retaining numeric input constraints and existing editing behaviour.
- Migrated subject name, permanent code, and short-name fields to the shared component, adding concise helper text where it improves clarity.
- Preserved existing validation and save/restore flows; no database schema or academic calculation changes were made.

## Verification
- Added `tools/check_settings_shared_forms.py` to guard shared-component usage and key input constraints.
- Source-level regression checks only; a full Android Gradle build and physical-device visual test have not been performed.
