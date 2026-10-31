# RVH Edu V56

## Learner duplicate-review foundation

- Added a deterministic possible-duplicate matcher using normalized admission number, date of birth, and normalized full name.
- Exposes reasons for each candidate and separates high-confidence identity collisions from name-only review suggestions.
- Explicitly does not merge, archive, or modify learner records. Shared names are not treated as proof of identity.
- Added a repository entry point for duplicate review.
- Updated name/age lookup to normalize whitespace and Unicode consistently and to avoid treating an omitted age as proof that stored age is missing.

## Validation boundary

The new source regression check verifies the duplicate-review safeguards. This is not a substitute for an Android Gradle build or physical-device testing.
