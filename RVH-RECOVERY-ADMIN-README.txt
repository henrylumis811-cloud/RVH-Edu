RVH Edu Secure Recovery — Administrator Kit

CONTENTS
- pin-recovery-admin.py: offline Ed25519 response signer
- RVH-Edu-Recovery-Private-Key.pem: private signing key for this RVH Edu installation family

CRITICAL SECURITY
Keep the PEM private key confidential. Do not upload it to GitHub, include it in the Android source ZIP, send it to teachers, or paste it into chat. Store at least one encrypted offline backup. Anyone who obtains this key can generate valid recovery responses for any request from this app version. If the key is lost, recovery cannot be performed with a replacement key unless the app is updated with a new public key.

REQUIREMENT
Python 3 and the cryptography package:
  python -m pip install cryptography

USAGE
  python pin-recovery-admin.py --private-key RVH-Edu-Recovery-Private-Key.pem --request 'PASTE_TEACHER_REQUEST'

Send ONLY the printed response string to the teacher. The tool does not contact a server; signing is performed locally and offline.
