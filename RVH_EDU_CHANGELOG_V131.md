# RVH Edu V131 — Feedback, audience opt-in, and update-check groundwork

## Added
- Added a Feedback & Updates destination under School Settings. Teachers can write feedback, select Suggestion/Problem/Praise, and explicitly share it through an Android share sheet. The app does not silently send feedback.
- Added an optional anonymous audience check-in, disabled by default. If a teacher opts in and the HTTPS endpoint is configured, a random installation identifier, app version code, and Android platform are sent no more than once every seven days while a validated internet connection is available. No learner names, marks, school names, or school records are sent. Ordinary server connection metadata (for example, IP address) may still be visible to the endpoint operator.
- Added a lightweight update checker. It checks on demand from Feedback & Updates and, once configured, quietly checks about daily while the app process is active. A newer version prompts the teacher with a dismissible update dialog; offline failures do not block school work.
- Added INTERNET and ACCESS_NETWORK_STATE permissions.
- Incremented Android versionCode to 97 and versionName to 1.1.1.

## Release configuration required
The online endpoints intentionally remain blank in `online/OnlineServices.kt` because this project snapshot does not identify a controlled audience backend or canonical RVH Edu release-manifest URL. Before release, configure both HTTPS URLs and host the documented JSON/API contract. Until then, audience check-ins are not transmitted and update checks explicitly report that they are not configured. Do not publish claims that live audience measurement or automatic update notifications are active until endpoints are deployed and tested.

## Validation status
Source-level checks validate the UI wiring, consent gate, payload boundaries, HTTPS-only endpoints, update version comparison, and offline-safe failure handling. A full Android Gradle build and physical-device tests are still required; this environment does not include the project Gradle wrapper script or a usable Gradle installation.
