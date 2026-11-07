# RVH Edu V156 — Bulk roster recomposition efficiency

- Cached the bulk mark-entry roster with Compose `remember` to avoid rerunning a synchronous roster database query on unrelated recompositions.
- Cache invalidates when the selected academic year, class, or academic data revision changes.
- Retains V155 asynchronous mark saving and earlier batch mark-loading changes.
- Version: 1.3.6 (versionCode 122).

## Verification
- Source assertion and ZIP integrity checks only. Android compilation, automated tests, and device profiling have not been run in this environment.
