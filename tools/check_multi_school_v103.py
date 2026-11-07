from pathlib import Path
p=Path(__file__).resolve().parents[1]
s=(p/'app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt').read_text()
checks={
 'backup schema version advanced':'private const val SCHEMA_VERSION = 7' in s,
 'teacher table backed up':'"teacher"' in s.split('private val tables = listOf(',1)[1].split(')',1)[0],
 'school memberships backed up':'"teacher_school_membership"' in s.split('private val tables = listOf(',1)[1].split(')',1)[0],
 'teaching assignments backed up':'"teaching_assignment"' in s.split('private val tables = listOf(',1)[1].split(')',1)[0],
 'legacy schemas accepted':'backupSchemaVersion in 4..SCHEMA_VERSION' in s,
 'legacy teacher tables initialized empty':'filter { !tableObject.has(it) }' in s,
 'restore deletes assignment children first':'listOf("teaching_assignment", "teacher_school_membership"' in s,
 'restore order includes parent school before school-linked rows':'tables.forEach { table -> restoreTable(db, table, tableObject.getJSONArray(table)) }' in s,
 'restore remains transactional':'db.beginTransaction()' in s and 'db.setTransactionSuccessful()' in s,
}
for k,v in checks.items(): print(('PASS' if v else 'FAIL')+': '+k)
if not all(checks.values()): raise SystemExit(1)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
