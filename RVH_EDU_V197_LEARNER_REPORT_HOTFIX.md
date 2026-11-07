# RVH Edu V197 — Individual Learner Report Hotfix

## Scope
This is an isolated maintenance change to the supplied V197 project. It does not upgrade the application or change its package identity, version code, version name, database schema, PIN/recovery flow, or unrelated screens.

## Changes
- The Class Results `Report` action now opens an on-screen individual learner report instead of immediately launching PDF sharing.
- The report displays the saved learner identity/profile fields available in this version: full name, admission number, age, gender, date of birth, and status.
- It displays school/class/year/term/assessment period, subject marks, each subject's percentage and saved aggregate, overall total, maximum total, average, aggregate sum, division, completion/missing-subject status, and class position where the learner is ranked.
- Incomplete saved assessments can also be opened as reports; missing subjects remain explicitly marked and are not fabricated.
- PDF export remains available from the report screen and uses the existing report-card export implementation.
- Result calculation uses the currently selected grading scale, while saved assessment policy snapshots remain authoritative in report construction.

## Validation
- ZIP integrity verified.
- Source-level navigation and report-builder integration inspected.
- Android compilation/device testing was not run because this environment has no Gradle executable or project Gradle wrapper. Build and phone verification remain required.
