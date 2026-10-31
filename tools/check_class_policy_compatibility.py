#!/usr/bin/env python3
"""Source regression checks for safe class-wide assessment policy handling."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
snapshot = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AssessmentPolicySnapshot.kt').read_text()
service = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/assessment/AcademicDataService.kt').read_text()
exports = (root / 'app/src/main/java/com/henrylumis/rvhgrader/export/AcademicExportManager.kt').read_text()
activity = (root / 'app/src/main/java/com/henrylumis/rvhgrader/MainActivity.kt').read_text()
assert 'fun classCompatibilityError(policyJsons: Collection<String?>): String?' in snapshot
assert 'pinned.size != parsed.size' in snapshot
assert 'pinned.map(::signature).distinct().size > 1' in snapshot
assert 'policy.capturedAtMillis' not in snapshot[snapshot.index('fun classCompatibilityError'):snapshot.index('fun capture')]
assert service.count('AssessmentPolicySnapshot.classCompatibilityErrorForLearners(') >= 2
assert 'Combined rankings and analytics were stopped' in snapshot
assert 'CLASS RESULTS BLOCKED:' in activity and 'CLASS ANALYTICS BLOCKED:' in activity
assert 'AssessmentPolicySnapshot.classCompatibilityError(policySnapshotJsons)' in exports
analytics = service[service.index('fun classAnalytics'):service.index('fun findLearners')]
assert analytics.index('AssessmentPolicySnapshot.classCompatibilityError') < analytics.index('val selectedPolicy')
print('PASS: class policy compatibility is shared by results, analytics, and exports')
