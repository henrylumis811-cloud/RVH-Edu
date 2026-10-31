from pathlib import Path
root = Path(__file__).resolve().parents[1]
repo = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
method = repo[repo.index('fun upsertEnrollment('):repo.index('\n    fun getEnrollment(')]
enroll = service[service.index('fun enroll('):service.index('\n    fun findAssessment(')]
checks = {
    'enrollment upsert is transactional': 'database.beginTransaction()' in method and 'database.setTransactionSuccessful()' in method and 'database.endTransaction()' in method,
    'existing enrollment preserves original id': 'return enrollment.copy(id = existing.id' in method,
    'repeated enrollment preserves original timestamp': 'enrolledAtMillis = existing.enrolledAt' in method,
    'conflicting stream change is rejected': 'requestedStream == null || storedStream == null || requestedStream.equals(storedStream, ignoreCase = true)' in method,
    'missing stream may be filled without replacing enrollment': 'if (storedStream == null && requestedStream != null)' in method,
    'archived learners cannot be enrolled': 'learner.status == "ACTIVE"' in enroll and 'Archived learners cannot be enrolled' in enroll,
    'academic year remains validated': 'academicYear in 2000..2100' in enroll,
    'stream whitespace is normalized': 'replace(Regex("\\\\s+"), " ")' in enroll,
    'school-scoped unique index replaces global constraint': 'idx_enrollment_school_learner_year_class' in (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text() and 'UNIQUE(learner_id, academic_year, school_class)' not in (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text(),
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if failed: raise SystemExit(1)
print('ENROLLMENT_INTEGRITY_OK')
