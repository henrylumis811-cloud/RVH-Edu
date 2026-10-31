from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt').read_text()
svc = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
checks = {
    'ordinary upsert seeds missing status without overwriting existing status': 'INSERT OR IGNORE INTO school_learner_status' in repo,
    'ordinary upsert does not use conflict update to replace lifecycle status': 'ON CONFLICT(school_id, learner_id) DO UPDATE SET status' not in repo,
    'explicit school-scoped lifecycle setter exists': 'fun setStatusInSchool(learnerId: String, schoolId: String, status: String' in repo,
    'lifecycle setter validates supported states': 'require(cleanStatus == "ACTIVE" || cleanStatus == "ARCHIVED")' in repo,
    'lifecycle setter targets the compound school and learner key': '"school_id = ? AND learner_id = ?"' in repo,
    'archive uses explicit school-local status setter': 'setStatusInSchool(learnerId, activeSchoolId(), "ARCHIVED", now)' in svc,
    'restore uses explicit school-local status setter': 'setStatusInSchool(learnerId, activeSchoolId(), "ACTIVE", now)' in svc,
    'archive event remains in same transaction as status change': svc.index('fun archiveLearner(') < svc.index('db.beginTransaction()', svc.index('fun archiveLearner(')) < svc.index('setStatusInSchool(learnerId, activeSchoolId(), "ARCHIVED", now)', svc.index('fun archiveLearner(')),
    'restore event remains in same transaction as status change': svc.index('fun restoreLearner(') < svc.index('db.beginTransaction()', svc.index('fun restoreLearner(')) < svc.index('setStatusInSchool(learnerId, activeSchoolId(), "ACTIVE", now)', svc.index('fun restoreLearner(')),
    'archive and restore do not upsert lifecycle status through profile path': 'upsert(learner.copy(status = "ARCHIVED"' not in svc and 'upsert(learner.copy(status = "ACTIVE"' not in svc,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' - ' + name)
assert all(checks.values()), 'V118 checks failed'
print(f'V118: {len(checks)}/{len(checks)} checks passed')
