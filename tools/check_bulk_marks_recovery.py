from pathlib import Path
p = Path('app/src/main/java/com/henrylumis/rvhgrader/ui/BulkMarkEntryScreen.kt')
s = p.read_text()
checks = {
    'device-local recovery storage': 'getSharedPreferences("rvh_bulk_mark_recovery"' in s,
    'draft scoped to academic context': 'draft_${year}_${schoolClass.name}_${term.name}_${section.name}_$selectedSubject' in s,
    'snapshot stores per-enrollment values': 'snapshot.put(entry.enrollment.id, values[entry.enrollment.id].orEmpty())' in s,
    'restores after screen/process recreation': 'val recovered = recoveryPreferences.getString(recoveryKey, null)' in s,
    'recovery only applied to current roster': 'roster.forEach { entry ->' in s and 'snapshot.has(entry.enrollment.id)' in s,
    'recovery message tells teacher to review': 'Recovered unsaved marks from this device. Review and save them.' in s,
    'input changes persist recovery': 'values[entry.enrollment.id] = digits' in s and 'persistRecoveryDraft()' in s and 'if (valid)' in s,
    'successful save clears recovery': 'if (errors.isEmpty()) clearRecoveryDraft() else persistRecoveryDraft()' in s,
    'discard clears recovery before navigation': 'clearRecoveryDraft()' in s and 'action?.invoke()' in s and s.index('clearRecoveryDraft()', s.index('RvhConfirmDialog(')) < s.index('action?.invoke()', s.index('RvhConfirmDialog(')),
    'corrupt recovery is discarded': '}.onFailure { clearRecoveryDraft() }' in s,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()):
    raise SystemExit(1)
print('BULK_MARKS_RECOVERY_STATIC_CHECKS_OK')
