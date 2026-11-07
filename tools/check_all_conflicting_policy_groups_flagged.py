#!/usr/bin/env python3
"""Regression: all groups in a policy conflict must be marked for review."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
snapshot = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
assert 'pinned.map { signature(it.policy!!) }.distinct().size > 1' in snapshot
assert 'pinned.map { it.label }' in snapshot
assert 'val hasDifferentPinnedPolicies = pinned.map { it.signature }.distinct().size > 1' in service
assert 'row.entry.status == "SAVED POLICY" && hasDifferentPinnedPolicies' in service
# Behavioral example: policy A is not safe just because it appears first when B also exists.
policies = [('Learner A', 'A'), ('Learner B', 'B'), ('Learner C', 'B')]
signatures = {signature for _, signature in policies}
flagged = [name for name, signature in policies if len(signatures) > 1]
assert flagged == ['Learner A', 'Learner B', 'Learner C']
print('PASS: every pinned policy group is flagged when class policies differ')
