# RVH Edu

RVH Edu is an offline-first Android academic record and grading app. The working workflow is learner-first and manual-entry-first: register learners, create assessment sessions, enter marks, review results, and produce academic records and exports.

## Core workflow

1. **Learners** — register, edit, archive, and restore learner records. Archiving removes a learner from active rosters without deleting academic history.
2. **Assessments** — choose the academic year, class, term, and assessment section.
3. **Marks** — enter marks manually, individually or in bulk. Drafts can be reviewed before finalization; finalized assessments are protected from ordinary edits.
4. **Results and history** — review learner and class results, rankings, progress, analytics, and exported academic documents.
5. **Backup and restore** — use device-local data and explicit backup workflows to protect school records.

## No OCR screen

OCR, camera-based marks capture, and AI-vision scanning are intentionally excluded from the active app workflow. Teachers enter marks directly so names and marks are not silently guessed from photographs. This build does not require an AI API key or an OCR setup step.

## School-specific rules

Schools do not all use the same subjects, class structures, grading boundaries, pass criteria, aggregate rules, division labels, ranking rules, or assessment schedules. The grading-scale screen allows staff to edit the current mark-to-aggregate and aggregate-to-division tables, with validation to reject ambiguous or incomplete tables. The School Subjects screen lets staff configure the active lower-primary and upper-primary subject lists. **Some academic rules remain fixed**: lower-primary and upper-primary subject lists are now editable per installation, and grading tables are editable, but arbitrary class structures, class-specific subject assignments, ranking policies, and versioned policy snapshots are not yet implemented. Review existing records before changing the active subject list.

When changing grading rules, review the effect on historical results and issued reports. Keep a verified backup before changing school-wide settings.

## First screen and navigation

The app opens on a welcome dashboard with direct actions for enrolling learners, entering individual or class marks, viewing results and analytics, opening academic history, importing a reviewed spreadsheet, and reaching settings or backup. Settings are grouped behind one **Settings & School Setup** hub. The previous grading form remains available as **Quick Grading**.

## Project layout

See [`ARCHITECTURE.md`](ARCHITECTURE.md) for the responsibility map. At a glance:

- `app/` — Android application source and resources
- `app/src/main/java/.../core/` — academic models, workflows, grading, analytics, repositories, import and history
- `app/src/main/java/.../ui/` — focused Jetpack Compose screens
- `app/src/main/java/.../export/` — PDF/CSV/spreadsheet and document export
- `tools/` — source-level integrity checks and pure regression tests
- `RVH_EDU_NEXT_IMPLEMENTATION.md` — current development checkpoint and next priorities

## Visual design

RVH Edu uses a shared Material 3 design system with a calm academic-blue palette, restrained teal accents, accessible light/dark color schemes, consistent rounded geometry, and a task-focused dashboard. The home screen prioritizes learner and academic-year context, then presents common workflows in compact, clearly labeled action cards. Bulk mark entry uses a class/year header, grouped horizontally scrollable assessment selectors, clear entered/missing progress, a visible unsaved-state banner, and consistent learner cards. Learner profiles, academic history, results, and analytics use the same information hierarchy and explicit incomplete-data states. The first-launch PIN gate follows the same design system, adapts to the on-screen keyboard, and keeps setup/unlock feedback clear. Interface polish should never hide important actions, invent unsupported capabilities, or compromise offline operation.

## Verification status

Run the available Python source-level checks from the project root with `python3 tools/check_*.py`. These checks help catch known source regressions but are not a substitute for an Android Gradle build, database/runtime tests, or installation and end-to-end testing on the target device. Report only the checks actually run for a given release.


## V34 — Subject configuration revision history

School subject settings retain up to 50 timestamped revisions. A previous revision can be restored as a new revision. This history does not yet pin policy snapshots to individual assessments. OCR remains excluded from the active workflow.


## V36 — assessment policy snapshots
New assessments now preserve their subject list, grading bands, division bands, and class pass threshold in a versioned policy snapshot. New assessment scoring uses its saved subject list and grading scale. Historical analytics and report exports still need to be migrated to read these pinned policies.


## V38 — Historical report aggregate consistency

Report cards now calculate upper-primary subject aggregates from the assessment’s saved complete grading-scale snapshot, including normalization for marks not out of 100. Legacy subject-only snapshots are not treated as if they contained historical scoring rules. Source-level regression checks pass; a full Android Gradle build and device test remain outstanding.


## V39 — Historical-policy-aware exports

Class-result PDF, Excel, and CSV exports now use saved assessment subject order and labels when a valid policy snapshot is available. Legacy records retain the active-catalog fallback. See `RVH_EDU_CHANGELOG_V39.md`.

