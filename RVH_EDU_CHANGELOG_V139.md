# RVH Edu V139 — Gmail feedback prefill fix and release-candidate handoff

## Fix
- Changed email feedback routing from `ACTION_SENDTO` with a `mailto:` URI to an explicit email-sharing intent (`ACTION_SEND`, `message/rfc822`).
- Explicitly supplies the recipient, subject, and complete feedback body so Gmail can populate the compose screen.
- WhatsApp routing remains unchanged.

## Versioning
- App version name: 1.1.9
- App version code: 105

## Release status
- Source updated from the supplied V138 archive.
- Android build and Gmail-on-device verification have not yet been run for this change.
- The existing CI workflow builds a release Android App Bundle (`bundleRelease`).
- A Play-uploadable AAB must be signed with the configured release upload key. Do not upload an unsigned candidate.
- Before production submission, complete the release checklist: verify CI build/tests, test email and WhatsApp sharing on-device, publish the privacy policy at a public HTTPS URL, and complete Play Console listing/Data safety/target-audience declarations.
