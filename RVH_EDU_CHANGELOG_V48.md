# RVH Edu V48

## Policy-conflict diagnostic integrity
- Refactored learner-level policy conflict diagnostics to keep each parsed policy attached to its original row rather than looking rows up again by learner display name.
- This avoids associating a policy with the wrong learner when names repeat.
- Mixed pinned/legacy and invalid-policy diagnostics now select rows directly from their original records.
- No marks, policy snapshots, or historical assessment records are changed by this diagnostic-only improvement.

## Validation
- Automated source regression checks passed.
- Python tool compilation and ZIP integrity passed.
- A full Android Gradle build and physical-device test were not performed.
