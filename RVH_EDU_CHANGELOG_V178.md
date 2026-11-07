# RVH Edu V178 — Signed Release Build Pipeline

- Replaced the debug-plus-optional-bundle workflow with a release-focused GitHub Actions workflow.
- Builds `assembleRelease` and `bundleRelease` using a required private upload keystore configured through GitHub Actions secrets.
- Verifies the release APK and AAB signatures before uploading the release artifact.
- Uploads a signed release APK for direct installation, the signed AAB for Google Play Console, and SHA-256 checksums.
- Adds `GOOGLE_PLAY_RELEASE_CHECKLIST.md` covering safe signing setup and the Play Console submission sequence.
- Version remains 1.5.7 / versionCode 143 for this build-pipeline-only update; no app behavior or version change is implied.

The project source has not been compiled in this authoring environment. The actual Android build and signing checks run in GitHub Actions after secrets are configured.
