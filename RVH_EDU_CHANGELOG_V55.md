# RVH Edu V55 — Learner identity validation at repository boundary

- Hardened `LearnerRepository.upsert`, so callers cannot bypass core learner identity validation by skipping the screen/service layer.
- Normalizes learner names and whitespace, admission numbers, and status values before persistence.
- Rejects blank learner IDs/names, out-of-range ages, and unsupported statuses.
- Checks admission-number duplicates after trimming and case normalization, excluding the same learner ID so a learner can retain their own number while being edited.
- Leaves learner IDs and academic-history links unchanged; no historical assessment or mark data is rewritten.

Validation is source-level only. A full Android Gradle build and physical-device test remain outstanding.
