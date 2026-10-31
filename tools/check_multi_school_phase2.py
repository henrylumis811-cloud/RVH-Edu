#!/usr/bin/env python3
"""Source-level guardrails for school-scoped enrollment uniqueness migration."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
db = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text()
checks = {
    'database version is 9': 'private const val DB_VERSION = 9' in db,
    'global enrollment uniqueness removed': 'UNIQUE(learner_id, academic_year, school_class)' not in db,
    'school scoped unique index exists': 'idx_enrollment_school_learner_year_class' in db,
    'school foreign key exists': 'FOREIGN KEY(school_id) REFERENCES school(id)' in db,
    'migration preserves enrollment IDs': 'INSERT INTO enrollment (id, school_id' in db,
    'migration preserves assessment policy snapshots': 'policy_snapshot_json FROM assessment_v7_copy' in db,
    'migration preserves mark IDs and values': 'INSERT INTO mark (id, assessment_id' in db,
    'dependent tables dropped child-first': db.indexOf('DROP TABLE mark') < db.indexOf('DROP TABLE assessment') < db.indexOf('DROP TABLE enrollment') if hasattr(db, 'indexOf') else db.find('DROP TABLE mark') < db.find('DROP TABLE assessment') < db.find('DROP TABLE enrollment'),
    'upgrade invokes rebuild': 'rebuildAcademicTablesForSchoolScopedUniqueness(db)' in db,
}
for label, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()): raise SystemExit(1)
print(f'{len(checks)}/{len(checks)} checks passed')
