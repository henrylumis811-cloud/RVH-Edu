# Changelog — V109

- Added school-verified repository read APIs for assessment lookup, assessment history, and mark retrieval.
- Added explicit enrollment/assessment-to-school ownership checks before returning data from the new APIs.
- Added `tools/check_multi_school_v109.py` source-contract regression checks.
- Existing unscoped APIs remain for compatibility; callers still need migration. This is not a claim of complete multi-school isolation.
- No schema migration or existing learner-data rewrite performed.
- Android compilation and device/database integration tests have not been run.
