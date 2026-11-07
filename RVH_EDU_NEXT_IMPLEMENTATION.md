# RVH Edu — Next Implementation Checkpoint

## Current checkpoint: V26 Bulk Marks Draft Recovery

The existing bulk marks screen now stores device-local recovery snapshots scoped to year, class, term, section, and subject. If the app process is interrupted, teachers can reopen the same context and recover their unsaved input for review. Recovery values are not automatically written to the academic database. Explicit discard and a fully successful save clear the snapshot; partial failures retain the draft for recovery.

## Next priorities

1. Run a real Android process-death/relaunch test on device and confirm the recovery UX is clear.
2. Test partial saves against finalized assessments and confirm only failed rows remain dirty.
3. Review the assessment lifecycle and make draft/final status visible and understandable to teachers.
4. Validate class progression with runtime SQLite rollback tests and a preview before confirmation.
5. Continue end-to-end reconciliation of class results, rankings, report cards, certificates, and exports.

## Build/testing caveat

Static source checks and archive integrity are not a substitute for a full Android build and physical-device testing. Do not describe this checkpoint as device-verified until those steps have actually been performed.

## V27 checkpoint — individual mark-sheet safeguards
- The individual learner mark sheet now tracks edits against the loaded marks and warns before term, section, or back navigation would discard them.
- Finalization is visibly blocked while required subject marks are missing, and the UI lists those subjects.
- Service-level completeness and persisted-status checks remain the authoritative enforcement layer.
- Validation is source-level only; full Android build/device verification remains outstanding.

## V28 checkpoint — atomic individual mark-sheet draft save

The individual mark sheet now submits the complete subject set to a single transactional replacement instead of committing one subject at a time. Next, review assessment reopen/finalization history and reconcile class analytics/report-card calculations against the school-provided reference documents and actual expected rules before expanding features.

## V29 checkpoint — school-specific grading-table validation and OCR documentation cleanup
- The grading-scale editor now rejects incomplete rows, out-of-range mark thresholds, duplicate mark thresholds, missing zero-floor coverage, invalid aggregate values, duplicate division limits, and duplicate/blank division labels.
- Documentation now clearly states that OCR/camera/AI-vision mark capture is excluded from the active workflow and removes the obsolete OCR setup and feature claims.
- Important remaining work: make class/grade structure, subjects per class, pass thresholds, grading modes, aggregation rules, ranking rules, and assessment schedules configurable per school. Current subject catalog and class modes are still defaults; this checkpoint does not claim universal school-rule support.
- Source-level validation only; full Android build and device verification were not performed in this environment.

## V30 checkpoint — Home and settings organization

- App entry now opens a welcome/home screen with direct actions for the main teacher workflows.
- A dedicated settings hub groups school details/grading configuration, backup/restore, archived learners, and class progression.
- The previous grading form remains accessible as Quick Grading.
- Regression check: `tools/check_home_settings_navigation.py`.
- Next: split the remaining navigation and state orchestration out of `MainActivity.kt` into a typed navigation/controller layer, then make subjects/classes/pass/ranking/assessment rules truly school-configurable and persist them safely.

## V31 checkpoint — navigation state and pass threshold
- Shared route/workspace selection state is owned by `ui/AppNavigationState.kt` rather than declared inline in `MainActivity`.
- School pass threshold is persisted and passed into class analytics; it defaults to 50% for existing installs and validates values from 0–100.
- Remaining school configuration work: school-owned subject catalog per class, assessment schedule, ranking/tie policy, and explicit per-school grading profiles. Those must be wired into engines and reports before being described as supported.

## V32 checkpoint — subject catalog consistency
`GradingLogic` now delegates subject lists and short labels to `SubjectCatalog`; the catalog has an integrity validator and a regression check. This removes a duplicate source of truth but does not yet make subjects configurable in Settings. The next safe step is a school policy/profile model and migration plan before enabling editable subjects, because changing subject codes/counts affects saved marks, assessment completeness, analytics, imports, reports and history.

