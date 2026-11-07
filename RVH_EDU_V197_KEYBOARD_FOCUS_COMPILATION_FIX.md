# RVH Edu V197 — Keyboard Focus Compilation Fix

## Scope
This is a narrowly scoped correction to the keyboard focus-visibility hotfix.

## Root cause
The keyboard focus hotfix uses Compose Foundation's `BringIntoViewRequester`, `bringIntoViewRequester`, and `bringIntoView()` APIs. In the project's Compose Foundation version these APIs are marked experimental. The two affected screen composables did not opt in to `ExperimentalFoundationApi`, so Kotlin compilation failed at those call sites.

## Correction
- Added the `ExperimentalFoundationApi` import to `BulkMarkEntryScreen.kt` and `MarkSheetScreen.kt`.
- Added `ExperimentalFoundationApi::class` to the existing function-level `@OptIn` annotations.
- Preserved the existing focus listeners, scroll requests, keyboard handling, UI, persistence, and application identity.

## Verification
- Source-level check confirms both affected composables opt in to `ExperimentalFoundationApi`.
- ZIP integrity check passed after packaging.
- Android/Gradle compilation was not run in this environment because neither a Gradle wrapper nor a system Gradle installation is available. GitHub Actions must confirm compilation.
