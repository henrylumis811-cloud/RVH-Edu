#!/usr/bin/env python3
"""Guard policy diagnostics against re-associating rows by non-unique learner names."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
start = source.index('fun classCompatibilityErrorForLearners(')
end = source.index('\n        fun capture(', start)
body = source[start:end]
assert 'entries.mapIndexed { index, (label, raw) ->' in body
assert 'data class ParsedEntry(val index: Int' in body
assert 'entries.firstOrNull { it.first == label }' not in body
assert 'hasPinned && hasMissingOrInvalid' in body
assert 'Learners to review:' in body
print('PASS: policy diagnostics preserve row identity and handle repeated learner names safely')
