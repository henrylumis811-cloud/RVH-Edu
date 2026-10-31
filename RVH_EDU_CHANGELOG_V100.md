# RVH Edu V100 — School directory and per-school settings foundation

- Added an offline school management repository: list, create, select and update school records.
- Active school selection is persisted locally and falls back safely to `legacy-school` if the saved ID is missing.
- School branding is now read from and written to the selected row in the `school` table.
- Class pass thresholds are stored under a school-specific preference key; the old preference remains a fallback for migration compatibility.
- No existing academic records are copied, moved or merged.

## Validation status

Source changes only. Android compilation, UI integration, device testing and real-database migration testing have not been performed. The existing settings screen does not yet expose a school switcher; this repository is the next integration foundation, not a claim that the full multi-school user experience is complete.
