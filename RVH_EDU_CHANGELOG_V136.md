# RVH Edu V136 — Release-readiness hardening

- Raised compileSdk and targetSdk to API 36 for current Google Play target requirements.
- Updated Android Gradle Plugin to 8.10.1 and CI Gradle to 8.11.1.
- Added explicit Android cloud-backup/device-transfer exclusions for app-private databases, preferences and files; RVH Edu's user-controlled export/restore remains the intended backup path.
- Removed automatic hourly audience/update polling and the unconfigured manual update endpoint UI. The app now explains that updates will be delivered through Google Play.
- Added a privacy-policy draft in `PRIVACY_POLICY.md` and an in-app-bundled HTML copy in `app/src/main/assets/privacy_policy.html` for review and publication.
- Updated CI to run unit tests and build both a debug APK and release AAB candidate. Release upload signing is sourced from GitHub Actions secrets only; without secrets, the AAB is unsigned and must not be uploaded to Play Console.
- Version set to 1.1.6 (version code 102).

Validation in this environment: source/manifest/workflow static checks and ZIP integrity only. Full Android Gradle build and device tests have not run because Gradle/Android SDK are not installed here and network access to download Gradle is unavailable. AMGP remains an open product requirement: the current codebase does not implement the previously discussed D/C/P/F-based AMGP formula, and the grade-category thresholds must be agreed before wiring it into live reports.
