# RVH Edu V72 — Professional UI design pass

## Design system
- Replaced the high-neon palette with a restrained academic blue/teal system for light and dark themes.
- Defined shared Material 3 shape tokens for consistent component geometry.
- Improved semantic container, surface, outline, and error colors for visual hierarchy and legibility.

## Home dashboard
- Rebuilt the first screen with clear RVH Edu identity and an offline-workspace label.
- Promoted active learner count and academic year into a dedicated summary panel.
- Organized the nine existing workflows into a compact two-column action grid with consistent icon containers, labels, descriptions, and navigation affordances.
- Preserved all existing callbacks and the “In dedication to Hellen” footer.
- Kept the dashboard offline-first and did not add unsupported feature claims.

## Verification
- Run `python3 tools/check_*.py` from the project root for the available source regression checks.
- Android Gradle compilation and visual/device testing have not been verified in this release environment.
