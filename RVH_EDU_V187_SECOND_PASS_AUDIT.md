# RVH Edu V187 — Second-pass findings and corrections

## Scope

Follow-up to the V186 source audit. Reviewed historical analytics policy selection, class export policy compatibility, global operation banners, and exception routing. Changes are implemented in V187 source.

## Findings corrected

### HIGH — Historical analytics used today's grading scale

**Root cause:** `AcademicDataService.classAnalytics()` selected the saved assessment policy for subjects/pass threshold, but passed the caller's current `GradingScale` to `ClassAnalyticsCalculator.summarize()`. `classResults()` calculates each learner using the saved complete policy snapshot, so changing Settings after an assessment could make analytics AMGP/grade bands disagree with class results.

**Correction:** Added `analyticsGradingScale(policy, currentScale)`. Complete saved policy snapshots supply their immutable grading scale. Legacy/subject-only snapshots continue to use the current configured scale, consistent with the legacy calculation fallback. Class analytics passes that effective scale to the calculator.

### MEDIUM — Unassessed learners could be mistaken for legacy-policy conflicts during export

**Root cause:** Export call sites supplied one nullable snapshot for every ranked learner. A learner without a persisted assessment row therefore contributed `null`, indistinguishable from an actual legacy assessment row whose saved policy is null.

**Correction:** Added `policySnapshotsForExistingAssessments()`. Export call sites first collect only existing persisted assessment rows, then preserve each row's nullable policy snapshot. An unassessed learner is omitted from policy compatibility checking but remains in class results/export as incomplete. A real legacy assessment row still contributes null and still conflicts with pinned rows, as intended.

### MEDIUM — Old global status messages could persist on unrelated screens

**Root cause:** `importStatus` is rendered globally and could remain visible after navigating away from the operation that generated it.

**Correction:** Transient operation feedback now expires after eight seconds. This prevents a previous ranking/export error from remaining indefinitely on Learners, Mark Sheet, or Policy Review. The banner remains dismissible.

### MEDIUM — Non-policy exceptions were misrouted to Assessment Policy Review

**Root cause:** broad `catch (Exception)` blocks in class-results and analytics loaders routed every exception to policy review, including SQLite, calculation, and programming errors.

**Correction:** Added `AssessmentPolicyCompatibilityException`; only policy compatibility conflicts are routed to Policy Review. Other failures clear stale result/analytics state, return to Home, and display a diagnostic message rather than falsely implying that saved policies conflict.

## Regression coverage

- Unit tests cover complete saved-scale precedence, legacy scale fallback, and the distinction between an absent assessment row and an actual legacy assessment row.
- Static contract checker verifies all four class export call sites use persisted-row filtering, exception routing, banner expiry, and version/artifact metadata.

## Verification

- V187 targeted static contracts: 9/9 passed.
- Historical policy consumption: passed.
- Class export policy consistency: passed.
- Class policy compatibility: passed.
- Migration safety: 13/13 passed.
- Backup/restore regression suite: 19/19 passed.
- Results/analytics UI static checks: 14/14 passed.
- Bulk-mark recovery static checks: 10/10 passed.
- Gradle compilation/unit tests were not run: this environment has no installed Gradle and the project ZIP has no Gradle wrapper JAR. The GitHub Actions workflow is the required build gate.
- Device test not performed. Keep the currently installed V185 until the V187 CI artifact has passed and can be installed over it.

## Remaining verification

The source changes address the four findings in the V186 second-pass report. They do not establish that every feature in the full app is defect-free. GitHub Actions must still compile/run unit tests, followed by direct installation over V185 and on-device checks of results, analytics, exports, historical grading behaviour, and policy-conflict handling.
