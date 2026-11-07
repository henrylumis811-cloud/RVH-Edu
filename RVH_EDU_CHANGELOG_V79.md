# RVH Edu V79 — Shared confirmation dialog refinement

## UI changes
- Added `RvhConfirmDialog`, a shared confirmation component using RVH Edu's Material theme shapes, surface/text colors, typography, and action colors.
- Standardized the unsaved-mark confirmation in the individual mark sheet and bulk mark entry.
- Destructive discard actions are visually differentiated from the safe “Keep editing” action.
- Kept the existing interaction guarantees: dismissing the dialog preserves the current edits; confirming discard follows the existing action flow, and bulk-entry recovery draft clearing occurs only after explicit confirmation.

## Scope
This is a focused consistency pass for consequential confirmation dialogs. It does not change grading, persistence, or assessment finalization logic.

## Validation
- Added `tools/check_ui_shared_dialogs.py` with source-level regression checks.
- Full Android Gradle compilation and physical-device visual testing have not been performed as part of this handoff.
