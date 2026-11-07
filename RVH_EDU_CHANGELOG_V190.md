# RVH Edu V190 — Quick Grading visual consistency

- Refreshed the existing Quick Grading screen to use the same RVH EDU app-bar identity, typography hierarchy, theme colours, card geometry and spacing as the main workspace.
- Grouped class, term and assessment-period selection in a dedicated themed session card.
- Reworked quick entry into a clearer form card with descriptive guidance, learner-name capitalization, subject mark grouping and a consistent primary action.
- Updated standings cards to match the rest of RVH Edu and made missing subject marks visibly distinct from an actual zero.
- Preserved the existing save callbacks, assessment workflow, export flow, selected session state and persistent signing identity.
- Version: 1.6.9; versionCode: 155; application ID unchanged: `com.henrylumis.rvhedu`.

Validation: source-level UI contracts and existing regression scripts are run before handoff. A successful Gradle build and physical-device check must still be confirmed by CI/device testing; no such result is claimed by this source update.
