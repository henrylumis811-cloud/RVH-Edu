# RVH Edu V175 — Reliable emulator diagnostics

Version: 1.5.5  
Build: 141

## Changes
- Pre-create the emulator diagnostics directory and placeholder files before emulator startup, so artifact upload has paths even if the emulator action fails before its script begins.
- Capture emulator logcat from an EXIT trap around the instrumentation command.
- Save the connected-test Gradle output and emulator setup output as separate artifacts.
- Add an always-run finalizer that guarantees diagnostic files exist and prints the directory listing.
- Upload the entire diagnostics directory and connected-test reports on failure. Missing diagnostics now fail artifact upload only if the finalizer itself fails, rather than silently skipping all evidence.

## Verification
- Workflow structure inspected and source-level assertions passed.
- ZIP integrity checked.
- GitHub Actions, Gradle, emulator and device tests have not been run in this environment.
