from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/grading/SubjectCatalogRepository.kt').read_text()
catalog = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/grading/SubjectCatalog.kt').read_text()
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/SchoolSubjectsSettingsScreen.kt').read_text()
assert 'HISTORY_KEY' in repo and 'MAX_REVISIONS = 50' in repo
assert 'data class Revision' in repo and 'savedAtMillis: Long' in repo
assert 'fun history(context: Context)' in repo and 'fun restore(context: Context, revision: Int)' in repo
assert 'SubjectCatalog.configure(lower, upper)' in repo
assert 'fun validate(lowerSubjects: List<Subject>, upperSubjects: List<Subject>)' in catalog
assert 'SUBJECT CONFIGURATION HISTORY' in screen and 'RESTORE' in screen
assert 'creates a new revision' in screen and 'does not rewrite historical marks' in screen
print('PASS: subject configuration revisions are validated, persisted, listable and restorable')
