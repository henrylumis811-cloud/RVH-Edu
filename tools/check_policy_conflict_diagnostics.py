#!/usr/bin/env python3
"""Ensure blocked class-wide operations identify learners whose policies need review."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
snapshot = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
assert 'fun classCompatibilityErrorForLearners(entries: List<Pair<String, String?>>): String?' in snapshot
assert 'Learners to review:' in snapshot
assert 'named.take(8)' in snapshot
assert 'classCompatibilityErrorForLearners(' in service
assert 'learnerRepository.getById(enrollment.learnerId)?.fullName' in service
print('PASS: class policy conflicts include concise learner-level diagnostics')
