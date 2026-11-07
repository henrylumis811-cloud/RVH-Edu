# RVH Edu V172 — Kotlin compilation fix

Version: 1.5.2  
Version code: 138

## Fix

Moved the local `navigateTo` and `goTo` function declarations above the document-import activity-result launcher in `MainActivity.kt`. The import callback referenced `navigateTo` before its local declaration, causing Kotlin compilation to fail with `Unresolved reference: navigateTo`.

The navigation implementation and drawer-closing behavior are unchanged.

## Verification

- Source-level assertion confirms the navigation function is declared before the launcher callback and only once.
- Backup/restore regression suite is run separately where the local Python environment supports it.
- ZIP archive integrity checked.
- Gradle compilation and Android tests require CI and have not been claimed as passed.
