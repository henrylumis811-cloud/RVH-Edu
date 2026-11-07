# RVH Edu V67 — executable checksum primitive tests

- Extracted the production SHA-256 and numeric canonicalization primitives into a pure-JVM helper.
- The backup manager delegates to that helper; this keeps production and regression tests exercising the same implementation.
- Added an executable Kotlin regression test for equivalent numeric forms, negative zero, small decimals, a known SHA-256 test vector, deterministic output, and changed-content detection.
- This is a focused runtime test of checksum primitives, not a full JSON/SQLite restore integration test.

## Validation

- Pure Kotlin checksum regression: 10 assertions passed.
- Full Python source regression suite and ZIP integrity checked during packaging.
- Android Gradle build and physical-device restore tests remain unverified.
