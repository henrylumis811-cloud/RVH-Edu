# RVH Edu V157 — Lazy-list item reuse hints

Version: 1.3.7  
Build number: 123

## Changes
- Added explicit Compose lazy-list content types to learner history, archived learners, class analytics, class progression, learner profile history, assessment policy review, and import review lists.
- Existing stable item keys are retained. Content types distinguish reusable row layouts and help lazy layouts avoid treating unlike rows as the same item type.
- No database schema, grading calculation, or mark persistence behaviour was changed.

## Verification
- Source replacement checks: passed.
- ZIP integrity: checked after packaging.
- Gradle compilation, unit tests, and device profiling: not run in this environment.
