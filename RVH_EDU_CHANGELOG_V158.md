# RVH Edu V158 — Draft Snapshot Efficiency

**Version:** 1.3.8  
**Build number:** 124

## Changes
- Replaced eager `toMap()` expressions used as Compose effect keys with remembered derived snapshots for current and saved bulk-mark values.
- The draft-recovery debounce now reacts to observable snapshot changes rather than copying the full roster maps on every unrelated recomposition.
- Preserved the existing 300 ms recovery debounce and draft comparison behaviour.

## Verification
- ZIP archive integrity and targeted source/version assertions checked.
- Android compilation, automated tests, and device profiling have not been run in this environment.
