# RVH Edu V89 — Android CI compile-error repair

This release addresses the Kotlin compile errors reported by the Android `assembleDebug` workflow:

- Added the Activity Result API imports required by the CSV/Excel document picker.
- Defined the class roster locally before building the PDF export's policy snapshot list.
- Corrected academic-year handling where the data model stores years as integers.
- Added the missing `SystemMode` model import.
- Corrected the `KeyboardOptions` package import.
- Removed an invalid top-level import for the exposed dropdown menu API.
- Adjusted the home-screen LazyList helper so composable rows are emitted from the LazyList DSL correctly.
- Updated the determinate progress-indicator calls for the resolved Material 3 API shape and opted affected screens into the experimental Material 3 API.

## Validation status

The source was checked for the specific reported unresolved references and type mismatches. Python-based repository checks and ZIP integrity checks are run as part of packaging. A complete Android build is **not claimed** in this environment because a Gradle executable/wrapper distribution is unavailable here. The next GitHub Actions run must confirm whether additional compiler errors remain.
