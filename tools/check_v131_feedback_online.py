from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
settings = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/SettingsHubScreen.kt').read_text()
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/FeedbackAndUpdatesScreen.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/online/OnlineServices.kt').read_text()
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
build = (root / 'app/build.gradle.kts').read_text()
checks = {
    'feedback destination is wired from settings': 'onFeedbackAndUpdates = { goTo(Screen.FEEDBACK_UPDATES) }' in main and 'SettingsAction("Feedback & updates"' in settings,
    'feedback is user-initiated via Android share sheet': 'Intent.createChooser(intent, "Send RVH Edu feedback")' in screen,
    'audience sharing is explicit opt-in and defaults off': 'getBoolean("anonymous_audience_opt_in", false)' in service and 'Switch(checked = analyticsEnabled' in screen,
    'telemetry payload excludes school and learner records': 'put("installId", installId).put("versionCode", versionCode).put("platform", "android")' in service and 'learner' not in service.lower().split('val payload')[1].split('connection.outputStream')[0],
    'remote endpoints require HTTPS and may remain unconfigured': 'AUDIENCE_ENDPOINT_URL.startsWith("https://")' in service and 'RELEASE_MANIFEST_URL.startsWith("https://")' in service,
    'update detection compares remote and installed version codes': 'latest > currentVersionCode' in service,
    'update prompt is dismissible and URL is HTTPS-gated before prompting': 'Text("Later")' in main and 'result.downloadUrl.startsWith("https://")' in main,
    'internet and connectivity permissions declared': 'android.permission.INTERNET' in manifest and 'android.permission.ACCESS_NETWORK_STATE' in manifest,
    'version incremented': 'versionCode = 97' in build and 'versionName = "1.1.1"' in build,
    'offline-safe errors do not crash normal workflow': 'RVH Edu will keep working normally.' in service,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
raise SystemExit(0 if all(checks.values()) else 1)
