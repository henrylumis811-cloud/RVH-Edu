# RVH Edu — V144 (1.2.4, build 110)

## Navigation candidate correction
- Added the explicit Compose `rememberSaveable` import used by the route-state saver.
- Retains route-aware system Back navigation and restoration of the current screen, bounded route history, and selected class/term/section/year after Activity recreation.
- No database schema or learner/marks data changes.

## Verification status
- Source-level checks and ZIP integrity checks only.
- Android compilation, unit tests, and device navigation checks have **not** run in this environment.
- Treat this as a corrected candidate, not a verified release build. CI must pass before installation or Play release.
