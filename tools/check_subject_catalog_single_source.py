#!/usr/bin/env python3
"""Guard against grading logic maintaining a second, drifting subject catalog."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
catalog = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/grading/SubjectCatalog.kt').read_text()
logic = (root / 'app/src/main/java/com/henrylumis/rvhgrader/grading/GradingLogic.kt').read_text()
assert 'fun forMode(mode: SystemMode)' in catalog
assert 'fun validationError()' in catalog
assert 'SubjectCatalog.forMode(SystemMode.LOWER).map { it.code }' in logic
assert 'SubjectCatalog.forMode(SystemMode.UPPER).map { it.code }' in logic
assert 'SubjectCatalog.all().associate { it.code to it.shortName }' in logic
assert 'val lowerSubjects = listOf(' not in logic
assert 'val upperSubjects = listOf(' not in logic
print('PASS: grading logic uses the central subject catalog')
