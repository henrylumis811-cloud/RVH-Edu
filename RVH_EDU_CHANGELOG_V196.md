# RVH Edu V196 — Recovery Admin design-system refinement

## Administrator companion app
- Rebuilt the Recovery Admin screen as a polished RVH-branded administrator workspace.
- Matched the teacher app's brand blue (`#2457C5`), teal (`#087F8C`), light/dark palettes, typography hierarchy, rounded geometry, and Material 3 component language.
- Added branded top bar and hero, key/connection status tiles, numbered workflow sections, protected-key status, request entry panel, animated response panel, and explicit copy/share actions.
- Added an RVH shield launcher icon.
- Added confirmation before replacing an imported administrator key.
- Kept the Ed25519 request signing protocol, public-key pinning, Android Keystore AES-GCM protection, and offline workflow unchanged.
- Bumped only the Recovery Admin app to version 1.0.1 (versionCode 2). The teacher-facing RVH Edu app remains version 1.7.3 (versionCode 159), preserving install-over compatibility and existing signing identity.

## Validation
- Recovery Admin static/security/design checks: 17/17.
- V193 PIN recovery security checks: 13/13.
- V195 support-contact checks: 12/12.
- Gradle compilation, APK generation/signature verification, and physical-device visual testing were not available in this environment; GitHub Actions performs the Android build and APK verification.
