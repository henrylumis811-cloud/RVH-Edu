# RVH Edu V147 — UI work reduction and scroll continuity

Version: 1.2.7 (versionCode 113)

## Changes
- Memoized learner directory filtering by roster/query, avoiding a full roster scan during unrelated recompositions.
- Memoized archived learner filtering by source/query.
- Memoized academic history filtering, including nested enrolment metadata scans.
- Retained stable item keys on high-volume LazyColumn lists and saveable per-screen state for scroll restoration.
- Kept transitions intentionally lightweight; no blanket animations were added that could increase latency on lower-memory devices.

## Verification
- Source assertions and ZIP integrity checked.
- Gradle compilation, automated test execution, frame-time profiling, and physical-device scrolling tests have not been run in this environment.
