from pathlib import Path
root = Path(__file__).resolve().parents[1]
recovery = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/PinRecovery.kt').read_text()
gate = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/PinGate.kt').read_text()
gradle = (root / 'app/build.gradle.kts').read_text()
checks = {
    'active request restored from persistent preferences': 'fun loadActiveRequest(context: Context): Request?' in recovery and 'getString(ACTIVE_REQUEST, null)' in recovery,
    'restored request retains original encoded payload': 'return Request(encoded, fields!![3].toLong())' in recovery,
    'expired request is cleared without touching PIN keys': 'remove(ACTIVE_REQUEST)' in recovery and 'remove(KEY_PIN)' not in recovery,
    'request remains installation-bound': 'fields!![1] != installId' in recovery,
    'forgot PIN reloads persisted request': 'recoveryRequest = PinRecovery.loadActiveRequest(context)?.encoded' in gate,
    'UI explains request survives app closure': 'you can close RVH Edu and return later' in gate,
    'replacement remains explicit': 'Generate a replacement request' in gate,
    'seven-day validity retained': 'private const val VALIDITY_SECONDS = 604800L' in recovery,
    'single-use consume retained': 'putString(USED_REQUEST, request).remove(ACTIVE_REQUEST)' in recovery,
    'teacher app version incremented': 'versionCode = 160' in gradle and 'versionName = "1.7.4"' in gradle,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ' - ' + name)
if not all(checks.values()):
    raise SystemExit(1)
print(f'\n{len(checks)}/{len(checks)} checks passed')
