# RVH Edu V73 — First-launch personal PIN setup

- Made the first-launch requirement explicit in the entry screen: a user must create and confirm a personal 4–8 digit access PIN before the app unlocks.
- New PINs are stored as salted PBKDF2-derived credentials instead of plaintext PIN strings. A fresh random salt is generated for each credential and verification uses constant-time byte comparison.
- Existing installations with the previous plaintext PIN remain compatible: after the correct PIN is entered, the credential is upgraded to salted derived storage and the old plaintext preference is removed.
- The app remains offline-first; no account or network connection is required for PIN setup.
- Added a pure-JVM regression suite for credential generation/verification and a source regression guard for first-launch gating and migration.

## Validation

- `PinCredentialRegression.kt`: 12 runtime assertions passed with the installed Kotlin/JVM compiler.
- `tools/check_first_launch_pin_setup.py`: 13 checks passed.
- Full Android Gradle build and physical-device testing are not yet verified.

## Security note

This PIN gate is local app-level protection, not a replacement for Android device security or hardware-backed authentication. Anyone with control of a rooted/unlocked device or access to app data may still be able to bypass local protections.
