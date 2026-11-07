#!/usr/bin/env python3
"""Run the maintained backup/restore regression checks used by CI.

These checks combine source-contract assertions with host-side SQLite fixtures. They are
not a substitute for Android instrumentation tests, but provide broader repeatable regression
coverage for backup compatibility, checksum handling, schema/domain validation, round-trips,
learner lifecycle records, and transaction rollback on every CI run.
"""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
CHECKS = [
    "tools/check_backup_restore_transaction_safety.py",
    "tools/check_restore_atomicity_v168.py",
    "tools/check_backup_roundtrip_v129.py",
    "tools/check_backup_semantic_preflight.py",
    "tools/check_backup_size_and_safe_write.py",
    "tools/check_backup_row_shape.py",
    "tools/check_backup_schema_completeness_v123.py",
    "tools/check_backup_checksum_versioning.py",
    "tools/check_backup_canonical_json_runtime_coverage.py",
    "tools/check_backup_includes_learner_status_history.py",
    "tools/check_backup_integrity_uses_historical_policy.py",
    "tools/check_backup_lifecycle_coverage_v124.py",
    "tools/check_backup_lifecycle_timestamps_v126.py",
    "tools/check_backup_number_bomb_resistance.py",
    "tools/check_backup_recovery_v119.py",
    "tools/check_backup_recovery_v120.py",
    "tools/check_backup_restore_preview.py",
    "tools/check_backup_unique_relationships.py",
    "tools/check_learner_archive_restore_atomicity.py",
]
failed = []
for relative in CHECKS:
    print(f"\n=== {relative} ===", flush=True)
    result = subprocess.run(
        [sys.executable, str(ROOT / relative)],
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=45,
    )
    print(result.stdout, end="" if result.stdout.endswith("\n") else "\n")
    if result.returncode != 0:
        failed.append((relative, result.returncode))

if failed:
    print("\nBACKUP REGRESSION SUITE FAILED:")
    for path, code in failed:
        print(f" - {path} (exit {code})")
    sys.exit(1)

print(f"\nBACKUP REGRESSION SUITE PASSED: {len(CHECKS)}/{len(CHECKS)} checks")