## V33 checkpoint — school-specific subject lists

A settings screen now edits the active lower-primary and upper-primary subject lists and persists them locally. Existing marks are not deleted by list edits, but a changed active list can affect completeness and downstream reporting. Versioned academic policy snapshots and class-specific subject assignments remain necessary before schools should freely change policies mid-year.

## V34 checkpoint — subject configuration revision history
- School subject settings retain up to 50 timestamped configuration revisions and allow a previous revision to be restored as a new active revision.
- The legacy current-catalog preference key remains readable for existing installations.
- This is not yet per-assessment policy pinning: historical results may still resolve against the active subject catalog. Next, persist a policy revision reference and a stable snapshot of the relevant subject/rule definitions on each assessment, then route reporting and analytics through that snapshot.

## V35 checkpoint — assessment subject-policy snapshot foundation
New assessments now persist a versioned snapshot of their class subject list in SQLite. Existing assessments upgraded from v4 keep a null snapshot rather than having today's configuration falsely presented as historical truth. Next: capture the complete grading/pass/aggregate/division/ranking policy, then make calculations and report/analytics consumers resolve the policy attached to each assessment.

## V36 checkpoint — complete scoring-policy snapshot
- Assessment snapshot schema v2 captures subjects, mark-to-aggregate bands, aggregate-to-division bands, and the configured class pass threshold.
- Newly saved assessment results use the captured subject list and grading scale for scoring, so later changes to the active catalog/scale do not silently alter this assessment's calculation.
- Schema v1 snapshots remain parseable and explicitly remain subject-only; they do not claim to recover scoring settings that were never recorded.
- Remaining work: route historical report cards, class rankings, analytics, and exports through each assessment's pinned policy; add richer policy labels and tests. No full Gradle/device build verified.


## V37 checkpoint — Historical policy consumption
- Central assessment history calculation now uses the persisted snapshot for subject codes and complete scoring rules.
- Report-card subjects and default class analytics threshold/subject list consume the saved policy where available.
- Remaining: verify rankings and all export formats use per-assessment policy consistently; implement policy-aware per-subject aggregates for all report/export paths; build/device QA.

## V38 checkpoint — report-card aggregate policy
- Report-card aggregate values for upper-primary assessments now use the saved complete grading-scale snapshot, not potentially stale cached mark aggregates.
- The remaining consistency work includes policy-aware exported report files and ensuring all class-level summaries handle policy differences across learners explicitly.

## V39 checkpoint — historical-policy-aware exports

PDF, Excel, and CSV class-result exports now accept an assessment policy snapshot and use its subject order and display names when available. The export call sites pass the captured policy from the selected class/term/section. Legacy assessments still use the active catalog. Next: detect and clearly handle a class export where learner assessments have conflicting snapshots, then finish policy-aware ranking/division and analytics across mixed historical policies.

## V40 checkpoint — mixed-policy export safety
- Class export call sites now supply policy snapshots for each learner included in the export.
- Export validation rejects a class report when learner policies differ, when pinned and legacy assessments are mixed, or when a saved policy is malformed. It no longer silently borrows the first available policy for the whole class.
- Equivalent policies captured at different timestamps are treated as compatible; capture time is metadata, not a scoring rule.
- Next: policy-aware class ranking/division calculations and per-subject analytics, then validate with a real Android build and device-level tests. Mixed-policy data should be handled explicitly rather than averaged as if it shared one scoring system.

## V41 checkpoint — exact normalized ranking ties

Ranking now compares normalized totals as exact fractions instead of relying on floating-point equality. This makes equal percentage results share the same competition position even when learners have different maximum totals. It does not yet solve the separate issue of policy compatibility across a combined class ranking; that needs explicit class-level validation and a UI-safe explanation path.


## V42 checkpoint — class policy compatibility

