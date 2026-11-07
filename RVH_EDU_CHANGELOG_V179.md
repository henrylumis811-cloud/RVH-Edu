# RVH Edu V179 — Direct-install release build

- Version 1.5.8, versionCode 144.
- Changed GitHub Actions to build and upload only a signed release APK for direct installation.
- Removed the mandatory Google Play upload keystore requirement and removed AAB production from this workflow.
- Release signing prefers configured persistent release signing secrets; otherwise it uses the runner debug key as a convenient first-install fallback.
- Workflow runs backup/restore regression checks, Android unit tests, and APK signature verification.
