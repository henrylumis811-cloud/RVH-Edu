from pathlib import Path

root = Path(__file__).resolve().parents[1]
model = (root / "app/src/main/java/com/henrylumis/rvhgrader/model/GradingScale.kt").read_text()
settings = (root / "app/src/main/java/com/henrylumis/rvhgrader/ui/SchoolGradingSettingsScreen.kt").read_text()
readme = (root / "README.md").read_text()
assert "fun validationError()" in model
for phrase in ["Each mark threshold must be unique", "Add a 0 minimum-mark band", "Division limits must be greater than zero"]:
    assert phrase in model, phrase
assert "candidate.validationError()" in settings
assert "Complete or remove every unfinished row before saving." in settings
assert "No OCR screen" in readme
assert "Some academic rules remain fixed" in readme
print("PASS: school grading-table validation and honest OCR/configurability documentation")
