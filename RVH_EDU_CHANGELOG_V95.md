# RVH Edu V95 — App Display Name

- Changed the Android launcher/app-switcher label from **RVH Grader** to **RVH Edu**.
- Kept the existing manifest resource reference so the label is centralized in `app/src/main/res/values/strings.xml`.
- No application ID, database, or academic logic was changed.

## Verification
- Source assertion confirms the app name is `RVH Edu`.
- Android manifest continues to reference `@string/app_name`.
- Full Android build and on-device launcher verification remain to be run.
