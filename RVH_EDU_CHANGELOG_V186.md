# V186 — Assessment-group ranking correction

- Fixed false class-results and class-analytics blocking caused by comparing per-learner assessment database row IDs.
- Class results now carry a shared school/year/class/term/section assessment-group key for safe class-wide ranking.
- Ranking still rejects mixed assessment groups and retains the strict legacy same-assessment-ID rule for callers without a group key.
- Class export validation now accepts distinct per-learner assessment rows only when they share the same explicit group key; legacy callers retain the prior guard.
- Successful class-results and class-analytics loads clear stale global block notices.
- Added regression tests for valid per-learner assessment IDs, mixed-group rejection, and legacy validation.
- Version: 1.6.5, versionCode 151. Application ID remains `com.henrylumis.rvhedu`.

## Verification status

- Backup/restore regression suite: passed (19/19 checks).
- Migration safety checks: passed (13/13 checks).
- Android Gradle unit tests/build: not run in this environment; the Gradle wrapper JAR is absent and no Gradle executable is installed. GitHub Actions remains the build verification step.
- Physical-device verification: pending. Confirm class results, class analytics, subject analytics, and class export against the two existing learners after installing the V186 APK.
