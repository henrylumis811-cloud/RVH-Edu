#!/usr/bin/env python3
from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
gate = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/PinGate.kt').read_text()
gradle = (root / 'app/build.gradle.kts').read_text()
workflow = (root / '.github/workflows/android-build.yml').read_text()
checks = {
    'WhatsApp support number is correct': 'wa.me/256786997653' in gate,
    'Telegram support number is correct': 'phone=256786997653' in gate,
    'support email is correct': 'mailto:henrylumis811@gmail.com' in gate and 'henrylumis811@gmail.com' in gate,
    'generated request is attached to support message': 'Please generate a recovery response for this request:' in gate and 'putExtra(Intent.EXTRA_TEXT, message)' in gate,
    'teacher must generate request before contact actions appear': 'recoveryRequest?.let { req ->' in gate and 'Generate recovery request' in gate,
    'WhatsApp and Telegram actions are exposed': 'Send request on WhatsApp' in gate and 'Send request on Telegram' in gate,
    'email support action is exposed': 'Send request by email' in gate,
    'missing messaging apps have safe fallback': 'Missing or disabled messaging apps must never crash the PIN gate.' in gate,
    'install-over version advanced': 'versionCode = 160' in gradle and 'versionName = "1.7.4"' in gradle,
    'stable app ID retained': 'applicationId = "com.henrylumis.rvhedu"' in gradle,
    'CI builds and verifies both APKs': ':recovery-admin:assembleRelease' in workflow and 'apksigner" verify --verbose "$ADMIN_APK"' in workflow,
    'CI artifact matches current teacher version': 'RVHEdu-1.7.4-release.apk' in workflow and 'v1.7.4-build160' in workflow,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f"{sum(checks.values())}/{len(checks)} passed")
sys.exit(0 if all(checks.values()) else 1)
