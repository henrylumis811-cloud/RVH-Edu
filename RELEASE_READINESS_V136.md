# RVH Edu V136 — Release readiness status

## Completed in source

- Android compile/target SDK raised to 36.
- Android Gradle Plugin and CI Gradle version updated for the API 36 build.
- Explicit Android automatic backup exclusions added for private databases, preferences and files. User-controlled in-app backup/restore remains available.
- Automatic audience/update polling removed from the main app path; the unconfigured update manifest UI removed. The app explains that updates are distributed through Google Play.
- Privacy policy draft is included as `PRIVACY_POLICY.md`; a readable policy is bundled in the app and linked from Feedback & Updates.
- CI configured to install SDK 36, run unit tests, build a debug APK and build a release AAB candidate.
- Release signing reads credentials from GitHub Actions secrets/environment variables only.
- Version: 1.1.6 (version code 102).

## Must be completed before Play production submission

1. Run the GitHub Actions workflow and fix any compiler, unit-test, packaging or target-API failures. This environment cannot download Gradle/Android SDK and has not completed a Gradle build.
2. Test on a physical Android 14/Tecno Spark 30C device: PIN, first launch, multi-school isolation, learner lifecycle, mark entry, grading, reports, exports, backup/restore, app upgrade, email feedback and WhatsApp feedback.
3. Configure release-upload signing secrets in GitHub Actions. A release AAB produced without those secrets is unsigned and **must not** be uploaded to Play Console. Use Play App Signing and protect the upload key.
4. Publish `PRIVACY_POLICY.md` content at a stable public HTTPS webpage and enter that URL in Play Console. The in-app policy text is not a substitute for a public policy URL.
5. Complete Data safety, target audience, content rating, store listing and support details accurately in Play Console.
6. Confirm account-specific testing/production-access requirements in Play Console.
7. Resolve AMGP before calling the app feature-complete if it remains part of the first-release scope. The agreed formula is `(D×1 + C×2 + P×3 + F×4) / (D+C+P+F)`, but the current source does not yet implement it. The category thresholds and how the school's grading scale maps to Distinction/Credit/Pass/Failure must be confirmed before showing AMGP in real reports; inventing thresholds could misreport academic outcomes.
8. Review Android 16 edge-to-edge/inset behaviour at target SDK 36 on physical devices and supported screen sizes.

## Current truth

V136 is a hardened release candidate, not a verified Play-ready release. XML parsing and source-level release checks pass. No full Gradle build or physical-device test has been performed in this environment.
