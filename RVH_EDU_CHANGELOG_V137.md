# RVH Edu v1.1.7 — AMGP implementation

## What changed
- Added Uganda primary-school AMGP calculation to subject analytics.
- AMGP uses the agreed formula: `(D × 1 + C × 2 + P × 3 + F × 4) / (D + C + P + F)`.
- Grade categories are derived from the **currently configured mark-to-aggregate grading scale**: aggregates 1–2 are Distinctions, 3–6 Credits, 7–8 Passes, and 9 (or a custom aggregate outside the standard passing bands) is Failure.
- AMGP is recalculated from the raw recorded marks and current settings when class analytics is calculated; it does not rely on stale stored aggregate values.
- Added the D/C/P/F counts alongside each subject's AMGP.
- Missing marks remain missing and are not counted as failures. AMGP is shown as unavailable when a subject has no recorded marks.
- Existing aggregate, division, and general pass-rate calculations remain separate and unchanged.

## Verification status
- Source-level call-chain and constructor references inspected after the changes.
- Arithmetic/category behavior reviewed against the Uganda primary D1/D2, C3–C6, P7/P8, F9 grade grouping.
- A full Android Gradle build was **not run in this environment** because the project archive has no `gradlew` wrapper and no system `gradle` executable was available. Run the project's GitHub Actions build before release.
