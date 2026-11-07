#!/usr/bin/env python3
"""Source-level regression checks for shared form design on remaining workflows."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui'
checks = []
def check(name, condition):
    checks.append((name, bool(condition)))

archived = (UI / 'ArchivedLearnersScreen.kt').read_text()
progression = (UI / 'ClassProgressionScreen.kt').read_text()
imports = (UI / 'ImportReviewScreen.kt').read_text()
shared = (UI / 'RvhFormTextField.kt').read_text()

check('archived learner search uses shared field', 'RvhFormTextField(' in archived and 'Search archived learners' in archived)
check('archived search explains searchable identifiers', 'Search by learner name or admission number.' in archived)
check('progression target year uses shared field', 'label = "Target academic year"' in progression)
check('progression year requests numeric keyboard', 'KeyboardType.Number' in progression)
check('progression validates year range and order', 'targetYear !in 2000..2100 || targetYear <= sourceYear' in progression)
check('progression field exposes actionable error', 'Choose a year from 2000–2100' in progression)
check('import learner name uses shared field', 'label = "Learner name"' in imports and 'RvhFormTextField(' in imports)
check('import name has review guidance', 'Check the name before confirming this import.' in imports)
check('import subject marks use numeric keyboard', 'keyboardType = KeyboardType.Number' in imports)
check('import marks explain allowed range', '0–100; leave blank if not assessed.' in imports)
check('shared field supports helper and error states', 'helperText: String? = null' in shared and 'errorText: String? = null' in shared)
check('shared field maps errors to Material error state', 'isError = errorText != null' in shared)

failed = [name for name, passed in checks if not passed]
for name, passed in checks:
    print(f"{'PASS' if passed else 'FAIL'}: {name}")
print(f"{len(checks)-len(failed)}/{len(checks)} checks passed")
if failed:
    raise SystemExit(1)
