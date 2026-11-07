# RVH Edu V163 — School-scoped backup status validation

Version: 1.4.3  
Build: 129

## Changes
- Extracted the school/learner lifecycle-status pair rule into a pure JVM helper used by backup preflight validation.
- Replaced delimiter-concatenated status keys with typed `(schoolId, learnerId)` pairs, avoiding ambiguous keys when IDs contain the separator character.
- Added regression tests proving that a status for one school cannot satisfy another school's enrollment, that the same learner may have independent status records in multiple schools, and that missing learner status is detected.

## Verification
- Source-level checks and ZIP integrity check only.
- Gradle compilation, JUnit execution, Android build, and device testing have not been run in this environment.
