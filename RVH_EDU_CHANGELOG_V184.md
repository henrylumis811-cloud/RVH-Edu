# V184 — Migration safety candidate

See `RVH_EDU_CHANGELOG_V184.md` for the migration-safety changes in this candidate.

## V185 — Stable update signing

- Keeps application ID `com.henrylumis.rvhedu` unchanged.
- Advances versionName to 1.6.4 and versionCode to 150.
- Removes the unsafe CI release-signing fallback to a disposable runner debug certificate.
- Requires four persistent signing secrets in GitHub Actions and decodes the private keystore only into the runner temporary directory.
- Uses one stable signing configuration for debug and release variants when credentials are configured.
- Fails early with a clear error when CI signing secrets are missing, rather than publishing an APK likely to be impossible to update later.
- Documents Android's package/signature/version requirements and the one-time transition limitation.

This does not prove that the new certificate matches the APK currently installed on a device. That must be confirmed by comparing signing-certificate fingerprints or by a controlled in-place update test. If the old private key is unavailable and the old APK uses a different certificate, Android requires a one-time uninstall/reinstall; preserve and verify data backups before that transition.


## V186 — Assessment-group ranking correction
- Fixes class ranking validation to compare the shared assessment context rather than per-learner assessment-row IDs.
- Preserves rejection of mixed class/term/section assessment groups and legacy single-assessment callers.
- Aligns class-export validation with the shared assessment group.
- Clears stale class-results/analytics block notices after a successful load.
- Adds regression tests for legitimate per-learner assessment IDs and genuine mixed-group rejection.
- Advances versionName to 1.6.5 and versionCode to 151.


## V187 — Historical analytics, exports and error-routing hardening

- Class analytics now uses the saved complete grading-scale snapshot for the selected historical assessment, matching class results after school grading settings change.
- Class exports compare policy snapshots only for learners with an actual persisted assessment row. Unassessed learners remain incomplete; real legacy assessment rows mixed with pinned rows remain blocked.
- Class-results and analytics policy conflicts now use a dedicated exception and are the only failures routed to Assessment Policy Review. Database/calculation failures return safely to Home with a diagnostic instead of being mislabeled as policy conflicts.
- Transient operation banners automatically expire after eight seconds so stale ranking/export messages do not persist across unrelated screens.
- Advances versionName to 1.6.6 and versionCode to 152.
- Adds focused regression coverage for historical grading-scale selection and assessment-policy compatibility.
