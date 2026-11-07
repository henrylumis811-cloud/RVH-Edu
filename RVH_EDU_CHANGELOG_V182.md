# RVH Edu V182 — Startup migration sequencing fix

## Root cause addressed

The V181 source launched `LegacyGradebookMigration.migrateIfNeeded(context)` immediately on app startup, before the PIN gate was passed. Once unlocked, the main academic service could open/use the same SQLite database while the migration was still running. This is a credible race condition given the earlier SQLite busy crash and the reported release-only startup crash.

## Changes
- Run legacy gradebook migration only after the user unlocks the app.
- Execute migration on `Dispatchers.IO`, not the UI thread.
- Hold main-screen/database initialization until migration completes.
- Catch migration exceptions and show a retry screen rather than allowing an uncaught startup exception to terminate the app.
- Keep the existing legacy gradebook source data untouched by the migration path.
- Incremented Android version to 1.6.1 / versionCode 147.

## Validation status
- Source-level inspection performed.
- Build and unit tests have NOT been run in this environment: the supplied archive does not include the `gradlew` launcher or wrapper JAR, and no system Gradle executable is installed. Run the GitHub Actions workflow before installing this build.
