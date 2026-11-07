# RVH Edu V143 — Navigation Hardening

- Persisted the current destination, route history, selected class, term, assessment section, and academic year across Activity recreation using Compose `rememberSaveable` and a custom `Saver`.
- Bounded route history to the most recent 100 transitions to prevent unbounded growth during long sessions.
- Preserved actual route-based Back behaviour and the drawer-first dismissal behaviour from V142.
- Added a read-only route-history snapshot helper for focused verification.
- Version: 1.2.3 (versionCode 109).

## Verification status

Source and ZIP structure checks only. Gradle compilation, automated tests, and on-device navigation checks have not yet been run for this revision. This is not a verified release candidate.
