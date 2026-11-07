# RVH Edu V183 — SQLite busy-timeout startup crash fix

## Confirmed issue
The V182 source still configured SQLite's `PRAGMA busy_timeout = 5000` using `SQLiteDatabase.execSQL()` inside `RvhEduDatabase.onConfigure()`. This PRAGMA returns a result row, and Android rejects row-returning statements passed to `execSQL()`. This matches the exception in the supplied device bug report.

## Changes
- Execute `PRAGMA busy_timeout = 5000` with `rawQuery()` and close the cursor safely.
- Keep foreign-key configuration and the existing database schema/migrations unchanged.
- Incremented Android version to 1.6.2 / versionCode 148.

## Data safety
This patch does not clear, recreate, or delete the app database. Do not uninstall or clear app storage while investigating existing records.

## Validation
Source-level patch applied. Build and tests were not run here because this archive does not include the Gradle wrapper launcher/JAR, and no system Gradle executable is available in this environment. Run the repository's GitHub Actions build/tests before installing.
