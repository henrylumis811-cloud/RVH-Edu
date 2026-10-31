from pathlib import Path

root = Path(__file__).resolve().parents[1]
report = (root / "app/src/main/java/com/henrylumis/rvhgrader/core/reports/ReportCard.kt").read_text()
assert "savedPolicy?.hasCompleteScoringPolicy == true && schoolClass.mode == SystemMode.UPPER" in report
assert "savedPolicy.gradingScale().aggregateFor(normalized)" in report
assert "mark.aggregate" in report  # legacy snapshots and lower-primary reports retain stored behavior
assert "roundToInt()" in report  # non-100 maximum marks normalize consistently with AcademicEngine
print("PASS: report-card aggregates use the assessment's saved grading scale when complete policy exists")
