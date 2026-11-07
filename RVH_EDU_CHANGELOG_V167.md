# RVH Edu V167 — Structured Backup Duplicate Keys

Version: 1.4.7 (build 133)

## Changes
- Replaced delimiter-joined backup validation keys with structural pairs/lists for school-local admission assignments, enrolments, assessments, marks, and legacy identity-review records.
- Prevents identifier or field values containing the separator character from causing false duplicate matches or bypassing duplicate detection.
- Keeps the existing preflight validation and transactional restore flow intact.

## Verification
- Source-level checks and ZIP integrity check performed.
- Gradle compilation, automated test execution, and Android device testing were not run in this environment.
