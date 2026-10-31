# RVH Edu V123 — Strict Current-Backup Table Completeness

## Problem addressed

The V122 compatibility normalizer synthesized several optional table arrays regardless of the declared backup schema. As a result, a current schema-v10 backup missing a teacher-related or school-admission table could be silently normalized with an empty array before the required-table check. That weakened the strict current-backup envelope and could hide incomplete exports.

## Changes

- Teacher, teacher-school membership, and teaching-assignment arrays are synthesized only for backups older than schema v7, when those multi-school tables did not yet exist.
- School-local admission-number arrays are synthesized only for backups older than schema v9.
- Current schema-v10 backups must contain every expected table; missing tables are rejected rather than silently replaced with empty data.
- Checksum verification remains before compatibility normalization, and domain validation remains before the destructive restore transaction.
- Added `tools/check_backup_schema_completeness_v123.py` for repeatable source-contract checks.

## Validation

Run:

```sh
python3 tools/check_backup_schema_completeness_v123.py
python3 tools/check_legacy_backup_compatibility_v122.py
```

## Limitations

These are source-level checks, not Android instrumentation tests. A Gradle build, real schema-v9 upgrade, and backup/restore through the running app remain release gates.
