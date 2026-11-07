# Legacy Gradebook Recovery

The legacy `gradebook.json` is private application data. The migration code must never delete or rewrite it.

## Migration behaviour

- Before reading a present legacy JSON file, migration copies its bytes into `filesDir/migration-backups/` under a unique `gradebook-pre-migration-v3*.json` name and verifies the copied length.
- A missing file or a successfully parsed empty array is a valid no-op. A present but unreadable/malformed file is an error, not an empty gradebook; the migration completion marker must not advance.
- Import is transactional. A failed import rolls back and leaves the migration marker unchanged so it can be retried.
- Logs use the `RVHEduMigration` tag and phase names. They should contain counts/timings, not learner names, marks, or other sensitive school records.
- On a unique assessment collision, marks must use the canonical assessment ID that actually exists.

## If startup reports a migration error

1. Do not uninstall the app, clear app storage, or delete `gradebook.json`.
2. Save a private bug report and capture logs filtered by `RVHEduMigration` and `SQLite`.
3. Preserve the original project ZIP and the generated release artifact used on the device.
4. Fix the specific phase/exception, run migration-safety and backup/restore regression checks, then run Android tests/build.
5. Test with a copy of representative data before testing against the only live school dataset.

A successful static check cannot guarantee zero crashes. Release readiness requires a successful CI build and on-device verification with the relevant dataset.
