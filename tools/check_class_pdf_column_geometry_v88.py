#!/usr/bin/env python3
"""Regression checks for class PDF column geometry and page-bound safety."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
source = (root / "app/src/main/java/com/henrylumis/rvhgrader/export/AcademicExportManager.kt").read_text()
checks = {
    "PDF width calculation accounts for the learner column": "val learnerEndX = left + 8f + 30f + (learnerWidth - 30f)" in source,
    "PDF width calculation accounts for aggregate and division columns": "val resultColumnsWidth = 47f + 42f + if (isUpper) 60f else 0f" in source,
    "subject labels are clipped to their calculated column width": "clipped(label, headerText, actualSubjectWidth - 4f)" in source,
    "subject width does not impose an overflow-causing minimum": "coerceAtMost(54f)" in source and "coerceIn(12f, 54f)" not in source,
    "incomplete rows keep their missing-subject disclosure": '"Incomplete: ${row.missingSubjects.joinToString' in source,
}
# Mirror the width geometry with realistic and stress subject counts. The last column
# must stay at or before the right margin for both lower- and upper-school layouts.
left, right, learner_width = 28.0, 814.0, 145.0
learner_end = left + 8.0 + 30.0 + (learner_width - 30.0)
for is_upper in (False, True):
    result_width = 47.0 + 42.0 + (60.0 if is_upper else 0.0)
    space = max(right - learner_end - result_width - 8.0, 1.0)
    for subject_count in (1, 5, 12, 20, 40, 80):
        col = min(space / max(subject_count, 1), 54.0)
        end = learner_end + col * subject_count + result_width
        checks[f"{subject_count} subjects fit the page ({'upper' if is_upper else 'lower'})"] = end <= right + 1e-6
for label, ok in checks.items():
    print(("PASS" if ok else "FAIL") + " " + label)
print(f"{sum(checks.values())}/{len(checks)} checks passed")
raise SystemExit(0 if all(checks.values()) else 1)
