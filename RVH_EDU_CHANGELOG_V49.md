# RVH Edu V49 — Complete policy-conflict diagnostics

- Fixed a review-screen blind spot: when multiple saved policy signatures exist, every learner with a pinned policy is now marked as part of the conflicting set, including learners whose policy happened to be encountered first.
- Fixed blocked-operation diagnostics to list every pinned policy group participating in a conflict, rather than listing only groups different from the first policy.
- Kept the mixed legacy/pinned path distinct so legacy/missing records remain individually identifiable.
- No marks, policy snapshots, or historical records are modified by this diagnostic change.

Validation: automated source checks and ZIP integrity only. Full Android Gradle build and physical-device testing have not been performed.
