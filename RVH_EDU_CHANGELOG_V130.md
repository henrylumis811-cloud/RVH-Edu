# RVH Edu V130 — Kotlin compile fix for class roster repository

## Fix

- Fixed the reported `:app:compileDebugKotlin` failure in `ClassRosterRepository.kt`: the constructor accepted `context` as a plain parameter but `get()` referenced it after property initialization. The constructor now declares `private val context: Context`, retaining the same Android context for school selection and school-scoped roster queries.
- No database schema, backup format, learner data, or roster query behavior was changed.
- Added `tools/check_class_roster_context_v130.py` to guard the constructor-property and school-scoping contract.

## Validation

Run `python3 tools/check_class_roster_context_v130.py` — 5/5 checks passed.

The supplied GitHub Actions log identifies the compile failure precisely, and this change addresses both unresolved `context` references on lines 19–20. A successful Android build still needs to be confirmed by rerunning `gradle assembleDebug --no-daemon` in GitHub Actions; this local source check does not claim compilation success.
