# RVH Edu V179 — Direct-install release APK

- App version: 1.5.8 (versionCode 144)
- Package ID: `com.henrylumis.rvhedu`
- Purpose: signed release APK for direct installation on Android; no Play Store AAB is built.
- GitHub Actions runs the host backup/restore regression suite, Android unit tests, builds `assembleRelease`, verifies the APK signature, and uploads the APK plus SHA-256 checksum.

## Signing note

To avoid requiring Play Store credentials, the workflow signs with a persistent release key if the existing `RVH_RELEASE_*` secrets are configured; otherwise it uses the GitHub runner's debug signing key. The fallback is suitable for a first direct-install trial but is not stable across fresh GitHub runners. Future APKs signed with a different key will not install as updates over this APK; Android may require uninstalling the old app, which deletes its private local data. For durable updates, configure and securely retain one private keystore and use it for every release. Never commit a keystore or passwords into the repository.

## Build status

The ZIP itself is source, not a compiled APK. The actual release APK is produced only after the GitHub Actions workflow completes successfully.
