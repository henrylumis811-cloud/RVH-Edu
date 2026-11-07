# RVH Edu V145 — Navigation regression tests

- Version: 1.2.5 (versionCode 111)
- Adds local JVM regression tests for route-history ordering, no-op navigation to the current route, bounded history, and clearing history.
- Adds JUnit 4 as a test dependency.
- Keeps the V144 navigation-state implementation and saved workspace state.

## Verification status

- Source and ZIP structure checks: performed before handoff.
- Gradle unit tests / Android compilation: not run in this environment because the project has no Gradle wrapper and a local Gradle installation is unavailable.
- On-device navigation tests: not run.
