#!/usr/bin/env python3
"""RVH Edu offline PIN recovery signer. Keep the private key file secret."""
import argparse, base64, sys, time
from cryptography.hazmat.primitives.serialization import load_pem_private_key
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey

DOMAIN = b"RVH-EDU-PIN-RECOVERY-V1:"

def unb64(s):
    return base64.urlsafe_b64decode(s + "=" * (-len(s) % 4))

def b64(b):
    return base64.urlsafe_b64encode(b).decode().rstrip("=")

def main():
    p = argparse.ArgumentParser(description="Sign one RVH Edu recovery request.")
    p.add_argument("--private-key", required=True, help="Path to private Ed25519 PEM; never share it")
    p.add_argument("--request", required=True, help="Exact request string copied from teacher")
    a = p.parse_args()
    try:
        payload = unb64(a.request).decode()
        parts = payload.split("|")
        now = int(time.time())
        if len(parts) != 5 or parts[0] != "v1" or len(unb64(parts[4])) != 32:
            raise ValueError("Malformed request")
        issued, expires = int(parts[2]), int(parts[3])
        if issued > now + 300 or expires <= now or expires-issued != 604800:
            raise ValueError("Request is expired or has an invalid lifetime")
        key = load_pem_private_key(open(a.private_key, "rb").read(), password=None)
        if not isinstance(key, Ed25519PrivateKey):
            raise ValueError("The key must be Ed25519")
        signature = key.sign(DOMAIN + a.request.encode())
        print(b64(signature))
    except Exception as e:
        print("ERROR:", e, file=sys.stderr)
        sys.exit(2)

if __name__ == "__main__":
    main()
