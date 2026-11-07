# RVH Edu V149 — Bulk Mark Entry UI Performance

Version: 1.2.9 (versionCode 115)

## Changes
- Consolidated entered, missing, and changed mark counts into a single derived roster pass.
- Debounced autosave-recovery draft serialization to avoid serializing the entire roster on every keystroke.
- Retained immediate recovery persistence for save failures and the existing save/discard behavior.
- Preserved prior navigation, list identity, and screen-state restoration changes.

## Verification status
- Source-level checks and ZIP integrity only.
- Android compilation, automated test execution, and on-device frame-time profiling have not been performed for this revision.
