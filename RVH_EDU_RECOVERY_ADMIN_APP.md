# RVH Edu Recovery Admin — companion Android app

This is a separate Android application module (`:recovery-admin`) for the administrator who issues RVH Edu V1 PIN-recovery responses. It does not replace or modify the teacher-facing recovery protocol.

## Design system

The companion app follows RVH Edu's established visual system: the same academic blue (`#2457C5`), teal (`#087F8C`), light/dark surfaces, rounded Material 3 geometry, typography hierarchy and icon language. The interface is organised as an administrator workspace with a branded security hero, key status summary, numbered workflow sections, protected-key state, request editor, generated-response panel, and explicit copy/share actions. It follows the device light/dark preference.

## Build

GitHub Actions builds both APKs from the same repository:
- `RVHEdu-1.7.3-release.apk` — teacher app
- `RVHEdu-Recovery-Admin-1.0.1.apk` — administrator companion app

The workflow signs both with the configured persistent RVH signing identity and verifies both APK signatures before uploading the artifact.

## First-time administrator setup

1. Install `RVHEdu-Recovery-Admin-1.0.1.apk` on the administrator's Android device.
2. Keep the separate `RVHEdu-Administrator-Recovery-Kit.zip` out of GitHub and away from teachers.
3. Extract the PEM private-key file from the kit locally on the administrator device.
4. In the admin app, choose **Import private key** and select that PEM file.
5. The app validates the Ed25519 public key against the public key embedded in RVH Edu V1. It then encrypts the 32-byte private seed using AES-GCM with a non-exportable Android Keystore key. The PEM file itself is not copied into app storage.
6. Delete unnecessary ordinary Downloads copies only after confirming a secure offline backup exists.

## Issuing a response

1. Receive the exact recovery request from the teacher.
2. Paste the complete request into the app.
3. Tap **Generate recovery response**.
4. Copy or share the generated response back to that teacher.

The app checks request structure, 256-bit nonce, installation-ID shape, issue/expiry timestamps and the exact seven-day validity period. The signature uses the same domain-separated Ed25519 protocol as RVH Edu V1. The teacher app binds the response to its active request and consumes it once.

## Security notes

- The private key is not in the source tree, APK, or GitHub Actions secrets.
- Never commit, upload, or share the administrator kit/private key.
- Keep an encrypted offline backup. If the private key is lost, existing V1 installations cannot be recovered with a newly generated key unless their embedded public key is updated first.
- Android Keystore protects the imported key at rest on this device. Device compromise/root access remains outside this protocol's protection.
- Uninstalling or clearing the admin app's data removes its locally protected key; re-import it from the secure backup.
- The admin app signs locally and does not require an internet connection.

## Validation status

The repository's static recovery-admin checks and V193 protocol checks are run locally. A Gradle Android build, APK signing verification, and physical-device test have not been performed in this environment; GitHub Actions performs the Android build and APK verification.