A shared compatibility guard now prevents class-wide rankings, analytics, and exports from silently combining invalid, mixed legacy/pinned, or materially different saved assessment policies. The UI catches the validation failure and surfaces a blocked-operation message. Capture timestamps are ignored when comparing equivalent policies.

Next: review the user-facing conflict recovery path and ensure teachers can identify which learners have missing/legacy/conflicting policy snapshots without altering historical records. Then add targeted tests for the policy signature rules and verify with a real Gradle build when the Android build environment is available.

## V43 checkpoint — learner-level policy conflict diagnostics

Class-wide result and analytics policy failures now include concise learner names for records needing review, instead of only a generic policy-conflict message. Next: provide a dedicated teacher-facing review screen that classifies each assessment as legacy/unpinned, invalid, or policy-different, while preserving historical snapshots and never auto-rewriting them.

## V44 checkpoint — teacher-facing assessment policy review
- Added a read-only screen for inspecting assessment policy status learner by learner for the selected class, year, term, and section.
- Statuses distinguish missing/legacy snapshots, invalid snapshots, mixed policy sets, and policy conflicts. No automatic repairs or historical rewrites occur.
- Blocked class-result and analytics operations route to the review screen to make the next step actionable.
- Added `tools/check_assessment_policy_review_screen.py`.
- Next: add deliberate, audited recovery actions only after defining safe rules for migrating legacy snapshots; do not infer a historical grading policy from current settings.

## V45 checkpoint — policy review usability
- The policy review screen now summarizes total, saved-policy, and needs-review records and provides filters.
- Record-level guidance emphasizes checking original records and approved school rules before making any historical correction.
- The review screen remains read-only; no automated policy repair or historical rewrite is performed.
- Project packaging should place Gradle files and `app/` directly at ZIP root.

## V46 checkpoint — invalid historical policy fails closed
- A malformed non-null policy snapshot no longer silently falls back to current grading settings during mark saving or historical calculation.
- Genuine `null` legacy snapshots remain distinguishable from corrupted/unsupported saved policy data.
- No historical record is auto-repaired or overwritten.
- Next: give teachers an explicit, audited policy-recovery action that requires choosing an authoritative historical source; preserve the original snapshot and log the correction.

## V47 checkpoint — strict saved-policy identity validation

Assessment policy parsing now rejects blank or case-insensitively duplicated subject identities, unknown school classes, negative capture timestamps, empty grading-band lists, and blank division labels. Malformed saved policy JSON continues to fail closed instead of silently falling back to active settings. See `RVH_EDU_CHANGELOG_V47.md`.

## V48 checkpoint — diagnostic row identity
Policy conflict diagnostics now retain each source row's identity during parsing. This prevents repeated learner names from being used as a lookup key when determining which record needs review. The change is read-only and does not alter historical marks or snapshots.

## V49 checkpoint — complete conflict-group identification

Policy review and blocked-operation diagnostics now flag every pinned learner policy when more than one materially different policy signature is present. This avoids implying that the first policy encountered is authoritative. The change is diagnostic only and does not alter stored academic data.

## V50 checkpoint — learner archive/restore atomicity

Archive and restore now run within SQLite transactions, reject unsupported learner status values rather than overwriting them, and avoid needless writes when the learner is already in the requested state. Historical enrolments and assessment records remain untouched. Source regression coverage: `tools/check_learner_archive_restore_atomicity.py`.

## V51 checkpoint — transaction-bound progression validation

The class progression repository now rechecks that every learner still exists and is `ACTIVE` within the insertion transaction, and validates nonblank learner IDs and target-year ranges at the repository boundary. This closes a gap where service-layer checks could become stale before the batch insert. Existing target-year duplicate prevention and atomic rollback behavior remain in place. See `RVH_EDU_CHANGELOG_V51.md` and `tools/check_progression_transaction_revalidation.py`.

