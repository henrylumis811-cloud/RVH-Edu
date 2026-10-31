from pathlib import Path
root = Path(__file__).resolve().parents[1]
management = (root/'app/src/main/java/com/henrylumis/rvhgrader/data/SchoolManagementRepository.kt').read_text()
info = (root/'app/src/main/java/com/henrylumis/rvhgrader/data/SchoolInfoRepository.kt').read_text()
checks = {
    'school list query': 'SELECT id,name,motto,created_at,updated_at FROM school' in management,
    'stable legacy fallback': 'LEGACY_SCHOOL_ID = "legacy-school"' in management,
    'active selection persisted': 'putString(KEY_ACTIVE_SCHOOL, schoolId)' in management,
    'school creation validates name': 'require(cleanName.isNotEmpty())' in management,
    'school create does not merge data': 'insertOrThrow("school"' in management and 'DELETE FROM' not in management,
    'branding stored in school table': 'update("school", values' in info,
    'pass threshold key scoped by school': 'KEY_CLASS_PASS_PERCENT}_${SchoolManagementRepository.activeId(context)}' in info,
    'old preference fallback preserved': 'getString(KEY_CLASS_PASS_PERCENT, "50")' in info,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'{sum(checks.values())}/{len(checks)} passed')
raise SystemExit(0 if all(checks.values()) else 1)
