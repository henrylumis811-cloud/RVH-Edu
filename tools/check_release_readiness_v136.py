from pathlib import Path
import xml.etree.ElementTree as ET
ROOT = Path(__file__).resolve().parents[1]
app = (ROOT / 'app/build.gradle.kts').read_text()
root_gradle = (ROOT / 'build.gradle.kts').read_text()
manifest = (ROOT / 'app/src/main/AndroidManifest.xml').read_text()
main = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
feedback = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/FeedbackAndUpdatesScreen.kt').read_text()
workflow = (ROOT / '.github/workflows/android-build.yml').read_text()
checks = {
    'Android 16 compile SDK': 'compileSdk = 36' in app,
    'Android 16 target SDK': 'targetSdk = 36' in app,
    'release version increment': 'versionCode = 102' in app and 'versionName = "1.1.6"' in app,
    'AGP 8.10.1': 'version "8.10.1"' in root_gradle,
    'backup exclusion rules wired': '@xml/backup_rules' in manifest and '@xml/data_extraction_rules' in manifest,
    'no background audience/update polling': 'maybeSendAudienceCheckIn' not in main and 'delay(60L * 60L * 1000L)' not in main,
    'privacy policy reachable in app': 'showPrivacyPolicy' in feedback and 'privacy_policy.txt' in feedback,
    'direct email feedback': 'henrylumis811@gmail.com' in feedback,
    'direct WhatsApp feedback': '256786997653' in feedback,
    'CI installs SDK 36': 'platforms;android-36' in workflow,
    'CI runs Android unit tests': 'testDebugUnitTest' in workflow,
    'CI builds release AAB': 'bundleRelease' in workflow,
    'release signing not hardcoded': 'RVH_RELEASE_KEYSTORE_BASE64' in workflow and 'RVH_RELEASE_KEY_PASSWORD' in app,
    'privacy policy hosted-source draft exists': (ROOT / 'PRIVACY_POLICY.md').exists(),
}
for xml in (ROOT / 'app/src/main').rglob('*.xml'):
    ET.parse(xml)
for name, passed in checks.items():
    print(('PASS' if passed else 'FAIL') + ' ' + name)
assert all(checks.values()), 'Release-readiness static check failed'
print(f'PASS XML parsing for Android resources; {len(checks)}/{len(checks)} static checks passed')
