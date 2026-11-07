# RVH Edu V142 — Route-aware Back Navigation

## Changes
- Replaced fixed screen-to-screen Back rules with a central navigation history stack.
- Android system Back now returns to the screen actually visited immediately before the current screen.
- Toolbar Back actions on secondary screens use the same history instead of hard-coded destinations.
- Drawer dismissal still takes priority over navigating backwards.
- Reselecting the current destination does not create duplicate history entries.
- The app exits through normal Android Back behaviour only when there is no in-app route history.

## Version
- Version name: 1.2.2
- Version code: 108

## Verification status
- ZIP integrity and source-level navigation checks performed.
- Gradle compilation and device testing have not yet been run; verify through GitHub Actions and on-device tests before release.
