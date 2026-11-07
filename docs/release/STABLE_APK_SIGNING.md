# Stable APK signing and in-place updates

## Why updates were being rejected

Android only accepts an APK as an update when its application ID and signing certificate match the installed app (and the new version code is higher). GitHub-hosted runners are disposable. Falling back to each runner's generated debug key can therefore produce a different certificate on every build, causing `INSTALL_FAILED_UPDATE_INCOMPATIBLE` and forcing an uninstall.

## Required GitHub Actions secrets

Add these repository secrets under **Settings → Secrets and variables → Actions**:

- `RVH_SIGNING_KEYSTORE_BASE64` — base64 contents of the private `RVHEdu-stable-signing.jks` file supplied separately.
- `RVH_SIGNING_STORE_PASSWORD` — keystore password.
- `RVH_SIGNING_KEY_ALIAS` — key alias.
- `RVH_SIGNING_KEY_PASSWORD` — key password.

The workflow decodes the keystore into the runner's temporary directory and uses the same signing identity for debug and release variants. It fails before building if any secret is missing. Never commit the keystore, its passwords, or a decoded copy to the repository.

## Important transition limitation

This makes builds consistent from the first build signed with this key onward. It cannot retroactively make this new key match an APK already installed with a different certificate. If the currently installed RVH Edu was signed with a previous ephemeral GitHub runner key, Android will still reject the first APK signed with this persistent key. In that case, a one-time migration is unavoidable unless the original signing keystore can be recovered. Back up/export school data and verify the backup before any uninstall. After installing the first APK signed with this persistent key, subsequent updates should install in place as long as the application ID remains `com.henrylumis.rvhedu` and versionCode increases.

## Release checks

The workflow verifies APK signature integrity with `apksigner`. On the phone, test an in-place update over the current installation before treating it as a release. Do not clear app storage or uninstall as a troubleshooting shortcut; preserve and verify school backups first.
