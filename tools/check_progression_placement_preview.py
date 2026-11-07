from pathlib import Path
root = Path(__file__).resolve().parents[1]
screen = (root / 'app/src/main/java/com/henrylumis/rvhgrader/ui/ClassProgressionScreen.kt').read_text()
checks = {
    'same-class repeat is explicitly distinguished': '"Repeat the same class"' in screen,
    'next-class promotion is explicitly distinguished': '"Promote to the next class"' in screen,
    'lower-class placement warns teacher': '"Move to a lower class — review carefully"' in screen and 'destination is below the source class' in screen,
    'skipped classes require careful review': '"Skip one or more classes — review placement carefully"' in screen and 'skips one or more classes' in screen,
    'placement preview shows selected count and years': 'Placement preview' in screen and 'selected • $sourceYear' in screen,
    'confirmation includes placement intent': 'append("$placementKind:' in screen,
    'school-leaver handling is not conflated with progression': 'School leavers should be handled separately through learner archiving.' in screen,
    'existing records and atomic rejection are explained': 'source record has changed' in screen and 'without partial changes' in screen,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if failed: raise SystemExit(1)
print('PROGRESSION_PLACEMENT_PREVIEW_OK')
