# RVH Edu V1 — Adversarial Test Checklist

Before V1 qualification, verify on a real Android device:

- Kill/reopen app after saving draft; draft survives.
- Kill/reopen after finalization; final result survives and remains locked.
- Attempt finalization with one blank mark; no learner/enrollment is created by the failed dashboard submission.
- Enter -1, 101, decimal, alphabetic, and blank marks; invalid values are rejected and blank stays missing.
- Create two learners with the same name; class result/history never silently merges them.
- Edit an existing learner's marks; prior academic assessments remain intact.
- Restore a backup over populated data; restored counts, marks, assessment statuses, and history match the backup.
- Interrupt restore; original database remains usable.
- Import spreadsheet with duplicate subject headers; review warns instead of silently duplicating a subject.
- Import spreadsheet with empty cells; imported marks remain missing.
- Enter marks with blank, zero, maximum and out-of-range values; blanks must remain missing and invalid values must be rejected.
- Verify rapid manual entry, editing, draft/final transitions and duplicate-name safeguards.
- Generate report card for complete result; PDF opens and matches stored marks.
- Attempt certificate for incomplete result; generation is refused.
- Share generated PDF/XLSX/CSV from Android; recipient app receives readable file.
- Verify app works with no network for manual grading, history, analytics, backup, and local reports.
