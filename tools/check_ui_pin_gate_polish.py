from pathlib import Path

root = Path(__file__).resolve().parents[1]
pin = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/PinGate.kt').read_text()
checks = {
    'PIN gate uses app background and surface card': 'MaterialTheme.colorScheme.background' in pin and 'CardDefaults.cardColors' in pin,
    'PIN screen remains scrollable when keyboard is open': 'verticalScroll(rememberScrollState())' in pin and '.imePadding()' in pin,
    'branding identifies RVH Edu rather than a generic mainframe': '"RVH EDU"' in pin and 'OFFLINE SCHOOL MANAGEMENT' in pin,
    'first-launch setup and existing PIN unlock remain distinct': 'if (creating)' in pin and 'Unlock RVH Edu' in pin,
    'PIN input remains numeric and limited to eight digits': 'KeyboardType.NumberPassword' in pin and 'take(8)' in pin,
    'PIN creation still requires minimum length and confirmation': 'input.length < 4' in pin and 'input != confirmInput' in pin,
    'legacy PIN migration and hashed credential functions remain': 'verifyAndUpgradePin' in pin and 'PinCredential.create(pin)' in pin,
    'errors are shown with themed error container': 'MaterialTheme.colorScheme.errorContainer' in pin,
    'offline behaviour and Hellen dedication remain visible': 'Works offline' in pin and 'In dedication to Hellen' in pin,
    'primary actions have a large consistent touch target': 'height(54.dp)' in pin and 'RoundedCornerShape(16.dp)' in pin,
}
for label, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f'{len(checks)} PIN UI checks passed.')
