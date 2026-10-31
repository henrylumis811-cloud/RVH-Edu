from pathlib import Path
root = Path(__file__).resolve().parents[1]
service = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
roster = (root/'app/src/main/java/com/henrylumis/rvhgrader/core/repository/ClassRosterRepository.kt').read_text()
checks = {
    'active school resolved from repository': 'SchoolManagementRepository.activeId(appContext)' in service,
    'enrollment creation passes active school': 'schoolId = activeSchoolId()' in service,
    'progression passes active school': 'schoolId = activeSchoolId()' in service and 'sourceClass = sourceClass' in service,
    'class results use scoped enrollment query': 'getEnrollments(year, schoolClass, activeSchoolId())' in service,
    'roster repository uses selected school': 'SchoolManagementRepository.activeId(context)' in roster,
    'legacy compatibility documented': 'legacy-school' in (root/'RVH_EDU_MULTI_SCHOOL_V102.md').read_text(),
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()): raise SystemExit(1)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