## V52 checkpoint — source-enrollment revalidation
- Progression now verifies the exact source enrollment identity, learner ID, source academic year, and source class inside the same SQLite transaction as destination enrollment insertion.
- A stale roster or changed source enrollment aborts the entire batch instead of creating a destination record from outdated selection state.
- Existing history is preserved; this does not alter previous marks or assessments.
- Added `tools/check_progression_source_enrollment_revalidation.py`. Full Gradle/device verification remains outstanding.

## V53 checkpoint — class placement preview
- The class progression screen distinguishes promotion to the next class, repeating the same class, moving to a lower class, and skipping classes.
- Non-standard placement paths display a caution and repeat that decision in the confirmation dialog.
- School-leaver handling is explicitly separate from progression; existing academic records remain preserved.
- Added `tools/check_progression_placement_preview.py`.
- Next: review the actual archive/restore UI and ensure school-leaver actions are clearly labelled, reversible, and do not imply that archival deletes academic history. Then validate progression rollback and screen behavior with a real Android build/device test.

## V54 checkpoint — learner status audit
- Added a durable learner status history table through database migration 6.
- Archive requires a reason and accepts an optional note; archive/restore status changes and audit events are atomic.
- Archived learner list displays the most recent archive reason.
- Source-level regression check: `tools/check_learner_status_audit.py`.

## V55 checkpoint — repository-boundary learner identity validation
- Learner repository writes now validate ID, required name, optional age range, and supported statuses rather than relying only on UI/service validation.
- Names and admission numbers are normalized before persistence. Admission-number collision checks normalize existing stored values too, protecting against older lower-case/whitespace variants.
- Duplicate checks exclude the same learner ID, so legitimate edits can preserve an existing admission number.
- Historical records and learner IDs are not modified by this validation change.
- Next: build a safe duplicate-candidate review workflow using admission number, normalized name, age and historical context; never merge records automatically based on a name match alone.
- Source-level validation only; full Gradle/device verification remains outstanding.

## V56 checkpoint — duplicate learner review foundation

Added `LearnerDuplicateMatcher` and repository lookup. Matching is a review aid only: admission-number/date-of-birth collisions are marked high confidence; matching names remain review suggestions, and no record is automatically merged. Next, wire candidate review into the learner-creation/edit UI and require explicit teacher choice when a strong collision is found.

## V57 checkpoint — Duplicate review connected to learner forms
- Added duplicate-candidate lookup to `AcademicDataService` using the existing repository matcher.
- Wired add/edit learner dialogs to show matching records and reasons before save; editing excludes the learner being edited.
- Explicitly confirmed continuation is available for legitimate same-name/different-person cases. Repository-level duplicate admission-number rejection remains authoritative.
- Added `tools/check_duplicate_review_ui_wiring.py`.
- Full Gradle build/device testing remains outstanding.

## V58 checkpoint — atomic normalized admission-number uniqueness
- Learner repository create/update now shares the duplicate review's canonical admission-number normalization.
- Duplicate checks scan existing stored values after normalization, catching legacy values whose case or whitespace differs.
- The scan and write are enclosed in one SQLite transaction, and edits exclude their own learner ID.
- Existing learner records are not automatically merged or modified by this check.
- Next: add an explicit, auditable duplicate-resolution workflow that compares records and associated academic history before any teacher-approved correction. Continue to preserve original records and never merge on name alone.
- Validation is source-level only; full Gradle build and device testing remain outstanding.


## V59 checkpoint — backup and identity lookup integrity
- Added `learner_status_history` to portable database backups and restore validation so archive/restore audit events are not silently lost.
- Bumped backup schema to 5 while retaining schema-4 restore compatibility; older backups correctly start with no status audit events because they never stored them.
- Restore clears audit child rows before deleting learners and restores in foreign-key-safe order within the existing transaction.
- Database integrity row counts include the learner status history table.
- Admission-number lookup now shares the repository's canonical normalization, so old values with case/whitespace differences can still be found.
- Added `tools/check_backup_includes_learner_status_history.py` and `tools/check_admission_lookup_normalization.py`.
- Full Gradle build and on-device backup/restore testing remain outstanding.

