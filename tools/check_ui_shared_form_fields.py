from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ui = ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui"
component = (ui / "RvhFormTextField.kt").read_text()
learner = (ui / "LearnerManagementScreen.kt").read_text()

checks = {
    "shared field component exists": "fun RvhFormTextField(" in component,
    "shared field spans available width": "Modifier.fillMaxWidth()" in component,
    "field errors use semantic error styling": "isError = errorText != null" in component,
    "helper and validation text are supported": "helperText" in component and "errorText" in component,
    "keyboard type and IME action are configurable": "KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction)" in component,
    "minimum touch-friendly field height": "heightIn(min = 56.dp)" in component,
    "age field requests numeric keyboard": "keyboardType = KeyboardType.Number" in learner,
    "add learner uses shared field component": learner.count("RvhFormTextField(") >= 6,
    "save errors use shared feedback banner": "error?.let { RvhFeedbackBanner(message = it, kind = RvhFeedbackKind.Error) }" in learner,
    "archive reason remains required": 'if (reason.isBlank()) error = "Enter a reason to keep the archive record auditable."' in learner,
    "archive note limit remains enforced": "if (it.length <= 500) note = it" in learner,
}
for label, passed in checks.items():
    print(("PASS" if passed else "FAIL") + " " + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f"{sum(checks.values())} passed")
