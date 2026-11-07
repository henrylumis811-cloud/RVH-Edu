# RVH Edu V162 — Backup checksum regression coverage

- Version: 1.4.2
- Android version code: 128

## Changes

- Added JVM regression tests for canonical backup JSON key ordering and array-order preservation.
- Covered JSON string escaping, numeric normalization, negative zero, and extreme exponents that must not expand into enormous strings.
- Covered rejection of unsupported checksum-tree values and non-string object keys.
- Added a known SHA-256 test vector to protect the checksum primitive used by backup verification.

## Verification status

Source-level assertions and ZIP integrity checks are run during packaging. Gradle compilation, JUnit execution, Android build, and device testing must still be run in CI or an Android build environment; this handoff does not claim those tests passed.
