# RVH Edu V66 — canonical numeric checksum values

- Hardened version-2 backup checksums by normalizing equivalent JSON number spellings before hashing (for example `1`, `1.0`, and `1e0`).
- Boolean values remain distinct from numeric values; arrays preserve order and object keys are sorted as before.
- This reduces checksum mismatches caused solely by harmless numeric formatting changes while still detecting changed data.
- Extended the source regression check for the numeric canonicalization contract.

Validation is source-level only. A full Android Gradle build and real backup/restore tests have not been run.
