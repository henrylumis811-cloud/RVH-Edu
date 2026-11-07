# RVH Edu V189 — Professional bulk-marking experience

- Version: 1.6.8, versionCode 154.
- Refines the V188 assessment selector into a branded RVH Edu Material 3 modal bottom sheet, using the app's existing surface, primary, secondary, outline, shape, and typography tokens rather than introducing a separate visual style.
- Adds a clear session heading, concise guidance, and a live summary preview for class, year, term, assessment period, and subject.
- Separates Term, Assessment Period, and Subject into clearly labelled groups with helper text and consistent spacing.
- Uses compact wrapping chips for Term and Subject, plus full-width radio selection rows for assessment periods to improve scanning and touch accuracy.
- Makes the sheet internally scrollable while keeping Cancel and Apply selection actions visible at the bottom.
- Applies staged selection only after explicit confirmation; the existing unsaved-marks confirmation remains in force.
- No database schema, assessment identity, grading, mark calculation, or save semantics are changed.

## Verification status

- V189 professional bulk-mark UI source contracts: 10/10 passed.
- V188 bulk-mark layout contracts: 9/9 passed.
- V187 historical analytics/error-routing contracts: 8/8 passed.
- Android Gradle compilation and unit tests: not run in this environment; GitHub Actions required.
- Physical TECNO Spark 30C verification: pending.