## V40 — mixed-policy export safety
Class-result export now checks every included learner's saved assessment policy. It refuses to create a combined report when policies disagree or when pinned and legacy records are mixed, instead of silently applying one learner's policy to the whole class. Source-level checks pass; a full Android build and device test remain outstanding.

## V54 — auditable learner archiving
Learner archiving now requires a reason and accepts an optional note. The status change and audit event are written in one database transaction; restoring a learner also records a status-history event. The Archived Learners screen displays the latest archive reason. Database schema version 6 adds the status-history table without rewriting existing academic records. Source-level checks pass; a full Android Gradle build and device test remain outstanding.

## V56 — learner duplicate-review foundation

Added a deterministic possible-duplicate matcher for normalized admission numbers, dates of birth, and normalized full names. Each candidate includes review reasons; strong identity collisions are distinguished from name-only suggestions. No records are merged or changed automatically. Name/age lookup now uses consistent normalization. Source-level checks pass; a full Android Gradle build and device test remain outstanding.


## Latest source checkpoint: V57

V57 connects duplicate-learner review to the add and edit learner dialogs. Potential matches are shown before saving; teachers may return to edit details or explicitly confirm a distinct learner. No automatic merge is performed. See `RVH_EDU_CHANGELOG_V57.md`.


## V72 — interface design pass

Introduced a consistent RVH Edu visual system and rebuilt the home dashboard around school identity, learner/year summary metrics, and a compact two-column task grid. The UI remains learner-first and offline-first; existing actions and callbacks are preserved. Visual rendering and Android compilation must still be verified on-device/build CI.

## V74 — learner directory and settings polish

The learner directory now has a clearer class/year summary, search and count context, designed empty states, learner initials, and a compact action menu while preserving the existing marks, profile, edit, archive and bulk-entry workflows. School settings are grouped into configuration and record-safety sections with consistent icon cards. See `RVH_EDU_CHANGELOG_V74.md`. Android compilation and Tecno Spark 30C visual validation remain outstanding.


### V78 interface checkpoint

The PIN setup and unlock entry point now uses the RVH Edu identity, shared theme colours and shapes, clearer form labels and validation feedback, and scroll/IME handling for compact screens. First-launch setup, salted credential storage, legacy migration, and offline behaviour are preserved.

## Latest UI checkpoint — V79

V79 introduces a shared, theme-aware confirmation dialog for consequential actions and applies it to unsaved-mark prompts in individual and bulk mark entry. Destructive discard actions are clearly differentiated, and the safe keep-editing path remains available. See `RVH_EDU_CHANGELOG_V79.md` for scope and validation limits.


## UI handoff status (V84)

The planned UI refinement pass is paused at V84 so work can move to the next project. Shared components now cover form fields, inline feedback, and confirmation dialogs; key screens have been refined for consistent hierarchy and incomplete-data clarity. See `RVH_EDU_CHANGELOG_V84.md` and `RVH_EDU_NEXT_IMPLEMENTATION.md`. This is a UI-refined candidate, not a device-verified release: complete Android compilation and visual/accessibility testing on the Tecno Spark 30C are still required.


## V86 academic export layout safety

V86 tightens class-results PDF layout safety for larger subject configurations and incomplete-result disclosures. The change has focused source-level regression coverage; full Android build, rendered PDF inspection, and physical printing still need verification. See `RVH_EDU_CHANGELOG_V86.md`.


## V87 report-card pagination

V87 adds multi-page report-card layout so large subject lists do not run through comments/signatures or beyond the A4 page. Totals and comments are placed on the final page, with continuation identity and page numbering. Android compilation and rendered-PDF/A4 verification remain outstanding. See `RVH_EDU_CHANGELOG_V87.md`.


## V88 — final class PDF column safety

V88 calculates class-results subject columns from the actual remaining page width, avoiding a hard minimum that can force wide tables beyond the right margin. Focused source and geometry regression checks cover large subject counts. Actual PDF rendering/printing and Android/device verification remain required. See `RVH_EDU_CHANGELOG_V88.md`.

## Android build-fix handoff (V89)

V89 addresses Kotlin compiler diagnostics reported by the `assembleDebug` workflow, including Activity Result imports, academic-year type handling, missing model imports, Material 3 API usage, and class-export roster scope. These are source-level fixes; the full Android build must be rerun in GitHub Actions before treating the APK as build-verified. See `RVH_EDU_CHANGELOG_V89.md`.


## V90 build correction
The explicit coroutine `launch` import required by the navigation drawer open/close actions was added to `MainActivity.kt`. A full Android build still needs confirmation in CI.

## V91 — Live learner updates and responsive inputs

