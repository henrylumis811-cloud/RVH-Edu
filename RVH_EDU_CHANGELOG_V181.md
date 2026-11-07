# RVH Edu V181 — PIN unlock crash hardening

- Version: 1.6.0 (versionCode 146).
- Moves PIN hashing and verification off the Compose main thread.
- Prevents duplicate unlock taps while verification is running and displays a progress indicator.
- Catches credential-provider/preference exceptions at the PIN gate so a failed verification reports an actionable message rather than crashing from the button handler.
- Keeps existing PIN storage format and legacy PIN upgrade behavior.
- Does not change learner records or the database schema.

Validation note: source-level and regression checks can be run in this environment; Android compilation and physical-device verification still require the GitHub Actions build and installation on a device.
