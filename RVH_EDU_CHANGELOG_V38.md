# RVH Edu V38 — Historical report aggregate consistency

## Changes
- Report-card subject aggregates now derive from the assessment's saved grading-scale snapshot when a complete scoring policy exists for an upper-primary assessment.
- Marks whose maximum is not 100 are normalized to a percentage before the saved aggregate bands are applied, matching `AcademicEngine` behavior.
- Legacy subject-only snapshots and lower-primary reports preserve their existing stored-aggregate behavior rather than fabricating historical grading rules.
- Added a source regression check for this report-card policy path.

## Verification
- Run all `tools/check_*.py` source checks.
- Run Python compile checks for the tool scripts.
- Validate ZIP integrity and root-level project layout.
- A full Android Gradle build and physical-device test remain unavailable in this environment; do not treat source checks as a substitute for those tests.
