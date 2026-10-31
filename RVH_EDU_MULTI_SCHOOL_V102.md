# Multi-school implementation status — V102

The active-school preference is now passed explicitly into the academic service's enrollment creation, class progression, and enrollment-based result/roster queries. The legacy-school default remains in repository methods for compatibility with older call sites.

This is not a security boundary between different user accounts: the app remains a local, single-device offline app. The next steps are to audit all learner/assessment/mark/report access paths, scope learner identity/admission numbers appropriately, and extend backup/restore in dependency-safe order.
