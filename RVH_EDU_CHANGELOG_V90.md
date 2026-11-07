# RVH Edu V90 — Coroutine launch import fix

## Fix
- Added the explicit `kotlinx.coroutines.launch` import in `MainActivity.kt`.
- This resolves the remaining `Unresolved reference: launch` errors for the drawer open/close actions and allows their suspend functions to be called inside the coroutine launched by `rememberCoroutineScope()`.

## Validation
- Source assertion confirms the import is present and both drawer actions use `navScope.launch`.
- ZIP integrity verified after packaging.
- Full Android compilation has not been run in this environment; GitHub Actions must confirm the result.
