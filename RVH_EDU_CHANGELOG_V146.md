# RVH Edu V146 — Navigation and scrolling continuity

- Version: 1.2.6 (versionCode 112).
- Added a `SaveableStateHolder` keyed by screen route so a screen can restore its saveable UI state when revisited. This is intended to preserve `LazyColumn` and `ScrollState` positions across in-app navigation without keeping every screen permanently composed.
- Added stable item keys to the home action rows and subject-configuration revision list to reduce item identity churn during updates.
- Preserved route-aware navigation and the bounded back stack from the preceding candidate.
- This is a source candidate. Android compilation, automated test execution, scroll-performance profiling, and on-device testing must still be run before release claims.
