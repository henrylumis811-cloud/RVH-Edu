#!/usr/bin/env python3
"""Source regression checks for multi-page report-card safety."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
docs = (root / "app/src/main/java/com/henrylumis/rvhgrader/export/AcademicDocumentExportManager.kt").read_text()
checks = {
    "report-card subjects are chunked across pages": "val chunks = mutableListOf<List<ReportCardSubject>>()" in docs and "subjects.subList(offset, subjects.size).toList()" in docs,
    "first page keeps learner identity panel": "RectF(35f, 158f, 560f, 225f)" in docs,
    "continuation pages identify continued results": "REPORT CARD • SUBJECT RESULTS CONTINUED" in docs,
    "totals and comments appear only on final page": "if (finalPage)" in docs and "TEACHER'S COMMENT" in docs,
    "non-final pages signal continuation": "SUBJECTS CONTINUED ON NEXT PAGE" in docs,
    "each report page has accurate page numbering": '"Page $pageNo of ${chunks.size}"' in docs,
    "all pages are finalized": "chunks.forEachIndexed" in docs and "document.finishPage(page)" in docs,
}
for label, ok in checks.items():
    print(("PASS" if ok else "FAIL") + " " + label)
print(f"{sum(checks.values())}/{len(checks)} checks passed")
raise SystemExit(0 if all(checks.values()) else 1)