## V60 checkpoint — bounded backup handling

- Limit backup JSON payloads to 64 MiB during generation and import.
- Stream-import with a byte limit before JSON parsing; reject oversized inputs before opening a database transaction.
- Generate the export payload before opening the destination stream.
- Explicitly validate that all expected tables are present before restore.
- Added `tools/check_backup_size_and_safe_write.py`.

## V61 checkpoint — backup semantic preflight
- Added pre-transaction domain validation for learner, enrolment, assessment, mark, and learner-status-history rows.
- Invalid references and unsupported enum-like values are rejected before existing database rows are deleted.
- Added `tools/check_backup_semantic_preflight.py`.
- Full Gradle build and device-level restore testing remain required.

## V62 checkpoint — backup semantic integrity
- Added preflight detection for duplicate compound-key records that would violate the database's unique constraints.
- Added validation of legacy identity-review references and transition validity for learner status history.
- Packaged project files at ZIP root.
- Remaining verification: Android Gradle build and device-level restore tests, including simulated failed restores against a copy of a test database.


## V63 checkpoint — historical-policy-aware integrity
- Finalized assessments with a saved policy are checked against the snapshot's subject codes and class identity, not today's mutable subject catalog.
- Legacy finalized assessments without snapshots are not falsely rejected due to present-day catalog changes.
- Added `tools/check_backup_integrity_uses_historical_policy.py`.
- Full Gradle build/device restore testing remains outstanding.

## V64 checkpoint — backup integrity checksum
- New backups use format version 2 with a SHA-256 checksum over canonicalized table data.
- Version 1 backups remain supported as legacy imports without checksum guarantees.
- Restore verifies version 2 checksum before semantic validation and before the restore transaction can delete existing rows.
- Regression coverage: `tools/check_backup_checksum_versioning.py`.

## V65 checkpoint — exact backup row shape

Restore preflight now rejects backup rows that omit any database column, including nullable columns, before the destructive transaction. This prevents an omitted field from being silently restored as a default or NULL. See `RVH_EDU_CHANGELOG_V65.md`.


## V66 checkpoint — canonical numeric checksum values
- Canonical checksum serialization normalizes equivalent JSON numeric spellings using `BigDecimal` (including exponent notation and trailing-zero variants), while keeping booleans separate.
- Extended `tools/check_backup_checksum_versioning.py` to guard the numeric canonicalization implementation.
- Remaining verification: compile on Android and run real restore tests for formatting-only changes, modified data, legacy v1 backups, and rollback on failed restore.


## V67 checkpoint — executable checksum primitives
- Production checksum number normalization and SHA-256 moved into `BackupChecksumPrimitives`, a pure-JVM helper.
- `tools/pure-tests/BackupChecksumPrimitivesRegression.kt` runs against the same helper and covers equivalent numeric spellings, negative zero, decimal normalization, the SHA-256 `abc` known vector, deterministic output and changed-content detection.
- This does not yet exercise Android `JSONObject` canonicalization or the full SQLite restore transaction; those remain integration-test work.


## V68 checkpoint — restore transaction safety regression guards
- Added `tools/check_backup_restore_transaction_safety.py` to enforce source ordering: checksum verification, domain preflight, installed-schema row validation, then destructive transaction.
- Guarded the critical order in which final database integrity is checked before `setTransactionSuccessful()`, with transaction closure in a `finally` block.
- Guarded version-1 compatibility and version-2 checksum enforcement.
- These checks inspect source structure; they do not replace an end-to-end SQLite rollback test.

## V69 checkpoint — read-only restore preview
- Added a pre-confirmation preview with export time, format/schema version, learner/enrolment/assessment/mark counts, and checksum status.
- Preview checks file bounds, backup envelope, required table arrays, and version-2 checksum without opening or mutating the database.
- Full semantic and installed-schema checks remain in the actual restore path; preview is not presented as complete validation.
- Remaining: runtime tests for Android JSON canonicalization and end-to-end SQLite rollback behavior.

