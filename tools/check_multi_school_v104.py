from pathlib import Path
root=Path(__file__).resolve().parents[1]
repo=(root/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt').read_text()
roster=(root/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/ClassRosterRepository.kt').read_text()
schools=(root/'app/src/main/java/com/henrylumis/rvhgrader/data/SchoolManagementRepository.kt').read_text()
backup=(root/'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
checks={
 'enrollment writes require a nonblank school id':'require(schoolId.isNotBlank())' in repo,
 'enrollment idempotency is scoped by school':'"school_id = ? AND learner_id = ? AND academic_year = ? AND school_class = ?"' in repo,
 'enrollment reads scoped by school':'"school_id = ? AND academic_year = ? AND school_class = ?"' in repo,
 'learner enrollment history scoped by school':'"learner_id = ? AND school_id = ?"' in repo,
 'roster passes active school id':'SchoolManagementRepository.activeId(context)' in roster,
 'active school selection validates persisted school':'require(list(context).any { it.id == schoolId })' in schools,
 'school creation selects created school':'select(context, school.id)' in schools,
 'backup contains teacher membership and assignments':'"teacher_school_membership"' in backup and '"teaching_assignment"' in backup,
 'backup restore transaction retained':'db.beginTransaction()' in backup and 'db.setTransactionSuccessful()' in backup,
}
for label,ok in checks.items(): print(('PASS' if ok else 'FAIL')+': '+label)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
if not all(checks.values()): raise SystemExit(1)
