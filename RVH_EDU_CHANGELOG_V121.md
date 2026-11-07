# RVH Edu V121 — Migration and Backup Round-Trip Regression Fixture

## Scope

V121 adds a focused regression harness for the school-local learner lifecycle migration and backup-shaped status-row round trip. It combines source-contract assertions against the current Kotlin database/backup implementation with behavioral exercises against Python's real SQLite engine.

## Covered scenarios

- Confirms the current installed database schema remains version 10; this checkpoint does not introduce another database migration.
- Checks that the existing additive migration creates the composite `(school_id, learner_id)` primary key and backfills each enrolled school from legacy learner status.
- Checks the compatibility path for learners without enrollments through `legacy-school`.
- Exercises a fixture where one learner is enrolled in two schools, verifying each receives a distinct lifecycle row.
- Exercises a school-local status update and verifies the other school's status remains unchanged.
- Verifies enrollment, assessment, mark, and lifecycle history fixture rows remain present through the backfill.
- Exercises a backup-shaped status-row round trip without collapsing school identity.
- Checks the source still exports the lifecycle table, backfills missing rows after restore, and runs integrity validation before transaction commit.

## Run

```bash
python3 tools/check_migration_roundtrip_v121.py
```

## Limitations

The SQLite fixture tests the migration's SQL behavior in an isolated test database, but it does not invoke Android's `SQLiteOpenHelper`, execute the app's Kotlin restore code, or create a real backup file through the Android content resolver. Android Gradle compilation, migration from an actual schema-v9 database, a real backup/restore round trip, and Tecno Spark 30C tests remain release gates.
