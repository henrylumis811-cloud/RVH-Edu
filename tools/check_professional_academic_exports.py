#!/usr/bin/env python3
"""Focused source-level regression checks for the professional academic PDF designs."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
docs = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/export/AcademicDocumentExportManager.kt").read_text()
exports = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/export/AcademicExportManager.kt").read_text()
checks = {
    "report card has school identity header": "drawBrandHeader(c, width.toFloat(), report.schoolName" in docs,
    "report card includes learner admission number": "report.learner.admissionNumber" in docs,
    "report card includes term and assessment section": "report.assessment.section.displayName" in docs,
    "report card includes complete/incomplete status": "COMPLETE RESULT" in docs and "INCOMPLETE RESULT" in docs,
    "report card includes teacher and head teacher comment areas": "TEACHER'S COMMENT" in docs and "HEAD TEACHER'S COMMENT" in docs,
    "report card supports subject pagination": "chunks.forEachIndexed" in docs and "Page $pageNo of ${chunks.size}" in docs,
    "report card continuation pages identify learner and assessment": "REPORT CARD • SUBJECT RESULTS CONTINUED" in docs,
    "report card includes signature lines": "Class teacher's signature" in docs and "Head teacher's signature / stamp" in docs,
    "certificate has a double frame": "style = Paint.Style.STROKE" in docs and "height - 18f" in docs and "height - 27f" in docs,
    "certificate includes learner and class/year": "certificate.learner.fullName" in docs and "certificate.schoolClass.displayName" in docs,
    "class PDF has title and summary metrics": "CLASS RESULTS REGISTER" in exports and "Class average:" in exports,
    "class PDF includes incomplete-subject disclosure": '"Incomplete: ${row.missingSubjects.joinToString' in exports,
    "class PDF includes page numbers and branding": '"Page $pageNumber"' in exports and "OFFLINE SCHOOL ACADEMIC SYSTEM" in exports,
    "class PDF uses page-bounded subject column widths without a forced minimum": "actualSubjectWidth" in exports and "val learnerEndX = left + 8f + 30f + (learnerWidth - 30f)" in exports and "coerceAtMost(54f)" in exports and "coerceIn(12f, 54f)" not in exports,
    "class PDF reserves space for incomplete-row disclosure before pagination": "requiredRowSpace = if (row.complete) 32f else 42f" in exports and "if (y > pageHeight - requiredRowSpace)" in exports,
}
failed = [name for name, passed in checks.items() if not passed]
for name, passed in checks.items():
    print(("PASS" if passed else "FAIL") + " " + name)
print(f"{len(checks) - len(failed)}/{len(checks)} checks passed")
raise SystemExit(1 if failed else 0)
