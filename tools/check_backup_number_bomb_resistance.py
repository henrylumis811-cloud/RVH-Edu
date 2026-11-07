"""Regression guards for bounded canonicalization of extreme JSON numbers."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
helper = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/BackupChecksumPrimitives.kt").read_text()
tests = (ROOT / "tools/pure-tests/BackupChecksumPrimitivesRegression.kt").read_text()
assert "normalized.toString()" in helper
assert "normalized.toPlainString()" not in helper
assert "Non-finite or invalid number in backup checksum." in helper
assert 'BigDecimal("1e100000000")' in tests
assert "Double.NaN" in tests and "Double.POSITIVE_INFINITY" in tests
assert "sha256(BackupChecksumPrimitives.canonicalJson(reorderedTree))" in tests
assert "alteredTree" in tests
print("Backup extreme-number canonicalization regression checks passed (7 assertions).")
