# RVH Edu V197 — CI version assertion correction

## Cause
The release configuration had already advanced the teacher app to versionName `1.7.4` / versionCode `160`, with CI artifact `RVHEdu-1.7.4-release.apk` and artifact label `rvh-edu-and-recovery-admin-v1.7.4-build160`.

Several older regression-contract scripts still asserted the previous `1.7.3` / `159` release identity. The V192 report/subject-analysis check therefore failed even though the application ID remained unchanged and the current release metadata was internally consistent.

## Correction
Updated the stale release metadata assertions in the V180, V188, V189, V190, V191 and V192 check scripts to verify the current `1.7.4` / `160` release and current CI artifact naming. No application ID, signing configuration, database schema, app feature, or runtime source was changed for this CI-only correction.

## Verification
- V193 PIN recovery security: 13/13
- V195 support contacts: 12/12
- Recovery admin app: 17/17
- V192 reports and subject analysis: 13/13
- V191 community links: 10/10
- V190 quick grading theme: 10/10
- V189 bulk marks professional UI: 10/10
- V188 bulk marks layout: 9/9
- Mark-entry UI polish: 13/13
- Bulk marks recovery: passed
- Bulk marks unsaved guard: passed
- V187 historical analytics/error routing: 8/8
- V180 SQLite busy handling: 7/7
- Backup regression suite: 19/19
- Legacy migration safety: 13/13
- V197 persistent PIN recovery: 10/10

Android Gradle compilation and signed APK generation still require the GitHub Actions environment and configured signing secrets; they were not run in this local environment.
