from pathlib import Path
root = Path(__file__).resolve().parents[1]
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
archive = service[service.index('fun archiveLearner('):service.index('/** Restore a previously archived learner')]
restore = service[service.index('fun restoreLearner('):service.index('fun learnerHistory(')]
checks = {
    'archive operation uses transaction': 'db.beginTransaction()' in archive and 'db.setTransactionSuccessful()' in archive and 'db.endTransaction()' in archive,
    'restore operation uses transaction': 'db.beginTransaction()' in restore and 'db.setTransactionSuccessful()' in restore and 'db.endTransaction()' in restore,
    'archive validates known statuses': 'learner.status == "ACTIVE" || learner.status == "ARCHIVED"' in archive,
    'restore validates known statuses': 'learner.status == "ACTIVE" || learner.status == "ARCHIVED"' in restore,
    'archive is idempotent': 'if (learner.status == "ACTIVE")' in archive,
    'restore is idempotent': 'if (learner.status == "ARCHIVED")' in restore,
    'historical data is not deleted': 'delete(' not in archive and 'delete(' not in restore,
    'archive audit is atomic with status change': 'insertLearnerStatusEvent(db, learnerId, activeSchoolId(), "ACTIVE", "ARCHIVED"' in archive,
    'restore audit is atomic with status change': 'insertLearnerStatusEvent(db, learnerId, activeSchoolId(), "ARCHIVED", "ACTIVE"' in restore,
    'updated timestamp changes only when state changes': 'val now = System.currentTimeMillis()' in archive and 'val now = System.currentTimeMillis()' in restore,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if failed: raise SystemExit(1)
print('LEARNER_ARCHIVE_RESTORE_ATOMICITY_OK')