## V70 checkpoint — production canonical JSON is runtime-testable
- Moved canonical JSON tree serialization into `BackupChecksumPrimitives`, which is called by the Android backup manager after adapting `JSONObject`/`JSONArray` values.
- Runtime tests now cover deterministic object-key ordering, nested number normalization, preserved array order, Boolean/number distinction, null, string escaping, and SHA-256.
- Remaining integration gap: exercise actual Android `org.json` parsing plus the full SQLite restore transaction and prove rollback on injected failure.

## V71 checkpoint — bounded checksum number canonicalization

Canonical number serialization now avoids expanding extreme exponents into potentially enormous plain-decimal strings and explicitly rejects invalid/non-finite numeric inputs. The executable JVM regression suite now exercises extreme exponents, equivalent number forms, and checksum stability/tampering using the production helper. Full Android/SQLite restore integration remains unverified.

## V72 checkpoint — professional interface design pass

- Established a shared Material 3 visual system using a restrained academic-blue/teal palette, semantic light/dark color schemes, and consistent rounded component shapes.
- Rebuilt the home dashboard with clearer product identity, an offline workspace label, active-learner and academic-year summary metrics, and a compact two-column grid for the nine existing workflows.
- Preserved existing navigation callbacks, offline-first behavior, learner-first manual entry, and the Hellen dedication.
- Added `tools/check_professional_ui_design.py` to protect key dashboard and design-system expectations.
- Validation: 53 Python source regression scripts passed; Python tools compile. Android Gradle build and visual device validation remain unverified in this environment.
- Next: run CI/Gradle and inspect every screen on the Spark 30C, then tune small-screen spacing, keyboard behavior, accessibility contrast, and long learner names based on real-device evidence.

## V73 checkpoint — required first-launch personal PIN
- The app starts in a locked state and presents PIN creation when no credential exists; users must enter and confirm a 4–8 digit PIN before accessing the dashboard.
- New PINs are stored using a per-credential random salt and PBKDF2-derived hash, not plaintext.
- Existing plaintext PINs are upgraded only after a successful match, then the old preference is removed.
- Added `PinCredentialRegression.kt` (12 runtime assertions) and `tools/check_first_launch_pin_setup.py` (13 source guards).
- Full Gradle build and Spark 30C validation remain outstanding.

## V74 checkpoint — learner directory and settings UI refinement
- Learner management now uses a structured class/year summary, searchable roster, initial avatars, a primary marks action, and a compact overflow menu for profile/edit/archive.
- Empty roster and no-search-results states have clear next actions. Existing callbacks for class/year selection, duplicate review, editing, archiving, profile, marks and bulk marks are preserved.
- Settings are grouped into school configuration and records/safety, with consistent icon surfaces and explanatory descriptions. All prior navigation callbacks remain connected.
- Added `tools/check_ui_learner_settings_polish.py`. Source checks are not a substitute for compilation or visual/accessibility checks on the Tecno Spark 30C; no full Android build was performed.
- Next UI pass: refine mark-entry and class-results screens, then inspect every screen at target-device dimensions, including keyboard overlap, long names, accessibility and dark mode.

## V75 checkpoint — mark-entry UI polish
- Reorganized the bulk mark-entry screen into a clear academic header, grouped horizontally scrollable assessment selectors, visible progress/unsaved states, and learner cards.
- Preserved draft recovery, unsaved-change confirmation, input limits, and save-only-changed-row behaviour.
- Added `tools/check_ui_mark_entry_polish.py` for source-level regression checks.
- Remaining UI work: review class results and learner academic-history screens, then complete build/device visual QA.


## V76 checkpoint — Results and analytics UI

