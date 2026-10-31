# RVH Edu V76 — Class Results & Analytics UI Refinement

## Interface improvements
- Reworked Class Results into a clearer academic-performance view with class/year context, scrollable term and section chips, summary metrics, incomplete-assessment warning, ranked learner cards, and a more visible export action.
- Ranked learner cards now distinguish complete and incomplete results, show total marks and average, surface aggregate/division where available, and keep report-card/certificate actions connected.
- Reworked Class Analytics with a compact class snapshot, completion indicator, subject coverage bars, subject performance summaries, and clearer learner-risk cards.
- Added intentional empty states for classes without results, subject analytics without data, and views without risk flags.
- Kept styling theme-aware through MaterialTheme colors and typography.

## Behaviour preserved
- Existing callbacks for period changes, export, report cards, certificates, and back navigation remain connected.
- No grading, ranking, pass-rate, analytics, or persistence calculations were changed.
- Incomplete learners remain visibly incomplete and are not presented as ranked results.

## Validation
- Added `tools/check_ui_results_analytics_polish.py` with source-level regression checks for the UI structure and retained actions.
- Full Android Gradle build and physical-device visual testing have not been verified in this release.
