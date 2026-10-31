# Installing RVH Edu builds as updates

Android accepts an in-place APK update only when the `applicationId` is unchanged, the APK is signed with the same signing certificate as the installed app (or valid key-rotation proof applies), and the new `versionCode` is not lower. See the official [Android app update guide](https://developer.android.com/google/play/app-updates) and [app signing guide](https://developer.android.com/studio/publish/app-signing).

RVH Edu keeps `applicationId = com.henrylumis.rvhgrader` and increments `versionCode` for this checkpoint. GitHub-hosted runners are fresh machines, so their default debug signing key is not stable between workflow runs. That is why a downloaded APK may demand uninstalling the previous build. Uninstalling can erase local app data.

## One-time setup for stable CI signing

**Do not commit a keystore or passwords to Git.** Create a dedicated development signing key on a trusted computer and keep a secure backup. Example:

```bash
keytool -genkeypair -v -keystore rvh-edu-debug.jks -alias rvh-edu-debug -keyalg RSA -keysize 2048 -validity 10000
base64 -w 0 rvh-edu-debug.jks
```

On macOS, use `base64 < rvh-edu-debug.jks` instead of `base64 -w 0`. Add these four values in GitHub repository **Settings → Secrets and variables → Actions → New repository secret**:

- `RVH_DEBUG_KEYSTORE_BASE64`: base64 output of the keystore file.
- `RVH_DEBUG_STORE_PASSWORD`: keystore password.
- `RVH_DEBUG_KEY_ALIAS`: `rvh-edu-debug` (or the alias you selected).
- `RVH_DEBUG_KEY_PASSWORD`: password for that key.

The workflow uses this persistent key to sign subsequent debug APKs. Keep the keystore and credentials safe and use the same values for every build intended to update the same installation. The workflow intentionally falls back to the default debug key if secrets are missing, but those builds are not guaranteed to update installations from other CI runs.

## Important first-install limitation

If the currently installed APK was signed by a different, ephemeral CI debug key, it cannot be updated with the new persistent key. Make a backup of any important school data first, then uninstall that one old build and install the first APK signed with the persistent key. After that transition, later APKs signed with the same key and higher `versionCode` should install over the app and preserve its private data, provided database migrations are safe.

For a future public release, create and protect a separate release signing key; do not reuse a debug key as the production release identity.
