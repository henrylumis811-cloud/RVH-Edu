# RVH Edu Secure PIN Recovery — V193

The app creates a 256-bit random nonce and installation-bound request. The administrator tool signs the exact request with Ed25519. The APK contains only the Ed25519 public key; the private key is kept separately by the administrator.

A request/response pair observed for another installation does not let an observer calculate a signature for a different request. Ed25519 signatures are unforgeable under the standard cryptographic assumption; the request is signed with a domain-separation prefix. The response is a 64-byte signature encoded base64url, not a short numeric code.

Requests expire after seven days. The app checks installation ID, nonce length, issue/expiry timestamps, signature, and one-time consumption. Successful recovery removes only the PIN credential; learner and assessment data are untouched.

## Administrator use

Install Python 3 and `cryptography` (`python -m pip install cryptography`). Keep `RVH-Edu-Recovery-Private-Key.pem` offline and private. Run:

`python tools/pin-recovery-admin.py --private-key RVH-Edu-Recovery-Private-Key.pem --request 'PASTE_EXACT_REQUEST'`

Send only the resulting response string to the teacher. Never put the private key in GitHub, the APK, a chat, or a public repository. Back it up securely; if lost, existing installations cannot be recovered using a replacement key.

## Security boundary

This protects against guessing/deriving responses from observed request/response pairs. It does not protect a rooted/compromised phone, malware with access to app preferences, or an attacker who steals the teacher's exact still-valid response before it is used. Android local PIN protection remains an access gate, not hardware-backed encryption of the school's database.
