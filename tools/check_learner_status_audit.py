#!/usr/bin/env python3
"""Source-level regression checks for auditable learner archiving/restoration."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
DB = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text()
SERVICE = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
MANAGEMENT = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/LearnerManagementScreen.kt').read_text()
ARCHIVED = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/ArchivedLearnersScreen.kt').read_text()
MAIN = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
assert 'DB_VERSION = 10' in DB
assert 'if (oldVersion < 6)' in DB and 'createLearnerStatusHistoryTable(db)' in DB
assert 'CREATE TABLE IF NOT EXISTS learner_status_history' in DB
assert 'FOREIGN KEY(learner_id) REFERENCES learner(id)' in DB
assert 'fun archiveLearner(learnerId: String, reason: String' in SERVICE
assert 'cleanReason.isNotEmpty()' in SERVICE and 'cleanReason.length <= 120' in SERVICE
assert 'insertLearnerStatusEvent(db, learnerId, activeSchoolId(), "ACTIVE", "ARCHIVED"' in SERVICE
assert 'insertLearnerStatusEvent(db, learnerId, activeSchoolId(), "ARCHIVED", "ACTIVE"' in SERVICE
assert 'db.beginTransaction()' in SERVICE[SERVICE.index('fun archiveLearner'):SERVICE.index('fun learnerHistory')]
assert 'latestArchiveReasons()' in SERVICE and 'ORDER BY h.changed_at DESC, h.id DESC' in SERVICE and 'h.school_id = ?' in SERVICE and 'if (!containsKey(learnerId))' in SERVICE
assert 'Reason (required)' in MANAGEMENT and 'Note (optional)' in MANAGEMENT
assert 'onArchiveLearner: (RosterEntry, String, String?) -> Unit' in MANAGEMENT
assert 'Archive reason: $reason' in ARCHIVED
assert 'academicService.archiveLearner(entry.learner.id, reason, note)' in MAIN
print('Learner status audit checks passed')
