#!/usr/bin/env python3
"""Static regression checks for legacy gradebook migration safety guarantees."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
migration = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/migration/LegacyGradebookMigration.kt").read_text()
repo = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/data/GradebookRepository.kt").read_text()
database = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/database/RvhEduDatabase.kt").read_text()
workflow = (ROOT / ".github/workflows/android-build.yml").read_text()
checks = {
    "migration version advanced for safe retry": "CURRENT_VERSION = 3" in migration,
    "legacy backup precedes parse/import": migration.index("createBackup(legacyFile") < migration.index("GradebookRepository.load(appContext)"),
    "migration logs phase diagnostics": "RVHEduMigration" in migration and "phase=import_progress" in migration,
    "transaction start is inside protected try": migration.index("try {\n            phase = \"database_opened\"") < migration.index("database.beginTransaction()"),
    "transaction ends only if it began": "if (transactionStarted) db?.endTransaction()" in migration,
    "helper is always closed in finally": "helper?.close()" in migration,
    "transaction cleanup failures are not swallowed": "catch (cleanup: Exception)" not in migration and "finally {\n                helper?.close()" in migration,
    "assessment canonical ID used for marks": "val assessmentId = upsertAssessment" in migration,
    "corrupt JSON is not converted to empty list": "throw IllegalStateException(\"Could not read the existing gradebook.json" in repo,
    "gradebook save is atomic-temp based": "$FILE_NAME.tmp" in repo and "temp.renameTo(target)" in repo,
    "SQLite busy timeout uses row-returning API": "rawQuery(\"PRAGMA busy_timeout = 5000\"" in database,
    "CI runs migration safety check": "tools/check_legacy_migration_safety.py" in workflow,
    "release artifact matches current version": "RVHEdu-1.7.4-release.apk" in workflow and "v1.7.4-build160" in workflow,
}
failed = []
for label, passed in checks.items():
    print(f"{'PASS' if passed else 'FAIL'}: {label}")
    if not passed:
        failed.append(label)
if failed:
    print(f"\nFAILED: {len(failed)} migration safety checks")
    sys.exit(1)
print(f"\nPASSED: {len(checks)} migration safety checks")
