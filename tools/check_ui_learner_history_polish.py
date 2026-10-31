from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
profile = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/LearnerProfileScreen.kt").read_text()
history = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/AcademicHistoryScreen.kt").read_text()
checks = {
    "profile identity header": '"Learner academic record"' in profile and "RoundedCornerShape(24.dp)" in profile,
    "profile overview metrics": '"Latest average"' in profile and '"Previous average"' in profile,
    "subject insights": '"Strongest subject"' in profile and '"Needs support"' in profile,
    "enrolment history preserved": "history.enrollments" in profile and "assessment.snapshot.result" in profile,
    "incomplete assessment not shown as ranked percentage": 'if (complete) "%.1f%%".format(result.average) else "—"' in profile,
    "missing subjects remain visible": 'result.missingSubjects.joinToString' in profile,
    "history search persists through recomposition": "rememberSaveable" in history and "mutableStateOf(\"\")" in history,
    "search covers class and academic year": "enrollment.academicYear.contains" in history and "enrollment.schoolClassName.contains" in history,
    "learner open callback preserved": "onOpenLearner(history.learnerId)" in history,
    "empty and no-match states": '"No matching learners"' in history and '"No academic history yet"' in history,
    "history summaries": '"Learners"' in history and '"With history"' in history,
    "long history list remains lazy": "LazyColumn(" in history and "items(filtered, key = { it.learnerId })" in history,
}
failed = [name for name, passed in checks.items() if not passed]
for name, passed in checks.items():
    print(("PASS" if passed else "FAIL") + " - " + name)
print(f"{len(checks) - len(failed)}/{len(checks)} checks passed")
if failed:
    raise SystemExit(1)
