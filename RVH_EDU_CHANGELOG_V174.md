# RVH Edu V174 — Emulator crash diagnostics

Version: 1.5.4 (versionCode 140)

## Why this change

The previous CI run compiled the app and test APKs, but `connectedDebugAndroidTest` reported that the instrumentation process crashed before individual tests completed. That summary alone does not identify the root cause.

## Changes

- Clear emulator logcat immediately before the instrumentation run.
- Preserve the full emulator logcat after the run, including when Gradle fails.
- Print crash-focused logcat context into the Actions log when instrumentation tests fail.
- Upload emulator logcat and available connected-test reports as a diagnostic artifact on failure/cancellation.
- Wipe emulator data before the run to reduce state contamination between CI runs.
- Keep the existing API 35 emulator and production instrumentation tests; no speculative application behavior changes were made.

## Verification limitations

The workflow has been updated but was not executed in this environment. The next GitHub Actions run should produce the logcat artifact needed to identify the actual process crash.
