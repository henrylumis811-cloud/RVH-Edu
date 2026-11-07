# RVH Edu V40 — Prevent mixed-policy class exports

- Class export entry points now pass each learner result's own assessment snapshot rather than selecting the first non-null snapshot in the roster.
- PDF, Excel, and CSV class-result export validation compares policy content while ignoring capture timestamps, so learners with the same policy captured at different times remain compatible.
- A combined export is rejected with a clear error when pinned policies disagree, or when pinned and legacy/unpinned assessments are mixed. This avoids silently using one learner's subject columns for everyone.
- Invalid saved policy JSON now stops the export instead of silently falling back to current settings.
- Added `tools/check_class_export_policy_consistency.py`; updated the historical-export regression check.

## Verification boundary

All available `tools/check_*.py` checks and Python tool compilation passed, and the ZIP integrity check passed. No full Android Gradle build or physical-device test was available, so this release is not claimed to be build-verified.
