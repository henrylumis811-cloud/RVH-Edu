# RVH Edu V61 — Backup semantic preflight

## Changes
- Added a read-only semantic validation pass for imported backup rows before the restore transaction begins.
- Validates learner IDs, names, supported statuses and optional age bounds.
- Validates enrolment learner references, academic years and P1–P7 class identifiers.
- Validates assessment references, terms, sections and DRAFT/FINAL states.
- Validates mark references, subject codes and score ranges.
- Validates learner status-history references, supported statuses, reasons and timestamps.
- Keeps the existing transaction-based restore and database integrity audit as additional safeguards.

## Safety behavior
Malformed records are rejected before the restore path deletes any existing rows. SQLite constraints and the final database integrity audit remain in force. This does not prove every possible corrupted backup is rejected; a full Android build and device-level backup/restore tests remain outstanding.
