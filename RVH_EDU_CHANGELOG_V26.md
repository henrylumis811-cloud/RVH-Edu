# RVH Edu V26 — Bulk Marks Draft Recovery

## Purpose

Protect teacher-entered marks from being lost if Android destroys the app process or the app is reopened before the teacher presses Save.

## Changes

- Reused the existing `BulkMarkEntryScreen` and its existing changed/saved comparison.
- Added small device-local recovery snapshots in Android SharedPreferences, scoped by academic year, class, term, assessment section, and subject.
- Saves the current entry snapshot as the teacher edits marks, including intentionally cleared cells.
- Restores saved-on-device draft input when the same bulk-entry context is reopened, applying values only to enrollment IDs in the current roster.
- Shows a clear message that recovered marks need review and a deliberate Save action.
- Clears the recovery snapshot after a fully successful save or when the teacher explicitly discards changes and leaves/switches context.
- Retains failed/partially saved edits for recovery after a partial save failure.
- Removes malformed recovery snapshots rather than letting them block marks entry.

## Data safety notes

- Recovery data is local to the app installation; it is not a cloud backup and does not replace the existing database backup feature.
- Recovered values are not silently submitted to the academic database. Teachers must review and press Save.
- This is a UI-level recovery aid; the existing database/service validation remains authoritative.

## Verification status

- Recovery source checks: passed.
- Existing source-level integrity scripts: to be run as part of this handoff.
- ZIP integrity: to be checked as part of this handoff.
- Full Android build and device/process-death test: not performed in this environment.
