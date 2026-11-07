#!/usr/bin/env python3
"""Guard school ownership checks for learner history and archive-reason reads (V116)."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
service = ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt'
text = service.read_text(encoding='utf-8')
checks = {
    'history resolves the active school once': 'val schoolId = activeSchoolId()' in text,
    'history loads enrollments for the active school': 'getEnrollmentsForLearner(learnerId, schoolId)' in text,
    'history refuses learners with no active-school enrollment': 'if (enrollments.isEmpty()) return null' in text,
    'history resolves learner with school context': 'learnerRepository.getById(learnerId, schoolId)' in text,
    'archive reasons are filtered by the history school ID': "h.school_id = ?" in text,
    'archive-reason query binds active school': 'arrayOf(schoolId)' in text,
    'archive-reason ordering remains deterministic': "ORDER BY h.changed_at DESC, h.id DESC" in text,
    'legacy lifecycle attribution limitation is documented': 'legacy-school' in text,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items():
    print(f"{'PASS' if ok else 'FAIL'}: {name}")
print(f'\\n{len(checks)-len(failed)}/{len(checks)} checks passed')
raise SystemExit(1 if failed else 0)
