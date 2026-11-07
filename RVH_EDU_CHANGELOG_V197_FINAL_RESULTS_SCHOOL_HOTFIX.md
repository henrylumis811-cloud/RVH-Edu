# V197 maintenance hotfix — class results and school scope

- Prevent silent fallback to `legacy-school` when a stale active-school preference exists in a multi-school installation.
- Preserve the original legacy-school default for installations with no active-school preference; safely auto-repair stale selection only when one school exists.
- Keep startup school branding and the Schools selection screen available when active-school resolution requires user action.
- Restrict official class results, rankings, and analytics to finalized assessments; drafts remain editable and are represented as incomplete in official results.
- Exclude school-archived learners from current active-class results and analytics while preserving historical data.
- No schema, package, version, or unrelated feature changes.
