from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
results = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/ClassResultsScreen.kt').read_text()
analytics = (ROOT / 'app/src/main/java/com/henrylumis/rvhgrader/ui/ClassAnalyticsScreen.kt').read_text()
checks = {
    'results retains class and academic period context': 'Academic year $year' in results and 'schoolClass.displayName' in results,
    'results selectors are scrollable chips': 'FilterChip(selected = item == term' in results and 'horizontalScroll(rememberScrollState())' in results,
    'results summary distinguishes total complete and average': 'LEARNERS' in results and 'COMPLETE' in results and 'CLASS AVG.' in results,
    'incomplete learners are explicitly not ranked': "won\'t be ranked" in results and 'missingSubjects.joinToString' in results,
    'learner result rows show rank and average': 'ranked.position' in results and 'result.average' in results,
    'report and certificate actions remain wired': 'onReportCard(result.learnerId)' in results and 'onCertificate(result.learnerId)' in results,
    'export action remains wired': 'onClick = onExport' in results,
    'results include an empty state': 'No results yet' in results,
    'analytics has assessment-period chips': 'FilterChip(selected = t == term' in analytics and 'FilterChip(selected = s == section' in analytics,
    'analytics shows class completion and pass-rate indicators': 'summary.completeCount' in analytics and 'summary.passRatePercent' in analytics,
    'subject coverage is visualized': 'subject.coveragePercent / 100f' in analytics and 'subject.missingCount' in analytics,
    'risk level and reasons remain visible': 'risk.level.name' in analytics and 'risk.reasons' in analytics,
    'analytics empty states remain explicit': 'No subject data yet' in analytics and 'No current risk flags' in analytics,
    'all key screens remain theme-aware': 'MaterialTheme.colorScheme' in results and 'MaterialTheme.colorScheme' in analytics,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
if failed:
    raise SystemExit(f'{len(failed)} check(s) failed')
print(f'{len(checks)} results and analytics UI checks passed')
