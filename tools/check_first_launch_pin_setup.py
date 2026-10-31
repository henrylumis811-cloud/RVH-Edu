#!/usr/bin/env python3
"""Guard first-launch PIN setup and salted credential storage."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
pin_gate = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/PinGate.kt").read_text()
credential = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/PinCredential.kt").read_text()
main = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt").read_text()

checks = {
    "application starts locked": "var unlocked by remember { mutableStateOf(false) }" in main,
    "gate is shown before app content": "if (!unlocked)" in main and "PinGate(context = context" in main,
    "first launch requires PIN creation": "val creating = !pinState.isConfigured" in pin_gate and "if (creating)" in pin_gate,
    "PIN confirmation is required": "input != confirmInput" in pin_gate,
    "minimum PIN length is enforced": "input.length < 4" in pin_gate,
    "PIN input is limited to eight digits": "take(8)" in pin_gate,
    "new PIN is stored as salted credential": "PinCredential.create(pin)" in pin_gate,
    "plaintext legacy PIN is removed after migration": ".remove(KEY_PIN)" in pin_gate,
    "legacy PIN only migrates after successful match": "if (pin != legacy) return false" in pin_gate,
    "credential derivation uses PBKDF2": "PBKDF2WithHmacSHA1" in credential,
    "credential salt is random": "SecureRandom()" in credential and "random::nextBytes" in credential,
    "verification uses constant-time comparison": "MessageDigest.isEqual(expected, actual)" in credential,
    "malformed credentials fail closed": "fromHexOrNull() ?: return false" in credential,
}
failed = [name for name, passed in checks.items() if not passed]
for name, passed in checks.items():
    print(("PASS" if passed else "FAIL") + " - " + name)
if failed:
    raise SystemExit(f"{len(failed)} PIN setup checks failed")
print(f"{len(checks)} first-launch PIN checks passed")
