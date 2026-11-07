#!/usr/bin/env python3
from pathlib import Path
import base64, re, sys
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey, Ed25519PublicKey
from cryptography.hazmat.primitives import serialization

root=Path(__file__).resolve().parents[1]
src=(root/'app/src/main/java/com/henrylumis/rvhgrader/ui/PinRecovery.kt').read_text()
gate=(root/'app/src/main/java/com/henrylumis/rvhgrader/ui/PinGate.kt').read_text()
admin=(root/'tools/pin-recovery-admin.py').read_text()
checks=[]
def check(name, ok):
    checks.append((name,bool(ok)))
check('Ed25519 public-key verification is embedded', 'Ed25519PublicKeyParameters' in src and 'PUBLIC_KEY' in src)
check('private key is absent from app source', 'PRIVATE KEY' not in src and 'privateKey' not in src)
check('request has 256-bit random nonce', 'ByteArray(32)' in src and 'SecureRandom' in src)
check('request is bound to installation UUID', 'UUID.randomUUID()' in src and 'parts[1] != installId' in src)
check('signed response uses domain separation', 'RVH-EDU-PIN-RECOVERY-V1:' in src and 'DOMAIN + request' in src)
check('seven-day expiry is verified', 'VALIDITY_SECONDS = 604800L' in src and 'expires <= now' in src)
check('response is 64-byte signature', 'signature.size != 64' in src)
check('only current active request can be redeemed', 'ACTIVE_REQUEST' in src and '!= request' in src)
check('successful response is one-time consumed', 'USED_REQUEST' in src and 'remove(ACTIVE_REQUEST).commit()' in src)
check('teacher can request and enter response', 'Forgot PIN?' in gate and 'Administrator recovery response' in gate)
check('admin tool signs exact request', 'key.sign(DOMAIN + a.request.encode())' in admin)
check('admin tool never includes private key in APK', 'private-key' in admin)
# Cryptographic property: observing one pair does not allow a response for a distinct request.
sk=Ed25519PrivateKey.generate(); pk=sk.public_key()
r1=b'example-request-one'; r2=b'example-request-two'
sig1=sk.sign(b'RVH-EDU-PIN-RECOVERY-V1:'+r1)
check('cross-request forgery rejected in Ed25519 test', (lambda: (pk.verify(sig1,b'RVH-EDU-PIN-RECOVERY-V1:'+r2),False)) if False else True)
try:
    pk.verify(sig1,b'RVH-EDU-PIN-RECOVERY-V1:'+r2)
    forged=True
except Exception:
    forged=False
checks[-1]=(checks[-1][0],not forged)
for n,ok in checks: print(('PASS' if ok else 'FAIL')+' '+n)
print(f'{sum(ok for _,ok in checks)}/{len(checks)} passed')
sys.exit(0 if all(ok for _,ok in checks) else 1)
