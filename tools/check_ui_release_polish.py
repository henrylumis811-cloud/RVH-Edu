#!/usr/bin/env python3
"""Final UI release-polish source checks for shared interaction components."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui"
dialog = (UI / "RvhConfirmDialog.kt").read_text()
field = (UI / "RvhFormTextField.kt").read_text()
feedback = (UI / "RvhFeedbackBanner.kt").read_text()
checks = {
    "confirmation dialog uses shared theme shape": "shape = MaterialTheme.shapes.large" in dialog,
    "confirmation dialog uses semantic theme surface": "containerColor = MaterialTheme.colorScheme.surface" in dialog,
    "destructive confirmation uses error color": "containerColor = MaterialTheme.colorScheme.error" in dialog,
    "dismiss action avoids forced fractional width": "fillMaxWidth(fraction = 0.44f)" not in dialog,
    "dialog keeps explicit confirm and dismiss actions": "confirmButton =" in dialog and "dismissButton =" in dialog,
    "form fields expose validation state": "isError = errorText != null" in field,
    "form fields expose keyboard configuration": "KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction)" in field,
    "feedback supports success error and info": "enum class RvhFeedbackKind { Success, Error, Info }" in feedback,
    "feedback announces updates accessibly": "liveRegion = LiveRegionMode.Polite" in feedback,
}
for name, ok in checks.items():
    print(f"{'PASS' if ok else 'FAIL'}: {name}")
print(f"{sum(checks.values())}/{len(checks)} checks passed")
if not all(checks.values()):
    raise SystemExit(1)
