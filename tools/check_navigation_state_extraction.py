from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
state = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/AppNavigationState.kt').read_text()
assert 'AppNavigationState()' in main
assert 'var currentScreen by navigation.currentScreen' in main
assert 'var selectedClass by navigation.selectedClass' in main
assert 'var selectedTerm by navigation.selectedTerm' in main
assert 'var selectedSection by navigation.selectedSection' in main
assert 'var academicYear by navigation.academicYear' in main
assert 'fun navigateTo(screen: Screen)' in state
assert 'require(value in 1900..2200)' in state
print('PASS: navigation and academic workspace selection state extracted')
