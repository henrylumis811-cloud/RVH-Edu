# RVH Edu V159 — Bulk-save interaction safety

Version: 1.3.9  
Build: 125

## Changes
- Prevents leaving Bulk Mark Entry while a mark-save batch is running.
- Disables mark editing and clear-mark actions during an active save, avoiding edits that race with the in-flight save snapshot.
- Disables term, section, and subject switching until the save batch completes, keeping the save context stable.
- Preserves the existing partial-failure reporting and retry behaviour.

## Verification
- Source-level assertions and ZIP integrity checked.
- Android compilation, automated tests, and device testing have not been run in this environment.
