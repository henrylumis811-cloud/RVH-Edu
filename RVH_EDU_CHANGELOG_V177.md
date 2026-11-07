# RVH Edu V177 — Package ID correction

- App display name remains `RVH Edu`.
- Corrected Android application ID from `com.henrylumis.rvhgrader` to `com.henrylumis.rvhedu`.
- Kept the Kotlin namespace/package paths unchanged to avoid an unnecessary source-package migration.
- Version name: `1.5.7`; version code: `143`.
- This is a fresh app identity. Android treats it as a separate app from builds using the old application ID; old local app data is not migrated.
- No emulator/instrumentation result is claimed. Build and device installation must be verified by CI and on the phone.
