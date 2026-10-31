# RVH Edu V7 — Bulk Marks Save Safety

- Bulk mark entry now writes only rows whose values actually changed.
- Untouched blank cells are no longer sent to the save service as null values, avoiding unnecessary delete/rewrite operations for missing marks.
- A changed cell cleared by the teacher is still saved as a deliberate removal of that subject mark.
- Successful rows are marked saved individually; failed rows remain dirty so the teacher can retry them.
- Partial saves now report both the number saved and the first failure instead of presenting a misleading all-or-nothing result.

Validation performed: source-level assertions and ZIP integrity check. Android compilation and device verification were not performed in this environment.