Learner Management now observes the academic data revision when loading the roster, so a successful learner add/edit/archive/restore refreshes the visible list immediately. Learner add/edit forms now give explicit required-name feedback, normalize names and optional admission numbers before saving, and the academic-year field provides live range guidance instead of silently rejecting incomplete input. See `RVH_EDU_CHANGELOG_V91.md`. Full Android build and physical-device interaction checks remain release gates.

### V92 settings interaction feedback

Settings save and restore actions dismiss the on-screen keyboard and provide visible success or failure feedback. Validation errors remain visible so users can correct invalid entries. See `RVH_EDU_CHANGELOG_V92.md`. Android compilation and physical-device verification are still required.

### V93 — Mark-entry save feedback

V93 extends the settings interaction pattern to single and bulk mark entry: saving hides the keyboard, clears focus, and keeps explicit success/error feedback visible. It also fixes success styling for draft-save and reopen messages. See `RVH_EDU_CHANGELOG_V93.md`. Full Android compilation and device verification remain required.

## Latest development checkpoint — V115 multi-school data isolation

V115 introduces school-local admission-number assignments and a schema-v9 migration that preserves learner and academic record IDs while moving legacy admission values into school-specific records. The same admission number can now be used by different schools, while duplicate checks remain scoped to the active school. Backup restore remains compatible with older backups and migrates legacy admission values within its restore transaction. See `RVH_EDU_CHANGELOG_V115.md` and run `python3 tools/check_multi_school_v115.py`. Android Gradle compilation, real database-upgrade/backup-restore testing, and Tecno Spark 30C verification remain release gates.


## V116 — school-scoped history visibility

Learner academic history now requires enrollment in the active school before returning a learner profile or history. Archived-learner reason lookup is also restricted to learners enrolled in the active school. **Lifecycle status remains learner-global in the current schema**; archive/restore must be redesigned around school-owned status before multi-school lifecycle isolation can be considered complete. See `RVH_EDU_CHANGELOG_V116.md`.

## V117 — school-local learner lifecycle
V117 introduces school-specific active/archived status, a v9→v10 additive migration, school-scoped lifecycle audit events, and backup schema v8 support with compatibility backfill for older backups. See `RVH_EDU_CHANGELOG_V117.md` and run `python tools/check_multi_school_v117.py` for the targeted source-level contract check. This does not replace a full Android build/device test.

## V118 — lifecycle write isolation
V118 prevents routine learner profile saves from accidentally overwriting an existing school-local archive/active status. Archive and restore use an explicit `(school_id, learner_id)` status update, with the audit event retained in the surrounding transaction. Run `python3 tools/check_multi_school_v118.py` for the focused source-level contract check. Android compilation, migration/backup round-trip testing, and device validation remain outstanding.

## V119 — Backup and Recovery Validation Hardening

V119 validates school references in enrollments and lifecycle history, and validates school-local learner-status records for references, status values, timestamps, and duplicate keys before restore begins. Older lifecycle-history records without a school identifier are preserved with `legacy-school` attribution rather than guessed attribution. Restore remains transactional. See `RVH_EDU_CHANGELOG_V119.md`. Source-level checks are not a substitute for an Android build or on-device migration/restore testing.

## V120 — Backup Schema Version Alignment

V120 aligns the backup envelope's schema version with the current database schema (v10). Compatible older backups are still accepted, including V119-era schema-9 backups that already contain school-local lifecycle status rows. Compatibility normalization remains after checksum verification and before preflight validation/transactional restore. See `RVH_EDU_CHANGELOG_V120.md`. Source checks do not replace a full Android build or device-level backup/restore testing.

## V121 — Migration and backup round-trip regression fixture

V121 adds a targeted SQLite behavioral fixture alongside source-contract checks for the existing schema-v10 school-local lifecycle migration and backup recovery path. It verifies per-school status backfill, independent status rows for a shared learner, preservation of fixture academic records, and a backup-shaped status-row round trip. Run `python3 tools/check_migration_roundtrip_v121.py`. This is not an Android instrumentation test: Gradle compilation, migration against a real schema-v9 database, app-level backup/restore, and Tecno Spark 30C verification remain outstanding.

## V122 — legacy backup shape compatibility

V122 fixes the restore preflight so compatible older backups are not rejected solely because their rows predate newer database columns. It normalizes pre-multi-school school/enrollment fields and legacy status-history attribution only after verifying the original checksum. Current schema-v10 backups still require an exact column set; older payloads may omit only nullable columns or columns with SQLite defaults. Run `python3 tools/check_legacy_backup_compatibility_v122.py`. This source/SQLite fixture is not a substitute for Android compilation or an app-level backup/restore test.

## V123 backup validation update

