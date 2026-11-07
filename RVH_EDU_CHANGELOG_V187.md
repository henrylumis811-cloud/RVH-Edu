# RVH Edu V187 — Historical analytics and workflow error hardening

- Version: 1.6.6, versionCode 152.
- Preserves application ID `com.henrylumis.rvhedu` and the V185 persistent signing configuration.
- Class analytics now uses the immutable grading scale saved with the selected complete assessment policy, so later Settings changes do not recalculate historical AMGP/grade bands using today's scale.
- Subject-only legacy policy snapshots continue to use the current configured scale, matching the existing legacy calculation fallback.
- Class export policy compatibility now includes only learners with an actual persisted assessment row. Learners without an assessment row remain incomplete in class results/exports; actual legacy rows with null policy snapshots remain represented and still conflict with pinned rows.
- Class-results and analytics policy conflicts now throw a dedicated `AssessmentPolicyCompatibilityException` and are the only errors routed to Assessment Policy Review.
- Database, calculation, and unexpected exceptions no longer masquerade as policy conflicts; stale results are cleared and the teacher is returned to Home with a diagnostic banner.
- Global operation banners automatically expire after eight seconds so old ranking/export messages do not persist indefinitely on unrelated screens.
- Added unit regression tests for immutable historical analytics scale selection, legacy fallback, and missing-assessment versus persisted-legacy export policy semantics.
- Added a V187 static regression contract checker and refreshed stale static checks for current database schema, UI coverage expression, and bulk-mark recovery behaviour.

## Verification status

- V187 targeted static regression contracts: 9/9 passed.
- Historical policy consumption check: passed.
- Class export policy consistency check: passed.
- Class policy compatibility check: passed.
- Migration safety checks: 13/13 passed.
- Backup/restore regression suite: 19/19 passed.
- Results/analytics UI checks: 14/14 passed.
- Bulk-mark recovery static checks: 10/10 passed.
- Full Gradle unit test/build: not run in this environment; no Gradle executable or Gradle wrapper JAR is present in the archive. GitHub Actions must verify the Android compilation and unit tests.
- Physical-device verification: pending. Keep V185 installed until the V187 GitHub Actions build succeeds and V187 is ready for direct in-place update testing.
