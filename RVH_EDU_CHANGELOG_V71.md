# RVH Edu V71 — bounded checksum number canonicalization

- Changed canonical numeric output to retain normalized exponent notation where useful instead of always expanding numbers with `BigDecimal.toPlainString()`.
- This avoids creating an extremely large intermediate string when a small backup contains a number with an extreme exponent.
- Invalid/non-finite numeric values now fail with a clear checksum-specific validation error.
- Expanded executable Kotlin regression coverage for huge exponents, equivalent exponent/decimal forms, non-finite values, and checksum stability when object keys are reordered.

## Validation limits

The pure Kotlin checksum helper regression test is executable independently. This release has not been verified with a full Android Gradle build or a physical-device restore test. JSON parser and SQLite restore integration remain separate end-to-end validation work.
