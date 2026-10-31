# RVH Edu V69 — backup restore preview

- Added a read-only backup preview before the restore confirmation dialog.
- Preview shows backup timestamp, format/schema versions, learner/enrolment/assessment/mark counts, and whether a version-2 checksum was verified.
- Preview rejects unsupported formats, malformed headers, missing table arrays, and checksum failures before asking the teacher to confirm.
- Preview does not open the academic database or mutate records. Full domain and installed-schema validation still runs in the restore path before the destructive transaction.
- Added `tools/check_backup_restore_preview.py` to guard the preview's read-only boundary, checksum check, displayed summary, and retained full restore validation.

## Validation

- Source regression scripts and pure-JVM checksum helper regression are run during packaging.
- ZIP integrity and archive-root layout are checked during packaging.
- Full Android Gradle build and physical-device backup/restore testing are not claimed by this release.
