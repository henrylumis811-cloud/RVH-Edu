# RVH Edu V197 — Mark-entry keyboard visibility hotfix

Scope: keyboard visibility only in the existing per-learner and bulk mark-entry screens.

Changes:
- Apply Compose IME bottom insets to the per-learner MarkSheet screen content.
- Apply Compose IME bottom insets to the Bulk Mark Entry screen content.
- The content viewport now resizes above the software keyboard, keeping the active mark-entry area and available controls reachable instead of allowing the keyboard to cover them.
- Preserve the existing numeric keyboard, validation, save/finalize behavior, assessment selection, data model, app identity, and all other features.

The Android activity already declares `windowSoftInputMode="adjustResize"`; that setting is retained.

Verification:
- Source-level change applied to both mark-entry screens.
- ZIP integrity can be checked independently.
- Android Gradle compilation and physical-device verification remain required; they are not claimed here.