V123 tightens the backup envelope: missing tables are synthesized only when the declared legacy schema predates those tables. A schema-v10 backup that omits a required table is rejected rather than silently treating that table as empty. See `RVH_EDU_CHANGELOG_V123.md` and run `python3 tools/check_backup_schema_completeness_v123.py` for the source-contract check.

## V124 — current-backup lifecycle coverage
V124 requires every enrollment in a schema-v10 backup to have a matching school-local learner-status row for the same `(school_id, learner_id)` pair. This prevents restore from silently reconstructing a missing current-schema status from the legacy global learner status. Automatic status backfill is limited to pre-v10 backups. Run `python3 tools/check_backup_lifecycle_coverage_v124.py`. This source-contract/fixture test does not replace Android build, actual database migration, app-level backup/restore, or physical-device testing.

## V125 — School-local lifecycle integrity

The database integrity audit now verifies that each enrollment has a lifecycle-status row for the exact school/learner pair and that lifecycle timestamps are valid. This prevents backup export/restore integrity checks from accepting enrollment data that would have to fall back to shared legacy status. Run `python3 tools/check_database_status_integrity_v125.py` for the standalone SQLite/source-contract regression check. This is not a substitute for an Android build or on-device migration/restore testing; see `RVH_EDU_CHANGELOG_V125.md`.


## V126 — Backup lifecycle timestamp preflight
V126 rejects school-local learner-status rows whose update timestamp precedes their creation timestamp during backup preflight, before the restore transaction can delete existing records. The post-restore database integrity audit remains enabled as a second defence. Run `python3 tools/check_backup_lifecycle_timestamps_v126.py` for the source-contract and SQLite-fixture checks. This does not replace a full Android build or real-device restore test. See `RVH_EDU_CHANGELOG_V126.md`.


## V127 — Schema-v10 migration postflight

The database upgrade now verifies school-local learner-status coverage immediately after the v10 backfill, checks that learners without enrollments retain a legacy-school status, and confirms historical status events reference known schools. If these postflight checks fail, the SQLiteOpenHelper upgrade transaction can roll back rather than opening a partially migrated database. Run `python3 tools/check_migration_recovery_v127.py` for the realistic linked-record SQLite fixture and source-contract checks. This fixture is not a substitute for Android instrumentation or device migration testing.

## V128 — Unenrolled learner lifecycle coverage
V128 extends current-schema backup preflight and the database integrity audit to require at least one school-local lifecycle row for every learner, including learners entered before their first enrollment. This prevents incomplete current backups or later data changes from silently dropping a learner’s only lifecycle status without incorrectly forcing a newly entered learner into `legacy-school`. Enrolled learners remain governed by their exact school/learner status pair. Run `python3 tools/check_unenrolled_lifecycle_coverage_v128.py` for the targeted source/SQLite checks. This is not a substitute for Android compilation, a real schema-v9 upgrade, app-level backup/restore, or physical-device testing. See `RVH_EDU_CHANGELOG_V128.md`.

## V129 — backup round-trip checkpoint

Added a relational SQLite fixture that checks row/ID preservation across the 12 exported tables, school-specific admission numbers and learner states, marks and assessment links, audit history, foreign-key integrity, and rollback after a simulated mid-restore failure. Run with `python3 tools/check_backup_roundtrip_v129.py`. This is a Python SQLite fixture plus source-contract checks, not Android instrumentation; the Android Gradle build and target-device export/restore test remain outstanding. Four stale source-check assertions were also aligned with the current schema and school-local audit signature. See `RVH_EDU_CHANGELOG_V129.md`.

## V130 — Class roster Kotlin compile fix

V130 fixes the GitHub Actions `Unresolved reference: context` error in `ClassRosterRepository`: its constructor now retains `context` as a private property used by the school-scoped roster queries. No data/schema behavior changed. Run `python3 tools/check_class_roster_context_v130.py` for the focused regression check. **Rerun `gradle assembleDebug --no-daemon` in CI to confirm Android compilation; source-level validation alone is not a successful build.** See `RVH_EDU_CHANGELOG_V130.md`.


## V131 — Feedback and online release hooks

V131 adds a Feedback & Updates screen, explicit share-sheet feedback submission, an opt-in anonymous audience check-in, and an update manifest checker. Online audience collection is off by default, excludes school/learner data, and is rate-limited to one successful check-in per installation per seven days. Both remote services require HTTPS endpoints configured in `OnlineServices.kt`; the endpoints are deliberately unset until the project owner chooses and deploys the service. Update checks are quiet and non-blocking, and local/offline school workflows remain available. See `RVH_EDU_CHANGELOG_V131.md`. Full Gradle build, endpoint integration, and device verification remain release gates.
