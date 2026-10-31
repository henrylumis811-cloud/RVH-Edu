# RVH Edu online service setup (before public release)

The app intentionally ships with both endpoint constants blank. This prevents it from sending data to an unowned or guessed server.

## Update manifest
Host a JSON file at a stable HTTPS URL and set `OnlineServices.RELEASE_MANIFEST_URL` to it. Required fields:

- `versionCode`: integer greater than the installed app version when an update exists.
- `versionName`: human-readable version label.
- `message`: short release note.
- `downloadUrl`: HTTPS page hosting the signed update, or the official Play Store listing after publication.

Use the same Android application ID and signing identity for APK updates, and increase `versionCode` for every release. If publishing through Google Play, prefer the Play Store listing and its native update delivery rather than a direct APK download.

## Anonymous audience endpoint
Set `OnlineServices.AUDIENCE_ENDPOINT_URL` to an HTTPS endpoint you control. It receives a JSON POST with only `installId` (random app-install identifier), `versionCode`, and `platform`. Count distinct active installation IDs over a chosen time window; do not treat them as exact human users. Do not collect school names, learner names, marks, reports, contacts, or backup contents. Publish a clear privacy notice, provide a way to opt out, secure the endpoint, set retention limits, and comply with applicable privacy law.

The client sends only after the teacher explicitly enables anonymous audience sharing, while Android reports a validated internet connection, and no more than once per seven days after a successful check-in. Normal network metadata, including IP address, can still be visible to the server. The client does not make the audience request when consent is off.

## Release gate
- Configure and test both endpoints over HTTPS.
- Verify the privacy disclosure and opt-out on a real device.
- Verify update dialog, valid URL, offline failure, and no duplicate prompts.
- Run `gradle assembleDebug --no-daemon` in CI and install/update-test the generated APK on a test phone.
