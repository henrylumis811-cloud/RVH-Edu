# RVH Edu Development Guide

## Project layout

- `app/src/main/java/com/henrylumis/rvhgrader/` — application source, grouped by feature and responsibility.
- `app/src/test/` — JVM unit tests.
- `app/src/androidTest/` — device/emulator tests, when configured for the target environment.
- `tools/` — deterministic host-side regression and source-contract checks used by CI.
- `.github/workflows/android-build.yml` — required test/build/release-artifact workflow.
- `docs/maintenance/` — current developer and recovery procedures.
- `RVH_EDU_CHANGELOG_*.md` and `RVH_EDU_MULTI_SCHOOL_*.md` — retained historical implementation records referenced by README and regression checks; these are history, not runtime application files.

## Local validation

Run these from the project root:

```sh
python3 tools/check_legacy_migration_safety.py
python3 tools/run_backup_regression_suite.py
gradle testDebugUnitTest --no-daemon
gradle assembleRelease --no-daemon
```

The Android build requires JDK 17, Android SDK platform 36, build-tools 36.0.0, and Gradle 8.11.1. The CI workflow installs the Gradle distribution because the current project archive does not include the Gradle wrapper script/JAR. A source-level check passing is not a substitute for a successful Android compile or a device test.

## Release rules

1. Increase `versionCode` for every installable update and update `versionName`.
2. Keep workflow APK filename and artifact label aligned with Gradle metadata.
3. Never commit signing keys, passwords, local SDK paths, generated APKs, or private learner data.
4. Before release, run CI, install over the existing app using the same signing key, and test PIN unlock, migration, add/edit learner, mark entry, export, and restore.
5. Never ask a school to uninstall or clear app storage as a routine troubleshooting step.
