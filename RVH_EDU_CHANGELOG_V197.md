# RVH Edu V197 — Persistent PIN recovery requests

- Teacher app version: **1.7.4** (versionCode **160**).
- A pending PIN recovery request is now restored from the app's private SharedPreferences when the PIN gate is recreated, including after the teacher closes RVH Edu, switches to WhatsApp/Telegram/email, or restarts the phone.
- Opening **Forgot PIN?** reloads the active request from persistent storage rather than silently requiring a new request.
- The existing seven-day expiry, installation binding, 32-byte random nonce, Ed25519 signature verification, one-time consumption and explicit replacement behavior remain in force.
- Expired or malformed pending requests are cleared safely; PIN credentials and school data are not modified by this cleanup.
- Recovery Admin is unchanged; no administrator key rotation or re-import is required.

## Validation

- Static recovery lifecycle checks added/run for persistent active-request restoration, expiry handling, installation binding and replacement semantics.
- Existing V193 recovery security checks and other project regression checks run where supported.
- This environment has no Gradle wrapper/executable, so an Android compile/APK build and TECNO Spark 30C runtime test must be completed by GitHub Actions/device testing.
