#!/usr/bin/env python3
"""Focused regression checks for V86 PDF layout boundary safeguards."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
exports = (root / "app/src/main/java/com/henrylumis/rvhgrader/export/AcademicExportManager.kt").read_text()
checks = {
    "subject widths are based on actual drawn result columns": "val learnerEndX = left + 8f + 30f + (learnerWidth - 30f)" in exports and "val resultColumnsWidth = 47f + 42f + if (isUpper) 60f else 0f" in exports,
    "subject width has no hard minimum that can force overflow": "coerceAtMost(54f)" in exports and ".coerceIn(12f, 54f)" not in exports and ".coerceIn(28f, 54f)" not in exports,
    "incomplete rows reserve extra vertical space": "requiredRowSpace = if (row.complete) 32f else 42f" in exports,
    "pagination check uses reserved row space": "if (y > pageHeight - requiredRowSpace)" in exports,
    "page break finishes current page before opening next": "document.finishPage(page); pageNumber++" in exports,
    "header is redrawn on new page": "canvas = page.canvas; header()" in exports,
}
for name, ok in checks.items():
    print(("PASS" if ok else "FAIL") + " " + name)
print(f"{sum(checks.values())}/{len(checks)} checks passed")
raise SystemExit(0 if all(checks.values()) else 1)
