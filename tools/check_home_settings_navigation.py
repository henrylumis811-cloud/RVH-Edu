#!/usr/bin/env python3
"""Static regression checks for the learner-first home and consolidated settings hub."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
main = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt").read_text()
screens = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/Screen.kt").read_text()
home = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/HomeScreen.kt").read_text()
settings = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/SettingsHubScreen.kt").read_text()

nav_state = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/ui/AppNavigationState.kt").read_text()
assert ('mutableStateOf(Screen.HOME)' in main or 'mutableStateOf(Screen.HOME)' in nav_state), "App must start at the welcome/home screen"
assert 'Screen.HOME -> HomeScreen(' in main, "Home route must be wired"
for callback in ("onEnrollLearners", "onEnterMarks", "onBulkMarks", "onResults", "onAnalytics", "onHistory", "onImport", "onSettings", "onBackup"):
    assert callback in home, f"Home screen is missing workflow action: {callback}"
assert 'Screen.SETTINGS -> SettingsHubScreen(' in main, "Settings must route to the settings hub"
assert 'Screen.GRADING_RULES -> SchoolGradingSettingsScreen(' in main, "Existing grading rules must remain reachable"
for callback in ("onSchoolAndGrading", "onBackup", "onArchivedLearners", "onClassSetup"):
    assert callback in settings, f"Settings hub is missing action: {callback}"
assert 'HOME("Home")' in screens and 'SETTINGS("Settings & School Setup")' in screens
assert 'GRADING_RULES("School Details & Grading Rules")' in screens
assert 'OCR' not in home.upper(), "Home should not reintroduce OCR"
print("PASS: welcome home, core workflow shortcuts, and consolidated settings routes")
