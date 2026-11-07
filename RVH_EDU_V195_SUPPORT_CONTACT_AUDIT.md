# V195 support-contact implementation audit

## Included
- WhatsApp support: +256786997653 via `https://wa.me/` with the generated request prefilled.
- Telegram support: +256786997653 via Telegram phone deep link with the generated request prefilled; falls back to Telegram's share intent if direct resolution is unavailable.
- Email support: henrylumis811@gmail.com with subject and generated request prefilled.
- The recovery request is generated before any channel action; the same request string is sent to the administrator.
- The teacher app version advances from 1.7.2 / 158 to 1.7.3 / 159. Application ID and signing environment variable names remain unchanged for install-over updates.
- GitHub Actions workflow is included at `.github/workflows/android-build.yml` and stages both teacher and administrator APKs.

## Security and data
- The private Ed25519 signing key is not part of the teacher app source or APK.
- The existing V193 Ed25519 protocol is unchanged.
- Contact actions only share the recovery request. They do not export school data or learner records.

## Verification status
- Static source checks are run in this handoff.
- Android Gradle compilation, release APK signing, APK verification, and physical-device testing have not been run in this environment. GitHub Actions must confirm those steps.
- Telegram phone-deep-link behavior depends on Telegram's installed version, contact discoverability, and privacy settings; the share fallback is provided.
