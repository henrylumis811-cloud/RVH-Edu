#!/usr/bin/env python3
from pathlib import Path
import re, zipfile

root = Path(__file__).resolve().parents[1]
checks = []
def check(name, condition):
    checks.append((name, bool(condition)))

signer = (root/'recovery-admin/src/main/java/com/henrylumis/rvhrecovery/RecoverySigner.kt').read_text()
ui = (root/'recovery-admin/src/main/java/com/henrylumis/rvhrecovery/MainActivity.kt').read_text()
manifest = (root/'recovery-admin/src/main/AndroidManifest.xml').read_text()
workflow = (root/'.github/workflows/android-build.yml').read_text()
app_recovery = (root/'app/src/main/java/com/henrylumis/rvhgrader/ui/PinRecovery.kt').read_text()
check('Separate Android application module included', 'include(":app", ":recovery-admin")' in (root/'settings.gradle.kts').read_text())
check('Distinct admin application ID', 'com.henrylumis.rvhedu.recoveryadmin' in (root/'recovery-admin/build.gradle.kts').read_text())
check('Exact V1 domain separation retained', 'RVH-EDU-PIN-RECOVERY-V1:' in signer and 'RVH-EDU-PIN-RECOVERY-V1:' in app_recovery)
check('Embedded public key matches teacher app', 'xVQmVrJljqFSnkUvYm06vRlp_YrRL1McsqYh879SvnE' in signer and 'xVQmVrJljqFSnkUvYm06vRlp_YrRL1McsqYh879SvnE' in app_recovery)
check('Imported key cryptographically matched before storage', 'parsed.generatePublicKey().encoded' in signer and 'does not belong to this RVH Edu recovery build' in signer)
check('Private key encrypted with Android Keystore AES-GCM', 'AndroidKeyStore' in signer and 'AES/GCM/NoPadding' in signer)
check('Request expiry and nonce validated', 'expires - issued == 604800L' in signer and 'decode(fields[4]).size == 32' in signer)
check('Ed25519 response uses same signed message format', 'Ed25519Signer()' in signer and '(DOMAIN + request)' in signer)
check('UI supports import, paste, generate, copy and share', all(x in ui for x in ['OpenDocument()', 'Paste the complete request', 'Generate recovery response', 'Copy response', 'Share response']))
check('Admin APK built and signature verified in CI', ':recovery-admin:assembleRelease' in workflow and 'apksigner" verify --verbose "$ADMIN_APK"' in workflow)
check('No private key bundled in app source', not list(root.glob('recovery-admin/**/*.pem')) and not list(root.glob('recovery-admin/**/*.jks')))
check('Admin UI uses RVH Edu brand blue and teal palette', 'Color(0xFF2457C5)' in ui and 'Color(0xFF087F8C)' in ui)
check('Admin UI includes matching light and dark themes', 'RvhLight' in ui and 'RvhDark' in ui and 'isSystemInDarkTheme()' in ui)
check('Admin UI uses consistent rounded Material 3 component geometry', 'RoundedCornerShape(28.dp)' in ui and 'RoundedCornerShape(18.dp)' in ui and 'MaterialTheme' in ui)
check('Admin UI has branded dashboard and workflow hierarchy', all(x in ui for x in ['ADMINISTRATOR WORKSPACE', 'SectionHeading(\"01\"', 'SectionHeading(\"02\"', 'SummaryTile(']))
check('Admin app has dedicated RVH shield launcher icon', '@drawable/ic_rvh_recovery_admin' in manifest and (root/'recovery-admin/src/main/res/drawable/ic_rvh_recovery_admin.xml').exists())
check('Admin app visual refresh increments its own version', 'versionCode = 2' in (root/'recovery-admin/build.gradle.kts').read_text() and 'versionName = \"1.0.1\"' in (root/'recovery-admin/build.gradle.kts').read_text())
for name, ok in checks: print(('PASS' if ok else 'FAIL') + ' ' + name)
print(f'{sum(ok for _,ok in checks)}/{len(checks)} checks passed')
raise SystemExit(0 if all(ok for _,ok in checks) else 1)