- Refined class results with clearer assessment-period controls, metric cards, incomplete-result warnings, ranked learner cards, and visible export/report actions.
- Refined class analytics with completion and pass-rate summaries, subject coverage bars, and clearer risk flags.
- Kept existing calculation and navigation callbacks unchanged.
- Added `tools/check_ui_results_analytics_polish.py`; full Android build/device visual verification remains outstanding.

## V77 checkpoint — learner profile and academic history UI
- Reworked learner profiles into an identity header, academic overview metrics, subject insights, and grouped enrolment history cards.
- Incomplete assessment rows now display a dash instead of presenting the partial average as a final result; missing subject names remain visible.
- Academic History now supports searchable filtering by learner name, class, stream, and academic year, with learner counts, clearer summary cards, and explicit no-data/no-match states.
- Preserved existing learner-open callbacks and historical snapshot consumption; no grading formulas were changed.
- Added `tools/check_ui_learner_history_polish.py` (12 source-level assertions). Python compilation and focused UI regression checks passed. No Gradle wrapper is present in this archive, so Android compilation and physical-device visual QA were not performed.
- Next: refine PIN/entry and common dialogs, then perform a whole-app visual/accessibility audit and a real Android build on CI or a configured Android environment.


## V78 checkpoint — PIN and entry-screen polish
- The PIN gate now uses RVH Edu branding and the shared light/dark theme, with scroll/IME handling for smaller screens.
- First-launch PIN setup, existing PIN unlock, hashed credential storage, and legacy migration are preserved.
- Remaining UI release work: shared dialogs/notifications and a whole-app visual/accessibility audit, followed by a real Android build and Spark 30C testing.

## V79 checkpoint — shared confirmation dialog UI
- Introduced a themed reusable confirmation dialog for consistent title/body/action treatment.
- Applied it to individual and bulk mark-entry unsaved-change prompts, highlighting destructive discard actions while retaining a safe keep-editing path.
- Added `tools/check_ui_shared_dialogs.py`.
- Full Gradle build and device verification remain required before release.

## V80 checkpoint — Consistent Form Feedback
- Added shared `RvhFeedbackBanner` for success, error, and informational feedback with theme-aware colors and polite accessibility announcements.
- Applied it to grading settings and class progression screens while preserving existing validation and operation state logic.
- Added `tools/check_ui_feedback_consistency.py`.
- Android build/device verification remains a release gate.

## V81 checkpoint — shared form components
- Added reusable `RvhFormTextField` with consistent dimensions, helper/error text, keyboard type and IME action options.
- Applied it to learner add/edit and archive forms; age uses a numeric keyboard, archive note limit is preserved, and save failures use the shared feedback banner.
- Added `tools/check_ui_shared_form_fields.py` (11 source-level assertions). Focused form checks, learner status-audit check, and Python compilation passed.
- Full Gradle build and Spark 30C visual QA remain release gates; no Gradle wrapper or system Gradle was available in this archive.
- Next UI pass should audit the remaining settings and data-entry forms for shared field adoption, then address navigation/loading/disabled states and perform whole-app visual QA rather than adding unrelated features.

## V82 checkpoint — settings forms use shared components
- Standardized school grading settings and subject configuration inputs on `RvhFormTextField`.
- Retained numeric filtering, subject-code normalization, short-name length limits, existing save callbacks, and revision restore behaviour.
- Added `tools/check_settings_shared_forms.py`.
- Still required before release: full Android Gradle build and visual/keyboard/accessibility checks on the Tecno Spark 30C.

## V83 checkpoint — remaining workflow form consistency
- Applied shared form styling to archived learner search, class progression target year, and import-review learner/mark inputs.
- Progression year now has a numeric keyboard, contextual helper, and inline invalid-year message.
- Import-review fields now explain name review and the valid mark range while preserving the draft-before-confirm workflow.
- Added `tools/check_remaining_forms_shared_design.py` (12 source-level checks).
- Not yet verified with a full Android build or physical-device test.


