# RVH Edu V160 — Academic analytics regression tests

Version: 1.4.0 (build 126)

## Changes
- Added unit regression coverage for subject grade-band counts and AMGP weighting (D=1, C=2, P=3, F=4).
- Added coverage for a subject with no marks, including null AMGP and zero coverage.
- Added validation coverage ensuring duplicate subject marks for one learner are rejected.
- No production calculation rules or stored data formats were changed.

## Verification
- Source-level review and ZIP integrity checks performed.
- Gradle compilation, JUnit execution, and device tests were not run in this environment.
