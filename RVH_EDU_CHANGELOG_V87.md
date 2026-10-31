# RVH Edu V87 — Multi-page Report Cards

## Changes
- Report cards now paginate subject rows instead of drawing every subject onto a single A4 page.
- The first page preserves the learner identity panel; continuation pages repeat school identity and learner/class/assessment context.
- Totals, result completeness, teacher/head-teacher comments, and signature areas are kept together on the final page.
- Continuation cues and page X of Y labels are printed on the report pages.
- Added focused source checks for report-card pagination and extended the academic-export assertions.

## Validation
- `tools/check_report_card_pagination_v87.py`: 7/7 source-level checks passed.
- Python test syntax checked.

## Not verified
- Full Android compilation, actual PDF rendering, visual inspection of generated pages, and physical A4 printing are still outstanding. Source-level assertions do not establish visual correctness.
