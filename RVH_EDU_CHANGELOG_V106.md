# RVH Edu V106 — Explicit School Context

## Change
Removed implicit `legacy-school` defaults from the public enrollment repository operations:
- `upsertEnrollment`
- `insertEnrollmentsAtomically`
- `getEnrollmentsForLearner`
- `getEnrollments`

Callers must now supply a school ID explicitly. This turns a quiet fallback into a compile-time error when a future caller forgets school context. Existing migration logic and stored records are not rewritten by this change.

## Validation
Run `python3 tools/check_multi_school_v106.py` from the project root. This is a source-contract check, not an Android compile or runtime isolation test.

## Limitations
Android compilation and device testing have not been performed in this environment. This change does not yet make admission numbers school-local and does not replace a full audit of assessment/mark reads, backup/restore fixtures, or authorization boundaries. The app remains offline-first; active-school selection is local context, not a user-authentication boundary.
