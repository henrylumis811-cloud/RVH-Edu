# RVH Edu — Multi-school phase 2 (V99)

## Scope

V99 replaces the database-wide enrollment uniqueness rule with a school-scoped rule. A learner may now have an enrollment for the same academic year and class at different schools, while duplicate enrollments for the same learner, school, year and class remain blocked.

## Migration safety

- Database version advances to 8.
- Upgrade copies `enrollment`, `assessment`, and `mark` rows to temporary tables, drops child tables before parents, recreates the tables, then restores the original row IDs and persisted columns.
- The migration runs within SQLiteOpenHelper's upgrade transaction; failure should roll back the migration rather than leave a partially rebuilt academic database.
- Assessment policy snapshots, marks, timestamps, statuses, and finalized timestamps are copied verbatim.
- Existing enrollment rows retain their existing `school_id` (normally `legacy-school`).
- A foreign key now ties enrollment.school_id to school.id.
- New installs use a unique index on `(school_id, learner_id, academic_year, school_class)`.

## Important boundary

This removes the database schema blocker, but it does not complete the end-user multi-school feature. Active-school selection, school management screens, school-specific settings, fully scoped reports/exports, and backup/restore coverage for teacher and membership tables remain follow-up work. Repository defaults still use `legacy-school` until active-school selection is wired through the app.

## Validation

Source-level checks and Python syntax checks are included. This environment has not run an Android Gradle build, an instrumented SQLite migration against a real V7 database, or device tests. Those remain required before release.
