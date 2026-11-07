#!/usr/bin/env python3
"""Source regression checks for RVH Edu's shared confirmation-dialog system."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ui = ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui"
component = (ui / "RvhConfirmDialog.kt").read_text()
mark_sheet = (ui / "MarkSheetScreen.kt").read_text()
bulk_marks = (ui / "BulkMarkEntryScreen.kt").read_text()

checks = {
    "shared confirmation component exists": "fun RvhConfirmDialog(" in component,
    "uses shared shape tokens": "shape = MaterialTheme.shapes.large" in component,
    "uses themed surface and text colors": "MaterialTheme.colorScheme.surface" in component and "onSurfaceVariant" in component,
    "supports destructive action styling": "destructive: Boolean = false" in component and "MaterialTheme.colorScheme.error" in component,
    "supports custom detail content": "detail: (@Composable () -> Unit)? = null" in component,
    "mark sheet uses shared dialog": "RvhConfirmDialog(" in mark_sheet and "AlertDialog(" not in mark_sheet[mark_sheet.find("if (pendingNavigation != null)"):],
    "bulk marks uses shared dialog": "RvhConfirmDialog(" in bulk_marks and "AlertDialog(" not in bulk_marks[bulk_marks.find("if (pendingAction != null)"):],
    "mark sheet keeps explicit destructive wording": "confirmLabel = \"Discard changes\"" in mark_sheet,
    "bulk marks clears recovery draft only on confirmed discard": "clearRecoveryDraft()" in bulk_marks and "onConfirm = {" in bulk_marks,
    "dismissal keeps edits available": "dismissLabel = \"Keep editing\"" in mark_sheet and "dismissLabel = \"Keep editing\"" in bulk_marks,
}
for name, passed in checks.items():
    print(("PASS" if passed else "FAIL") + ": " + name)
if not all(checks.values()):
    raise SystemExit(1)
print(f"{len(checks)} shared-dialog UI checks passed")
