from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = root / 'app/src/main/java/com/henrylumis/rvhgrader'
checks = {
    'school management screen exists': (main/'ui/SchoolManagementScreen.kt').exists(),
    'school selector lists records': 'SchoolManagementRepository.list(context)' in (main/'ui/SchoolManagementScreen.kt').read_text(),
    'school creation action exists': 'SchoolManagementRepository.create(context, name, motto)' in (main/'ui/SchoolManagementScreen.kt').read_text(),
    'active selection uses repository': 'SchoolManagementRepository.select(context, school.id)' in (main/'ui/SchoolManagementScreen.kt').read_text(),
    'settings hub links schools': 'onSchools' in (main/'ui/SettingsHubScreen.kt').read_text(),
    'navigation enum includes schools': 'SCHOOLS(' in (main/'ui/Screen.kt').read_text(),
    'main activity routes schools': 'Screen.SCHOOLS -> SchoolManagementScreen' in (main/'MainActivity.kt').read_text(),
    'school change invalidates cached results': 'classResults = emptyList()' in (main/'MainActivity.kt').read_text(),
}
for label, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' ' + label)
print(f'{sum(checks.values())}/{len(checks)} passed')
raise SystemExit(0 if all(checks.values()) else 1)
