# RVH Edu V155 — Asynchronous Bulk Mark Saving

## Changes
- Runs the bulk-mark save loop on the IO dispatcher so synchronous SQLite reads/writes do not block the Compose UI thread during the save action.
- Captures edited values on the UI thread before background work and applies save outcomes back on the UI thread.
- Prevents duplicate save taps while a save batch is running and exposes a saving state in the button.
- Keeps per-learner error reporting and updates the saved baseline only for successful writes, allowing failed entries to remain retryable.
- Preserves the existing changed-row-only behavior and draft-recovery handling.

## Version
- Version name: 1.3.5
- Version code: 121

## Verification status
- Source assertions and ZIP integrity checks: run when packaging.
- Gradle compilation, automated tests, and physical-device performance checks: not run in this environment.
- Draft recovery is retained if the teacher edits a field while a save batch is in progress; successful writes only advance the saved baseline to the captured value.
