#!/usr/bin/env python3
"""Regression checks for shared form components in academic settings screens."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui"
grading = (UI / "SchoolGradingSettingsScreen.kt").read_text()
subjects = (UI / "SchoolSubjectsSettingsScreen.kt").read_text()
shared = (UI / "RvhFormTextField.kt").read_text()
checks = {
    "grading settings use shared fields": "RvhFormTextField(" in grading,
    "school name helper guidance preserved": "Shown on exported reports and class lists." in grading,
    "pass threshold keeps analytics explanation": "Used by class and subject analytics" in grading,
    "aggregate bands use numeric keyboard": "keyboardType = androidx.compose.ui.text.input.KeyboardType.Number" in grading,
    "division label helper preserved": 'helperText = "Label"' in grading,
    "subject settings use shared fields": "RvhFormTextField(" in subjects,
    "subject code remains normalized": "it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' }.take(20)" in subjects,
    "subject short name remains length-limited": "row.shortName = it.take(10)" in subjects,
    "subject permanent identifier warning preserved": "Subject codes are permanent identifiers" in subjects,
    "shared component supports helper text": "helperText: String? = null" in shared,
    "shared component supports keyboard type": "keyboardType: KeyboardType = KeyboardType.Text" in shared,
    "shared component supports validation state": "isError = errorText != null" in shared,
}
for name, passed in checks.items():
    print(f"{'PASS' if passed else 'FAIL'}: {name}")
if not all(checks.values()):
    raise SystemExit(1)
print(f"{sum(checks.values())} settings shared-form checks passed")
