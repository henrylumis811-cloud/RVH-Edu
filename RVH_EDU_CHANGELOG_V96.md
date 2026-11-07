# RVH Edu V96 — Architecture and update-install foundation

- Incremented Android `versionCode` to 96 and `versionName` to 1.1.0.
- Added optional stable CI debug signing via GitHub Actions secrets, so APKs from separate hosted-runner jobs can be signed consistently.
- Updated the CI workflow to configure the signing key only when all required secrets are present, and to label artifacts with the workflow run number.
- Added `ANDROID_UPDATE_INSTALL_GUIDE.md` explaining the one-time signing transition and safe update requirements.
- Added `ARCHITECTURE_DECISIONS_MULTI_SCHOOL.md` to document teacher identity, concurrent school memberships, safe school scoping, transfer semantics, and mandatory migration safeguards.
- Kept multi-school database migration itself out of this checkpoint: the current schema has a school table, but learners/enrolments/assessments are not consistently school-scoped. A UI-only school switcher would risk data leakage, so schema inventory and migration tests must come first.

## Validation limitations

Source/configuration checks and ZIP integrity can be performed locally. A complete Gradle Android build and an install-over-existing-app test must still be run in GitHub Actions and on a device. Stable in-place updates require the one-time GitHub signing-secret setup documented in the guide.
