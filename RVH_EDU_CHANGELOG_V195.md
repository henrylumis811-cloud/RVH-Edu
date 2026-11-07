# RVH Edu V195 — PIN recovery support contacts

- Teacher app version: 1.7.3 (versionCode 159); application ID remains `com.henrylumis.rvhedu`.
- Added administrator support contacts to the Forgot PIN flow: WhatsApp and Telegram at +256 786 997 653, and email at henrylumis811@gmail.com.
- Teachers generate an installation-bound recovery request before contacting support. The exact request is prefilled into the selected channel.
- WhatsApp opens a direct chat to the configured support number. Telegram attempts a phone-based direct chat and falls back to Telegram sharing if that route is unavailable. Email opens a preaddressed support message.
- The recovery response field remains in the same recovery flow. Existing Ed25519 request/response protocol, active-request binding, expiry, one-time consumption, app ID, and stable signing identity are unchanged.
- No learner, school, assessment, or marks data is cleared or migrated by this change.
