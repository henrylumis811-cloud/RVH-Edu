#!/usr/bin/env python3
"""Source-level guard for RVH Edu multi-school assessment boundaries (V112)."""
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[1]
service = ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt'
repo = ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt'
service_text = service.read_text(encoding='utf-8')
repo_text = repo.read_text(encoding='utf-8')
checks = {
    'service resolves active school context': 'private fun activeSchoolId()' in service_text,
    'assessment reads are school scoped': 'getAssessmentForSchool(' in service_text and 'getAssessmentsForEnrollmentInSchool(' in service_text,
    'mark reads are school scoped': 'getMarksForSchool(' in service_text,
    'draft writes are school scoped': 'replaceDraftMarksForSchool(' in service_text,
    'finalization is school scoped': 'replaceMarksAndFinalizeForSchool(' in service_text,
    'status updates are school scoped': 'setAssessmentStatusForSchool(' in service_text,
    'repository checks enrollment ownership': 'requireEnrollmentBelongsToSchool(' in repo_text,
    'repository checks assessment ownership': 'requireAssessmentBelongsToSchool(' in repo_text,
    'scoped writes use database transactions': repo_text.count('database.beginTransaction()') >= 1 and 'database.setTransactionSuccessful()' in repo_text,
    'school-scoped mark query joins enrollment': re.search(r'JOIN\s+enrollment\s+e', repo_text, re.I) is not None,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items():
    print(f"{'PASS' if ok else 'FAIL'}: {name}")
print(f'\n{len(checks)-len(failed)}/{len(checks)} checks passed')
raise SystemExit(1 if failed else 0)
