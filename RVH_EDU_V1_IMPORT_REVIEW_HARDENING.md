# RVH Edu V1 — Import Review Hardening

This milestone strengthens spreadsheet import before any data is persisted.

## Safety changes

- The teacher can edit the learner name during review.
- The teacher can correct or clear each subject mark before import.
- Marks are restricted to 0–100 in the review UI.
- Import re-resolves the edited learner name against the current class/year roster at confirmation time; stale preview matches are never trusted.
- Multiple exact same-name matches remain blocked. RVH Edu never guesses which learner is intended.
- Near-name matches are warned about, but are never automatically merged.
- Blank marks remain missing and are not converted to zero.
- Nothing is persisted until the teacher confirms.
- Imported records remain DRAFT.

## Verification

- `IMPORT_PARSER_REGRESSION_OK`
- `IMPORT_REVIEW_REGRESSION_OK`

Full Android compilation is still pending because the workspace does not contain a Gradle launcher and no system Gradle installation is available in the current environment.
