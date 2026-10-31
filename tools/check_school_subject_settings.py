from pathlib import Path
root = Path(__file__).resolve().parents[1]
catalog = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/grading/SubjectCatalog.kt').read_text()
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/grading/SubjectCatalogRepository.kt').read_text()
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/SchoolSubjectsSettingsScreen.kt').read_text()
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
hub = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/SettingsHubScreen.kt').read_text()
assert 'fun configure(lowerSubjects: List<Subject>, upperSubjects: List<Subject>)' in catalog
assert 'fun loadAndApply(context: Context)' in repo and '.commit()' in repo
assert 'SAVE SUBJECTS' in screen and 'Before changing subjects' in screen
assert 'Screen.SCHOOL_SUBJECTS -> SchoolSubjectsSettingsScreen' in main
assert 'onSchoolSubjects' in hub
assert 'SubjectCatalogRepository.loadAndApply(context)' in main
print('PASS: school subject settings, persistence, navigation and startup loading are wired')
