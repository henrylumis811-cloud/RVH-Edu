# RVH Edu V193 — Secure PIN Recovery Audit

## Release
- Version: 1.7.2
- versionCode: 158
- Application ID: com.henrylumis.rvhedu (unchanged)
- Signing configuration: retained; existing GitHub signing secrets are not modified.

## Security design
- Ed25519 asymmetric signatures; the APK contains only the raw public verification key.
- Administrator private key is distributed separately in the administrator kit and is not present in the source archive or Android project.
- Each request contains a random 256-bit nonce, installation UUID, issue time and seven-day expiry.
- The exact encoded request is signed with domain separation (`RVH-EDU-PIN-RECOVERY-V1:`).
- Response is a 64-byte Ed25519 signature (base64url), not a short numeric code or predictable checksum.
- App verifies installation binding, request structure, nonce size, time window, signature, active-request equality and one-time consumption.
- Generating a new request invalidates the previous pending request. Successful use consumes the active request.
- Recovery removes only PIN credential preference entries. Learner, mark, assessment and school data are not cleared.

## Why observed pairs do not reveal future responses
Ed25519 is a standard public-key signature scheme. Knowing the public key, an arbitrary request, and signatures for other requests does not provide a practical way to create a valid signature for a new request. The signature is verified over a domain-separated exact request string. The administrator private key is required to produce valid responses.

## Verification performed
- New source-contract and cross-request signature checks: 13/13 passed.
- Existing migration-safety checks: 13/13 passed.
- V192 reporting checks: 13/13 passed.
- V191 community checks: 10/10 passed.
- V190 Quick Grading checks: 10/10 passed.

## Not yet verified
No Gradle compile, Android unit-test run, signed APK build, apksigner verification, or physical TECNO Spark 30C test was performed in this environment. GitHub Actions must confirm dependency resolution and compilation before installation. The new Bouncy Castle dependency is `org.bouncycastle:bcprov-jdk18on:1.78.1`.

## Administrator key handling — IMPORTANT
The separate administrator ZIP contains the private signing key. Keep it confidential and make an encrypted offline backup. Never commit it to GitHub, include it in the app/source ZIP, or send it to teachers. Anyone possessing it can sign recovery requests for this app's embedded public key. Loss or compromise requires a planned public-key rotation in a future app update.

## Security boundary
This prevents deriving signatures for a different request from observed request/response pairs. It does not prevent a malicious person from using a stolen, still-valid response for the exact same request before the teacher does, nor does it protect a rooted/compromised device or encrypt the local database. The current implementation stores installation binding and recovery state in app preferences; it is an offline recovery mechanism, not hardware-backed identity attestation.
