# RVH Edu V88 — Final class PDF column safety

- Replaced the hard minimum subject-column width with a calculation based on the actual learner, total, average, aggregate, and division columns. This prevents the minimum-width rule from pushing the end of wide class-results tables beyond the page.
- Kept subject headings clipped to their allocated width and preserved incomplete-result disclosures and row pagination.
- Updated the source regression assertions and added geometry checks across 1, 5, 12, 20, 40, and 80 subjects for both lower- and upper-school layouts.
- Validation is source/geometry-level only. Actual Android PDF rendering, visual inspection, printing, full Gradle build, and Tecno Spark 30C testing remain outstanding.
