from pathlib import Path

root = Path(__file__).resolve().parents[1]
learner = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/LearnerManagementScreen.kt').read_text()
settings = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/SettingsHubScreen.kt').read_text()
checks = {
    'learner screen has a clear directory title and context': 'STUDENT DIRECTORY' in learner and 'Class roster' in learner,
    'class and academic year remain selectable': 'onClassChange' in learner and 'onYearChange' in learner,
    'learner search covers names and admission numbers': 'fullName.contains(search, ignoreCase = true)' in learner and 'admissionNumber?.contains(search, ignoreCase = true)' in learner,
    'empty roster provides a clear add action': 'Your class starts here' in learner and 'Add first learner' in learner,
    'empty search provides recovery action': 'No matching learners' in learner and 'Clear search' in learner,
    'learner cards retain profile edit archive and mark actions': all(x in learner for x in ['onProfile()', 'onEdit()', 'onArchive()', 'onMarks']) and all(x in learner for x in ['onProfile =', 'onEdit =', 'onArchive =', 'onMarks =']),
    'bulk mark action remains wired': 'onOpenBulkMarks' in learner and 'Bulk marks' in learner,
    'settings grouped into configuration and safety': 'SCHOOL CONFIGURATION' in settings and 'RECORDS & SAFETY' in settings,
    'settings navigation callbacks retained': all(x in settings for x in ['onSchoolAndGrading', 'onBackup', 'onArchivedLearners', 'onClassSetup', 'onSchoolSubjects']),
    'settings cards have semantic icon surfaces and chevrons': 'secondaryContainer' in settings and 'Icons.Filled.ChevronRight' in settings,
    'no OCR flow introduced': 'OCR' not in learner and 'OCR' not in settings,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)} learner/settings UI checks passed.')
