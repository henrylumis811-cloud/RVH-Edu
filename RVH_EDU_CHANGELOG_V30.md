# RVH Edu V30 — Organized Home and Settings

## User-facing changes

- The app opens to a welcoming RVH Edu home screen rather than dropping staff directly into the old grading form.
- The home screen offers clear actions for enrolling learners, entering individual or class marks, results, analytics, academic history, spreadsheet import, settings, and backup/restore.
- Settings is now a hub. School details and grading tables remain in the existing grading configuration screen; backup/restore, archived learners, and class progression are reachable from the same hub.
- The previous grading form remains available as **Quick Grading** so the workflow is not silently removed.
- Navigation labels were updated to reflect their role rather than exposing an internal/developer-oriented “mainframe” label as the primary welcome experience.

## Architecture direction

The codebase remains organized by responsibility: `core/` for academic rules and domain operations, `data/` and `core/repository/` for persistence, `ui/` for Compose screens, and `export/` for output generation. New screens are separate composables instead of further inflating `MainActivity.kt`.

## Known limitations

This change organizes navigation; it does not claim that all school-specific rules are configurable yet. Subjects, class structures, pass criteria, ranking policy, and assessment schedules still require a complete configurable academic-rules implementation. Android Gradle/device verification must be reported separately from static source checks.

## Structural cleanup

- Renamed the previous `SettingsScreen.kt` to `SchoolGradingSettingsScreen.kt` so its filename and composable accurately describe its responsibility; the new `SettingsHubScreen.kt` is the actual settings landing page.
- Added `ARCHITECTURE.md` documenting layer ownership and known orchestration debt instead of claiming the project is fully modularized already.
