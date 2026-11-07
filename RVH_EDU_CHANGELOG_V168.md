# RVH Edu V168 — Restore Atomicity Regression Coverage

Version: 1.4.8  
Build: 134

## Changes
- Added `tools/check_restore_atomicity_v168.py`.
- Added a SQLite integration-style regression fixture that starts with existing school, learner, assessment and mark data, simulates a restore that deletes old rows and inserts replacement rows, then triggers a late foreign-key failure.
- The check asserts that rollback restores the exact prior rows across all four related tables and leaves no foreign-key violations.
- Added source-contract assertions that semantic and schema preflight checks occur before the destructive transaction and the final integrity audit occurs before transaction success.

## Scope and verification
This is a host-side SQLite regression fixture plus source-contract checks; it is not Android instrumentation and does not execute the app's Kotlin restore manager directly. Run with:

```sh
python3 tools/check_restore_atomicity_v168.py
```

Gradle compilation, JUnit execution, Android device testing, and CI remain unverified for this archive.
