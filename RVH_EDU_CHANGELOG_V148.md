# RVH Edu V148 — List Identity & Search Continuity

Version: 1.2.8 (versionCode 114)

## Changes
- Added stable learner identity keys to the dashboard standings list so Compose can keep item identity correct when ranking/order changes.
- Persisted the learner directory search query through saveable screen-state restoration.
- Persisted the archived-learner search query through saveable screen-state restoration.
- Retains route-aware navigation and the existing per-screen SaveableStateHolder.

## Verification limits
Source-level checks and ZIP integrity can be run in this environment. Android compilation, automated test execution, Macrobenchmark/frame timing, and physical-device scrolling tests have not been run and must not be inferred from this changelog.
