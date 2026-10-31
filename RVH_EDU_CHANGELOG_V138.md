# RVH Edu v1.1.8 — CI Android SDK setup fix

## Fix
- Updated `.github/workflows/android-build.yml` to explicitly request only `platform-tools` from `android-actions/setup-android@v3`.
- This avoids the action's legacy default request for the retired Android SDK `tools` package, which causes `sdkmanager` to fail with `Failed to find package 'tools'`.
- Android command-line tools (including `sdkmanager`) are installed by the setup action itself. Android platform and build-tools packages remain installed/resolved by the existing SDK installation step and Gradle build.

## Versioning
- App version name: 1.1.8
- App version code: 104

## Verification status
- Workflow change checked in the source file and ZIP contents.
- This fixes the reported setup-android failure path, but a new GitHub Actions run is still required to verify the remote workflow and subsequent Gradle build.
