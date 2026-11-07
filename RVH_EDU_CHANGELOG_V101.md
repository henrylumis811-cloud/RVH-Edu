# RVH Edu V101 — School management screen

- Added a Schools screen accessible from School settings.
- Displays locally stored schools and marks the active school.
- Allows selecting an existing school and registering a new school with an optional motto.
- Switching/creating a school refreshes visible school identity and invalidates cached class analytics/results.
- Existing records are not moved or merged by school creation or selection.

## Verification

Source-level checks and ZIP integrity are performed for this handoff. Android compilation and device testing must be run separately; this change is not represented as device-verified.

## Known limitation

This screen does not itself complete end-to-end school isolation. Academic queries, backup/restore, and school-scoped grading/subject settings still require auditing before multi-school support should be considered complete.
