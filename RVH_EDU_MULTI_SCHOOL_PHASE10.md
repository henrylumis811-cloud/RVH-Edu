# Multi-school phase 10 — Active-school learner directory (V114)

Ordinary learner-directory reads and name/age search now use the selected school's enrollment roster. This closes a cross-school discovery leak in the service-level directory without duplicating learner identity or rewriting old records.

This is intentionally narrower than complete multi-school isolation. Admission numbers remain globally unique on the shared `learner` row; the directory does not expose un-enrolled learner records; and shared profile edit/archive semantics still need a defined policy. Source checks are not a substitute for Android compilation, SQLite migration fixtures, or device tests.