## V84 — UI polish handoff / release gate
- Completed the planned shared-UI refinement pass: home/dashboard, learner directory and profiles, mark entry, results/analytics, PIN gate, settings/forms, feedback banners, and confirmation dialogs have been addressed across V72–V84.
- V84 removes a brittle fractional-width constraint from the shared confirmation dialog's dismiss action and adds `tools/check_ui_release_polish.py`.
- **UI work is paused here** so development can move to the next project. Do not keep creating cosmetic versions without new device evidence.
- Release status is **UI-refined candidate, not device-verified release**: this archive has no Gradle wrapper and the environment has no system Gradle. Full Android build/CI and a Tecno Spark 30C visual/accessibility pass are still required.

## V85 checkpoint — professional exported documents
- Updated PDF presentation for individual report cards, certificates, and class results. Existing academic calculations and historical policy handling remain authoritative and unchanged.
- Report cards now have school/learner identity hierarchy, subject table, summary metrics, result-completeness status, comments, and signature lines.
- Certificates use a landscape, framed presentation with learner/class/year/result details and signature/date areas.
- Class-result PDFs include summary metrics, alternating rows, adaptive subject columns, incomplete-subject notes, and page labels.
- Focused source assertions live in `tools/check_professional_academic_exports.py`.
- Before build/release, still verify generated PDFs against real sample data, long school/learner names, maximum subject count, multi-page class lists, sharing/printing, and actual A4 paper/phone previews. Android build/device validation is still outstanding.

## V86 checkpoint — PDF layout boundary safeguards
- Class-results PDF subject columns now use a compact 12-point lower bound rather than forcing at least 28 points, reducing right-edge overflow for larger subject configurations.
- Pagination reserves additional row height for the missing-subject disclosure shown beneath incomplete learners.
- Added `tools/check_export_layout_safety_v86.py` (6 source-level assertions) and expanded the professional academic export checks to 13 assertions.
- Focused checks and Python syntax compilation passed. This is still source-level validation only; rendered PDF QA, full Android build, and Spark 30C verification remain outstanding.


### V87 academic document checkpoint
Report cards now split subject rows across pages, retain school/learner context on continuation pages, and reserve the final page for summary/comments/signatures. Source checks do not substitute for generated-PDF visual QA; build and device/print validation remain required.


### V88 academic export checkpoint — pause feature changes
- Class-results PDF subject-column width now derives from the space remaining after the actual learner/result columns, with no hard minimum that can force horizontal overflow.
- Added `tools/check_class_pdf_column_geometry_v88.py` to check source structure and model the layout across up to 80 subjects for lower and upper classes.
- This is the final source-level export-safety pass before build/QA. Do not call the release verified until a full Android build, real generated-PDF visual review, A4 print check, and Spark 30C device test are complete.

## V92 settings interaction checkpoint

Settings save actions now dismiss the software keyboard and show a visible outcome for school name, pass threshold, grading rules, and subject configuration. Subject revision restore also dismisses the keyboard and uses the shared feedback banner. Focused source assertions are provided in `tools/check_settings_save_feedback_v92.py`. Full Gradle compilation and device testing remain required.


## V115 checkpoint — school-local admission numbers
- Added schema-v9 `school_admission_number` assignments so admission numbers belong to a school, not the shared learner identity.
- Backfilled existing numbers to each school where a learner is enrolled, preserving learner/enrollment/assessment/mark IDs, then cleared the legacy shared column. Learners without enrollment remain represented under `legacy-school`.
- Learner add/edit/roster/archive/restore paths now resolve the active school’s admission number. Duplicate checks are school-local.
- Backup export includes the new table; restore accepts older backup schemas and migrates legacy admission numbers within the restore transaction.
- Added `tools/check_multi_school_v115.py`; full Android build and device upgrade/restore verification remain mandatory.
- Next: test V8-to-V9 migration and old/new backup round trips on Android/CI, inspect any legacy normalized-admission collisions, then continue multi-school isolation through exports, archives, learner history, and school switching before update/release hardening.
