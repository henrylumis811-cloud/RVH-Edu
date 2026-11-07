# RVH Edu V86 — Academic Export Layout Safety

## Changes
- Class-results PDF subject columns may shrink to a compact 12-point minimum instead of being forced to 28 points, preventing avoidable right-edge overflow for classes with many configured subjects.
- Class-results pagination now reserves extra vertical space for incomplete learners' missing-subject disclosure line before drawing a row. This reduces the risk of a disclosure line colliding with the page footer or being clipped at a page boundary.
- Expanded the focused academic-export source checks to cover both layout safeguards.

## Validation
- `tools/check_professional_academic_exports.py`: 13/13 source-level checks passed.
- `python3 -m py_compile tools/*.py`: passed.
- ZIP integrity check: passed.

## Not verified
- Full Android Gradle build, on-device PDF generation, rendered-page inspection, and physical A4 printing remain unverified in this environment. Source assertions do not replace visual PDF QA.
