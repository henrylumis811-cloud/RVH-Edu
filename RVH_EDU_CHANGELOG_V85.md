# RVH Edu V85 — Professional Academic Exports

## What changed
- Redesigned individual report-card PDFs with a school identity header, learner/class metadata, readable subject table, summary metrics, completion status, teacher comments, signature areas, and a restrained academic-blue/teal visual system.
- Redesigned certificate PDFs as landscape certificates with a double frame, clear achievement hierarchy, learner name, class/year, result summary, and signature/date areas.
- Redesigned class-results PDF layout with a clear title, class/term metadata, learner/completeness/class-average summary, alternating row shading, adaptive subject columns, missing-subject notes, and page labels.
- Preserved the existing result models and calculation policy. These changes affect presentation, not grades, rankings, aggregates, divisions, or eligibility.
- Added focused source-level regression checks for key output-design elements.

## Validation status
- Focused export design assertions: 12/12 expected to pass (run `python3 tools/check_professional_academic_exports.py`).
- Python syntax check for the new test: run `python3 -m py_compile tools/check_professional_academic_exports.py`.
- Android Gradle build and physical print/share preview are not verified in this environment. Review the PDFs on a phone and at actual A4 print scale before calling the export work release-ready.
