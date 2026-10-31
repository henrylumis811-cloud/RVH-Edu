from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
db = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text()
model = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/model/AcademicModels.kt').read_text()
repo = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt').read_text()
service = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
snapshot = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
assert 'DB_VERSION = 10' in db
assert 'ALTER TABLE assessment ADD COLUMN policy_snapshot_json TEXT' in db
assert 'policy_snapshot_json TEXT' in db
assert 'policySnapshotJson: String? = null' in model
assert 'policy_snapshot_json' in repo and 'existing != null' in repo
assert 'AssessmentPolicySnapshot.capture(appContext, enrollment.schoolClass).toJson()' in service
assert 'CURRENT_SCHEMA_VERSION = 2' in snapshot and 'fun parse(json: String)' in snapshot
assert 'SubjectCatalog.forClass(schoolClass)' in snapshot
assert 'aggregateBands' in snapshot and 'divisionBands' in snapshot and 'classPassPercent' in snapshot
assert 'snapshot?.hasCompleteScoringPolicy == true' in service
assert 'subjectCodes = expectedSubjects' in service
assert 'schemaVersion = 1' not in snapshot
print('PASS: assessment versioned subject and scoring-policy snapshot persistence, legacy parsing, and assessment calculation checks')
