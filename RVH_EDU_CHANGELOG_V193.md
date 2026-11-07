# RVH Edu V193 — Secure PIN Recovery

- Added offline administrator-assisted recovery using Ed25519 signatures.
- Requests include a random 256-bit nonce, per-installation UUID, timestamps and seven-day expiry.
- APK embeds only public verification key; administrator private key is separate.
- Responses are request-specific 64-byte signatures; no universal/master PIN.
- Valid response is consumed once and resets only the PIN credential.
- Existing learner and assessment data are preserved.
- Version 1.7.2, versionCode 158.
