from pathlib import Path

root = Path(__file__).resolve().parents[1]
db = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt').read_text()
gradle = (root / 'app/build.gradle.kts').read_text()
workflow = (root / '.github/workflows/android-build.yml').read_text()
checks = {
    'WAL enabled on shared helper': 'setWriteAheadLoggingEnabled(true)' in db,
    'SQLite busy timeout configured': 'PRAGMA busy_timeout = 5000' in db,
    'foreign-key enforcement retained': 'setForeignKeyConstraintsEnabled(true)' in db,
    'schema version unchanged to avoid destructive migration': 'private const val DB_VERSION = 11' in db,
    'release version bumped': 'versionName = "1.7.4"' in gradle and 'versionCode = 160' in gradle,
    'release artifact filename updated': 'RVHEdu-1.7.4-release.apk' in workflow,
    'release artifact label updated': 'rvh-edu-and-recovery-admin-v1.7.4-build160' in workflow,
}
for label, passed in checks.items():
    print(('PASS' if passed else 'FAIL') + ' - ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'SQLITE_BUSY_V180_OK ({len(checks)} checks)')
