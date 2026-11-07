from pathlib import Path

root = Path(__file__).resolve().parents[1]
feedback = (root / "app/src/main/java/com/henrylumis/rvhgrader/ui/FeedbackAndUpdatesScreen.kt").read_text()
settings = (root / "app/src/main/java/com/henrylumis/rvhgrader/ui/SettingsHubScreen.kt").read_text()
gradle = (root / "app/build.gradle.kts").read_text()
workflow = (root / ".github/workflows/android-build.yml").read_text()
checks = {
    "official Telegram invite is exact": 'https://t.me/+nII1BLKPzJw4MmU0' in feedback,
    "official WhatsApp channel is exact": 'https://whatsapp.com/channel/0029VbDuyWL3AzNJNWCkps3Q' in feedback,
    "community section is visible in Feedback & Updates": 'JOIN THE RVH COMMUNITY' in feedback,
    "Telegram action is clearly labelled": 'Join RVH on Telegram' in feedback,
    "WhatsApp channel action is clearly labelled": 'Follow RVH on WhatsApp' in feedback,
    "destinations open through Android browsable ACTION_VIEW": 'Intent(Intent.ACTION_VIEW, Uri.parse(destination))' in feedback and 'Intent.CATEGORY_BROWSABLE' in feedback and 'catch (_: Exception)' in feedback,
    "existing feedback and privacy content remains": 'SEND FEEDBACK' in feedback and 'PRIVACY' in feedback,
    "settings entry advertises community access": 'Join the RVH Telegram group or WhatsApp channel' in settings,
    "release version advances": 'versionCode = 160' in gradle and 'versionName = "1.7.4"' in gradle,
    "CI artifact matches release version": 'RVHEdu-1.7.4-release.apk' in workflow and 'v1.7.4-build160' in workflow,
}
for label, passed in checks.items():
    print(("PASS" if passed else "FAIL") + " — " + label)
if not all(checks.values()):
    raise SystemExit(1)
print(f"V191 COMMUNITY INTEGRATION CONTRACTS PASSED: {len(checks)}/{len(checks)}")
