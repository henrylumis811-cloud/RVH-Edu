# RVH Edu V63

## Historical-policy-aware database integrity

- Finalized assessments with saved policy snapshots are checked against the subject codes captured with that assessment, rather than the school's current mutable subject catalog.
- Integrity audit now detects saved policies that are malformed or whose school class does not match the enrolment class.
- Legacy finalized assessments without policy snapshots are not declared incomplete merely because today's subject catalog differs from the historical catalog. Their marks and historical policy state remain untouched.
- Added a source regression check for historical-policy-aware integrity validation.

## Validation limits

Source regression checks, Python compilation and ZIP integrity are checked for this handoff. A full Android Gradle build and on-device backup/restore test are not claimed unless separately executed.
