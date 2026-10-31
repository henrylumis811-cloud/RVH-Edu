package com.henrylumis.rvhgrader.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.henrylumis.rvhgrader.core.reports.Certificate
import com.henrylumis.rvhgrader.core.reports.ReportCard
import com.henrylumis.rvhgrader.core.reports.ReportCardSubject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/** Print-ready academic documents, designed to remain legible on paper and on phones. */
object AcademicDocumentExportManager {
    private val navy = 0xFF17324D.toInt()
    private val teal = 0xFF147D82.toInt()
    private val pale = 0xFFEAF3F5.toInt()
    private val ink = 0xFF253746.toInt()
    private val muted = 0xFF647786.toInt()
    private val gold = 0xFFC49A43.toInt()

    fun exportReportCard(context: Context, report: ReportCard): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "${safe(report.learner.fullName)}_${report.academicYear}_${safe(report.assessment.term.displayName)}_Report_Card.pdf")
        writeReportCard(file, report)
        return file
    }

    fun exportCertificate(context: Context, certificate: Certificate): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "${safe(certificate.learner.fullName)}_${certificate.academicYear}_Certificate.pdf")
        writeCertificate(file, certificate)
        return file
    }

    private fun paint(size: Float, color: Int = ink, bold: Boolean = false): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size; this.color = color; typeface = if (bold) Typeface.create("sans-serif", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL)
    }

    private fun text(canvas: Canvas, value: String, x: Float, y: Float, p: Paint) { canvas.drawText(value, x, y, p) }
    private fun centered(canvas: Canvas, value: String, y: Float, p: Paint, centerX: Float) {
        val safe = ellipsize(value, p, 690f)
        text(canvas, safe, centerX - p.measureText(safe) / 2f, y, p)
    }
    private fun ellipsize(value: String, p: Paint, maxWidth: Float): String {
        if (p.measureText(value) <= maxWidth) return value
        var s = value
        while (s.isNotEmpty() && p.measureText("$s…") > maxWidth) s = s.dropLast(1)
        return "$s…"
    }
    private fun line(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, color: Int = 0xFFD7E1E7.toInt(), stroke: Float = 1f) {
        canvas.drawLine(x1, y1, x2, y2, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; strokeWidth = stroke })
    }
    private fun rounded(canvas: Canvas, rect: RectF, color: Int, radius: Float = 10f) {
        canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    }
    private fun drawBrandHeader(canvas: Canvas, width: Float, schoolName: String, subtitle: String) {
        canvas.drawRect(0f, 0f, width, 13f, Paint().apply { color = teal })
        centered(canvas, schoolName.ifBlank { "SCHOOL NAME" }, 51f, paint(23f, navy, true), width / 2f)
        centered(canvas, subtitle, 76f, paint(10f, muted), width / 2f)
        line(canvas, 40f, 92f, width - 40f, 92f, teal, 1.5f)
    }

    private fun writeReportCard(file: File, report: ReportCard) {
        val document = PdfDocument()
        val width = 595; val height = 842
        val subjects = report.subjects
        // Keep the first page focused on learner identity. Reserve space on the final page
        // for totals, result status, comments, and signatures instead of allowing rows to
        // collide with those sections when a school configures many subjects.
        val chunks = mutableListOf<List<ReportCardSubject>>()
        var offset = 0
        if (subjects.size <= 8) {
            chunks += subjects.toList()
            offset = subjects.size
        } else {
            chunks += subjects.take(8)
            offset = 8
            while (subjects.size - offset > 10) {
                val take = minOf(25, subjects.size - offset - 10)
                chunks += subjects.subList(offset, offset + take).toList()
                offset += take
            }
            chunks += subjects.subList(offset, subjects.size).toList()
        }
        if (chunks.isEmpty()) chunks += emptyList<ReportCardSubject>()

        chunks.forEachIndexed { pageIndex, chunk ->
            val pageNo = pageIndex + 1
            val page = document.startPage(PdfDocument.PageInfo.Builder(width, height, pageNo).create())
            val c = page.canvas
            c.drawColor(0xFFFFFFFF.toInt())
            val firstPage = pageIndex == 0
            if (firstPage) {
                drawBrandHeader(c, width.toFloat(), report.schoolName, "LEARNER PERFORMANCE • OFFICIAL ACADEMIC RECORD")
                text(c, "REPORT CARD", 40f, 124f, paint(17f, navy, true))
                text(c, "${report.assessment.term.displayName}  •  ${report.assessment.section.displayName}  •  ${report.academicYear}", 40f, 143f, paint(9f, muted))
                rounded(c, RectF(35f, 158f, 560f, 225f), pale, 9f)
                text(c, "LEARNER", 49f, 178f, paint(8f, muted, true))
                text(c, ellipsize(report.learner.fullName, paint(14f, navy, true), 330f), 49f, 198f, paint(14f, navy, true))
                text(c, "CLASS", 390f, 178f, paint(8f, muted, true))
                text(c, ellipsize(report.schoolClass.displayName, paint(11f, navy, true), 145f), 390f, 198f, paint(11f, navy, true))
                text(c, "ADMISSION NO.", 49f, 215f, paint(7f, muted, true))
                text(c, ellipsize(report.learner.admissionNumber?.takeIf { it.isNotBlank() } ?: "—", paint(9f), 155f), 130f, 215f, paint(9f, ink))
                text(c, "ACADEMIC YEAR", 300f, 215f, paint(7f, muted, true))
                text(c, report.academicYear.toString(), 390f, 215f, paint(9f, ink))
            } else {
                drawBrandHeader(c, width.toFloat(), report.schoolName, "REPORT CARD • SUBJECT RESULTS CONTINUED")
                text(c, ellipsize(report.learner.fullName, paint(12f, navy, true), 480f), 40f, 119f, paint(12f, navy, true))
                text(c, "${report.schoolClass.displayName}  •  ${report.assessment.term.displayName}  •  ${report.academicYear}", 40f, 137f, paint(8f, muted))
            }

            val left = 40f; val right = 555f
            var y = if (firstPage) 252f else 169f
            rounded(c, RectF(left, y - 17f, right, y + 7f), navy, 5f)
            text(c, "SUBJECT", 50f, y, paint(8f, 0xFFFFFFFF.toInt(), true))
            text(c, "MARK", 337f, y, paint(8f, 0xFFFFFFFF.toInt(), true))
            text(c, "OUT OF", 397f, y, paint(8f, 0xFFFFFFFF.toInt(), true))
            if (report.schoolClass.mode.name == "UPPER") text(c, "AGG.", 484f, y, paint(8f, 0xFFFFFFFF.toInt(), true))
            y += 25f
            chunk.forEachIndexed { index, subject ->
                if (index % 2 == 0) c.drawRect(left, y - 14f, right, y + 5f, Paint().apply { color = 0xFFF5F8FA.toInt() })
                text(c, ellipsize(subject.name, paint(9f), 265f), 50f, y, paint(9f))
                text(c, subject.mark?.toString() ?: "—", 347f, y, paint(9f, if (subject.mark == null) muted else ink, subject.mark != null))
                text(c, subject.maxMark.toString(), 405f, y, paint(9f, muted))
                if (report.schoolClass.mode.name == "UPPER") text(c, subject.aggregate?.toString() ?: "—", 491f, y, paint(9f, ink, true))
                line(c, left, y + 7f, right, y + 7f)
                y += 21f
            }

            val finalPage = pageIndex == chunks.lastIndex
            if (finalPage) {
                y += 8f
                rounded(c, RectF(left, y - 13f, right, y + 44f), pale, 8f)
                text(c, "TOTAL", 52f, y + 5f, paint(8f, muted, true))
                text(c, "${report.result.total} / ${report.result.maxTotal}", 52f, y + 26f, paint(13f, navy, true))
                text(c, "AVERAGE", 205f, y + 5f, paint(8f, muted, true))
                text(c, String.format(Locale.US, "%.1f%%", report.result.average), 205f, y + 26f, paint(13f, teal, true))
                if (report.result.graded) {
                    text(c, "AGGREGATE", 355f, y + 5f, paint(8f, muted, true))
                    text(c, report.result.aggregateSum?.toString() ?: "—", 355f, y + 26f, paint(13f, navy, true))
                    text(c, "DIVISION", 455f, y + 5f, paint(8f, muted, true))
                    text(c, report.result.division ?: "—", 455f, y + 26f, paint(13f, navy, true))
                }
                y += 67f
                val status = if (report.result.complete) "COMPLETE RESULT" else "INCOMPLETE RESULT • Missing: ${report.result.missingSubjects.size} subject(s)"
                text(c, status, 42f, y, paint(8f, if (report.result.complete) teal else 0xFF9B5B20.toInt(), true))
                y += 30f
                text(c, "TEACHER'S COMMENT", 42f, y, paint(8f, navy, true)); y += 16f
                rounded(c, RectF(40f, y - 10f, 555f, y + 35f), 0xFFF8FAFB.toInt(), 5f)
                text(c, ellipsize(report.teacherComment?.ifBlank { null } ?: "________________________________________________", paint(9f), 485f), 50f, y + 9f, paint(9f))
                y += 62f
                text(c, "HEAD TEACHER'S COMMENT", 42f, y, paint(8f, navy, true)); y += 16f
                rounded(c, RectF(40f, y - 10f, 555f, y + 35f), 0xFFF8FAFB.toInt(), 5f)
                text(c, ellipsize(report.headTeacherComment?.ifBlank { null } ?: "________________________________________________", paint(9f), 485f), 50f, y + 9f, paint(9f))
                y += 67f
                line(c, 42f, y, 220f, y, muted); line(c, 375f, y, 553f, y, muted)
                text(c, "Class teacher's signature", 42f, y + 14f, paint(7f, muted))
                text(c, "Head teacher's signature / stamp", 375f, y + 14f, paint(7f, muted))
            } else {
                text(c, "SUBJECTS CONTINUED ON NEXT PAGE", 40f, 780f, paint(8f, muted, true))
            }
            line(c, 40f, 806f, 555f, 806f, 0xFFD7E1E7.toInt())
            text(c, "RVH EDU  •  OFFLINE SCHOOL ACADEMIC SYSTEM", 40f, 822f, paint(7f, muted))
            text(c, "Page $pageNo of ${chunks.size}", 455f, 822f, paint(7f, muted))
            document.finishPage(page)
        }
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
    }

    private fun writeCertificate(file: File, certificate: Certificate) {
        val document = PdfDocument()
        val width = 842; val height = 595
        val page = document.startPage(PdfDocument.PageInfo.Builder(width, height, 1).create())
        val c = page.canvas
        c.drawColor(0xFFFFFEFB.toInt())
        // Double-frame certificate design with a restrained academic palette.
        c.drawRect(18f, 18f, width - 18f, height - 18f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = navy; style = Paint.Style.STROKE; strokeWidth = 2.4f })
        c.drawRect(27f, 27f, width - 27f, height - 27f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; style = Paint.Style.STROKE; strokeWidth = 0.9f })
        c.drawRect(28f, 28f, width - 28f, 38f, Paint().apply { color = navy })
        c.drawRect(28f, height - 38f, width - 28f, height - 28f, Paint().apply { color = navy })
        centered(c, certificate.schoolName.ifBlank { "SCHOOL NAME" }, 83f, paint(23f, navy, true), width / 2f)
        centered(c, "CERTIFICATE OF ACHIEVEMENT", 112f, paint(9f, teal, true), width / 2f)
        line(c, 270f, 127f, 572f, 127f, gold, 1.5f)
        centered(c, certificate.title.uppercase(Locale.ROOT), 164f, paint(19f, navy, true), width / 2f)
        centered(c, "This certificate is proudly presented to", 204f, paint(12f, muted), width / 2f)
        centered(c, certificate.learner.fullName, 250f, paint(29f, navy, true), width / 2f)
        line(c, 180f, 263f, 662f, 263f, gold, 1.2f)
        centered(c, "in recognition of successfully completing the recorded academic assessment", 291f, paint(11f, ink), width / 2f)
        centered(c, "${certificate.schoolClass.displayName}  •  Academic year ${certificate.academicYear}", 321f, paint(12f, ink, true), width / 2f)
        val resultLine = buildString {
            append("Average: "); append(String.format(Locale.US, "%.1f%%", certificate.result.average))
            if (certificate.result.graded) { append("     •     Aggregate: "); append(certificate.result.aggregateSum ?: "—"); append("     •     Division: "); append(certificate.result.division ?: "—") }
        }
        centered(c, resultLine, 350f, paint(11f, teal, true), width / 2f)
        line(c, 92f, 438f, 270f, 438f, muted); line(c, 335f, 438f, 507f, 438f, muted); line(c, 572f, 438f, 750f, 438f, muted)
        centered(c, "Class teacher", 455f, paint(8f, muted), 181f)
        centered(c, "Head teacher", 455f, paint(8f, muted), 421f)
        centered(c, "Date", 455f, paint(8f, muted), 661f)
        centered(c, "RVH EDU  •  OFFLINE SCHOOL ACADEMIC SYSTEM", 493f, paint(7f, muted), width / 2f)
        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
    }

    private fun safe(value: String): String = value.trim().replace(Regex("[^A-Za-z0-9_-]+"), "_").trim('_').ifBlank { "learner" }
}
