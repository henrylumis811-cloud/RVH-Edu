# RVH Edu V93 — Mark-entry save feedback

- Single-learner mark entry now dismisses the software keyboard and clears input focus when saving a draft, finalizing an assessment, or reopening a finalized assessment.
- Bulk mark entry dismisses the keyboard and clears focus before attempting to save changed marks, including when some rows fail validation.
- Corrected feedback styling so “Draft saved” and “Reopened for editing” are displayed as successful outcomes rather than errors.
- Kept existing unsaved-change protection, missing-mark semantics, partial-save reporting, and academic validation behavior intact.

Validation: focused source assertions and archive integrity checks are available; a full Android build and physical-device interaction test remain outstanding.
