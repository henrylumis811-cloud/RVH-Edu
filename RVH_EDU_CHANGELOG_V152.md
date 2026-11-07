# RVH Edu — V152 Change Log

## Bulk mark-entry loading responsiveness
- Moved bulk-entry roster mark/assessment reads off Compose’s main dispatcher onto the IO dispatcher.
- Collected the loaded subject values first, then applied the completed snapshot to Compose state on the main dispatcher.
- This prevents synchronous SQLite reads inside the per-learner loading loop from directly blocking UI rendering while the screen initializes or switches subject/term/section.
- Existing draft recovery remains after the database snapshot is loaded, preserving the recovery workflow.

## Version
- Version name: 1.3.2
- Version code: 118

## Validation status
- Source-level checks and ZIP integrity checks only.
- Gradle compilation, automated tests, device testing, and frame-time profiling were not run in this environment.
