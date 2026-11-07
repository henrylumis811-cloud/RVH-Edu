# RVH Edu v1.5.6 — Phone Backup/Restore Test Build

## Purpose

The GitHub-hosted Android emulator repeatedly timed out before the instrumentation test script started. This release-gate adjustment removes emulator startup from the required CI path so it no longer blocks producing an installable APK. Backup/restore verification will proceed on the physical Android phone, as requested.

## Changes

- Version updated to 1.5.6 (versionCode 142).
- Required CI still runs the host-side backup regression suite and Android unit tests.
- CI builds the debug APK and release bundle candidate.
- CI uploads the APK as `rvh-edu-phone-test-build-v<run number>`.
- Android instrumentation tests remain in the source tree but are not run in this workflow until emulator infrastructure is stable.

## Phone backup/restore smoke test

Use a test school or non-critical sample records first. Do not test restore on the only copy of real school data.

1. Install the APK from the successful GitHub Actions artifact. If Android reports that the update is incompatible because of signing, stop and report the message rather than uninstalling over data you need.
2. Open RVH Edu and select the intended school.
3. Create a small known test set: two learners and distinctive marks for at least two subjects/exam sessions, if supported by the current screen.
4. Open **Backup & Restore** and export a backup to a known location using Android's file picker.
5. Confirm the exported file exists and has a non-zero size. Keep a second copy outside the phone if possible.
6. For a safe restore test, use a disposable/test school or a test copy of the app data. Restore the backup, review the preview/confirmation, and verify school, learners, marks, exam sessions, and calculated results match the source.
7. Confirm other schools' records remain isolated and are not replaced by the selected school's backup.
8. Repeat by importing a deliberately invalid file; the app should show a clear error and preserve existing records.
9. Record the exact step and any displayed error if something fails; screenshots are useful, but hide learner personal information.

**Important:** A restore can replace data in the target scope. Keep a verified backup before confirming restore. Do not clear app storage or uninstall the installed app as part of this test.
