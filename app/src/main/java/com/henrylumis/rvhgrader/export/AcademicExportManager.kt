package com.henrylumis.rvhgrader.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.assessment.AssessmentPolicySnapshot
import com.henrylumis.rvhgrader.core.analytics.AcademicAnalytics
import com.henrylumis.rvhgrader.core.analytics.SubjectAnalytics
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.core.model.RankedResult
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.roundToInt
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Exports the new RVH Edu model. No legacy StudentRecord objects are involved. */
object AcademicExportManager {
    data class ClassExportRow(
        val position: Int?,
        val learnerName: String,
        val marks: Map<String, Int>,
        val total: Int,
        val maxTotal: Int,
        val average: Double,
        val aggregate: Int?,
        val division: String?,
        val complete: Boolean,
        val missingSubjects: List<String>
    )

    fun exportClassResults(
        context: Context,
        schoolName: String,
        schoolClass: SchoolClass,
        year: Int,
        term: Term,
        section: TermSection,
        results: List<RankedResult>,
        learnerNames: Map<String, String>,
        marksByLearner: Map<String, List<Mark>>,
        format: ExportFormat,
        policySnapshotJson: String? = null,
        policySnapshotJsons: Collection<String?>? = null,
        currentGradingScale: GradingScale = GradingScale.default(),
        currentPassPercent: Double = 50.0
    ): File {
        require(year > 0) { "Academic year must be positive" }
        require(currentPassPercent.isFinite() && currentPassPercent in 0.0..100.0) {
            "Class pass threshold must be between 0 and 100 percent"
        }
        require(results.map { it.result.learnerId }.distinct().size == results.size) {
            "Class export contains duplicate learner results"
        }
        val assessmentGroupKeys = results.map { it.result.assessmentGroupKey }
        if (assessmentGroupKeys.any { it != null }) {
            require(assessmentGroupKeys.all { !it.isNullOrBlank() } && assessmentGroupKeys.distinct().size == 1) {
                "Class export cannot mix assessment groups"
            }
        } else {
            // Backward compatibility for older callers that export a single shared assessment ID.
            require(results.map { it.result.assessmentId }.distinct().size <= 1) {
                "Class export cannot mix assessments"
            }
        }
        // A combined class export must not borrow the first learner's policy for everyone.
        // A null snapshot mixed with pinned snapshots is also a conflict: legacy rules are unknown.
        val savedPolicy = if (policySnapshotJsons != null) {
            val compatibilityError = AssessmentPolicySnapshot.classCompatibilityError(policySnapshotJsons)
            require(compatibilityError == null) { compatibilityError ?: "Assessment policies are incompatible." }
            policySnapshotJsons.firstNotNullOfOrNull { json ->
                json?.let { AssessmentPolicySnapshot.parse(it) }
            }
        } else {
            policySnapshotJson?.let { runCatching { AssessmentPolicySnapshot.parse(it) }.getOrElse {
                throw IllegalArgumentException("The saved assessment policy is invalid; export was stopped to protect results.")
            } }
        }
        val expectedSubjects = savedPolicy?.subjects?.map { it.code } ?: subjectsFor(schoolClass)
        val savedLabels = savedPolicy?.subjects?.associate { it.code to it.displayName }.orEmpty()
        val labels = expectedSubjects.associateWith { code -> savedLabels[code] ?: subjectLabel(code) }
        val effectiveScale = savedPolicy?.takeIf { it.hasCompleteScoringPolicy }?.gradingScale() ?: currentGradingScale
        val effectivePassPercent = savedPolicy?.classPassPercent ?: currentPassPercent
        val rows = results.map { ranked ->
            val result = ranked.result
            val learnerMarks = marksByLearner[result.learnerId].orEmpty()
            require(learnerMarks.map { it.subjectCode }.distinct().size == learnerMarks.size) {
                "Duplicate subject marks for learner ${result.learnerId}"
            }
            require(learnerMarks.all { it.assessmentId == result.assessmentId }) {
                "Export marks do not match learner ${result.learnerId}'s assessment"
            }
            require(learnerMarks.all { it.subjectCode in expectedSubjects }) {
                "Export contains a subject not assigned to the saved assessment policy for ${schoolClass.displayName}"
            }
            require(learnerMarks.all { it.maxMark > 0 && it.mark in 0..it.maxMark }) {
                "Export contains a mark outside its valid range"
            }
            val byCode = learnerMarks.associateBy { it.subjectCode }
            val missingCodes = expectedSubjects.filterNot { it in byCode }
            val calculatedTotal = learnerMarks.sumOf { it.mark }
            val calculatedMax = expectedSubjects.sumOf { byCode[it]?.maxMark ?: 100 }
            val calculatedAverage = if (calculatedMax == 0) 0.0
                else (calculatedTotal.toDouble() / calculatedMax * 100.0).let { (it * 100.0).roundToInt() / 100.0 }
            require(result.complete == missingCodes.isEmpty() && result.missingSubjects == missingCodes) {
                "Export completeness does not match marks for learner ${result.learnerId}"
            }
            require(result.total == calculatedTotal && result.maxTotal == calculatedMax &&
                kotlin.math.abs(result.average - calculatedAverage) < 0.000001) {
                "Export result totals do not match marks for learner ${result.learnerId}"
            }
            val marks = byCode.mapValues { it.value.mark }
            ClassExportRow(
                position = ranked.position.takeIf { result.complete },
                learnerName = learnerNames[result.learnerId] ?: "Unknown learner",
                marks = marks,
                total = result.total,
                maxTotal = result.maxTotal,
                average = result.average,
                aggregate = result.aggregateSum,
                division = result.division,
                complete = result.complete,
                missingSubjects = result.missingSubjects
            )
        }
        val subjectAnalytics = AcademicAnalytics.subjectAnalytics(
            marksByLearner = results.map { ranked -> marksByLearner[ranked.result.learnerId].orEmpty() },
            passPercent = effectivePassPercent,
            expectedLearnerCount = results.size,
            expectedSubjectCodes = expectedSubjects,
            gradingScale = effectiveScale
        )
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safe = { value: String -> value.replace(Regex("[^A-Za-z0-9_-]"), "_") }
        val file = File(dir, "${safe(schoolClass.displayName)}_${year}_${safe(term.displayName)}_${safe(section.displayName)}_Results.${format.extension}")
        when (format) {
            ExportFormat.CSV -> writeCsv(file, schoolName, schoolClass, year, term, section, rows, expectedSubjects, labels, subjectAnalytics, effectivePassPercent)
            ExportFormat.EXCEL -> writeXlsx(file, schoolName, schoolClass, year, term, section, rows, expectedSubjects, labels, subjectAnalytics, effectivePassPercent)
            ExportFormat.PDF -> writePdf(file, schoolName, schoolClass, year, term, section, rows, expectedSubjects, labels, subjectAnalytics, effectivePassPercent)
        }
        return file
    }

    private fun subjectsFor(schoolClass: SchoolClass): List<String> = SubjectCatalog.forClass(schoolClass).map { it.code }

    private fun subjectLabel(code: String): String = when (code) {
        "eng" -> "English"
        "mtc" -> "Mathematics"
        "liti" -> "Literacy I"
        "read" -> "Reading"
        "writ" -> "Writing"
        "re" -> "Religious Education"
        "lug" -> "Luganda"
        "sci" -> "Science"
        "sst" -> "Social Studies"
        else -> code.uppercase()
    }

    private fun writeCsv(file: File, schoolName: String, schoolClass: SchoolClass, year: Int, term: Term, section: TermSection, rows: List<ClassExportRow>, subjects: List<String>, labels: Map<String, String>, subjectAnalytics: List<SubjectAnalytics>, passPercent: Double) {
        val header = buildList {
            add("POSITION"); add("NAME"); subjects.forEach { add(labels[it] ?: subjectLabel(it)) }
            add("TOTAL"); add("MAX TOTAL"); add("AVERAGE %")
            if (schoolClass.mode.name == "UPPER") { add("AGGREGATE"); add("DIVISION") }
            add("STATUS")
        }
        val sb = StringBuilder()
        if (schoolName.isNotBlank()) sb.append(csv(schoolName)).append('\n')
        sb.append(csv("${schoolClass.displayName} • $year • ${term.displayName} • ${section.displayName}")).append("\n\n")
        sb.append(header.joinToString(",") { csv(it) }).append('\n')
        rows.forEach { row ->
            val values = mutableListOf<String>()
            values += row.position?.toString() ?: "-"
            values += row.learnerName
            subjects.forEach { values += row.marks[it]?.toString() ?: "" }
            values += row.total.toString(); values += row.maxTotal.toString()
            values += String.format(Locale.US, "%.1f", row.average)
            if (schoolClass.mode.name == "UPPER") { values += row.aggregate?.toString() ?: "-"; values += row.division ?: "-" }
            values += if (row.complete) "COMPLETE" else "INCOMPLETE: ${row.missingSubjects.joinToString("; ")}" 
            sb.append(values.joinToString(",") { csv(it) }).append('\n')
        }
        sb.append("\n").append(csv("SUBJECT PERFORMANCE ANALYSIS")).append('\n')
        sb.append(csv("Pass rate is calculated among learners assessed in that subject only. AMGP uses the saved grading policy and D/C/P/F weights 1/2/3/4.")).append('\n')
        sb.append(listOf("SUBJECT", "ASSESSED", "MISSING", "COVERAGE %", "AVERAGE %", "PASS RATE % (>= ${String.format(Locale.US, "%.1f", passPercent)})", "HIGHEST %", "LOWEST %", "AMGP", "D", "C", "P", "F").joinToString(",") { csv(it) }).append('\n')
        subjectAnalytics.forEach { subject ->
            val values = listOf(
                labels[subject.subjectCode] ?: subjectLabel(subject.subjectCode), subject.learnerCount.toString(), subject.missingCount.toString(),
                fmt(subject.coveragePercent), subject.averagePercent?.let(::fmt) ?: "", subject.passRatePercent?.let(::fmt) ?: "",
                subject.highestPercent?.let(::fmt) ?: "", subject.lowestPercent?.let(::fmt) ?: "", subject.amgp?.let { String.format(Locale.US, "%.2f", it) } ?: "",
                subject.distinctionCount.toString(), subject.creditCount.toString(), subject.passCount.toString(), subject.failureCount.toString()
            )
            sb.append(values.joinToString(",") { csv(it) }).append('\n')
        }
        file.writeText(sb.toString())
    }

    private fun writeXlsx(file: File, schoolName: String, schoolClass: SchoolClass, year: Int, term: Term, section: TermSection, rows: List<ClassExportRow>, subjects: List<String>, labels: Map<String, String>, subjectAnalytics: List<SubjectAnalytics>, passPercent: Double) {
        val data = mutableListOf<List<Any>>()
        if (schoolName.isNotBlank()) data += listOf(schoolName)
        data += listOf("${schoolClass.displayName} • $year • ${term.displayName} • ${section.displayName}")
        val header = mutableListOf<Any>("POSITION", "NAME")
        subjects.forEach { header += (labels[it] ?: subjectLabel(it)) }
        header += "TOTAL"; header += "MAX TOTAL"; header += "AVERAGE %"
        if (schoolClass.mode.name == "UPPER") { header += "AGGREGATE"; header += "DIVISION" }
        header += "STATUS"
        data += header
        rows.forEach { row ->
            val line = mutableListOf<Any>(row.position ?: "-", row.learnerName)
            subjects.forEach { line += row.marks[it] ?: "" }
            line += row.total; line += row.maxTotal; line += row.average
            if (schoolClass.mode.name == "UPPER") { line += row.aggregate ?: "-"; line += row.division ?: "-" }
            line += if (row.complete) "COMPLETE" else "INCOMPLETE: ${row.missingSubjects.joinToString("; ")}"
            data += line
        }
        data += emptyList<Any>()
        data += listOf("SUBJECT PERFORMANCE ANALYSIS")
        data += listOf("Pass rate is calculated among learners assessed in that subject only. AMGP uses saved grading policy and D/C/P/F weights 1/2/3/4.")
        data += listOf("SUBJECT", "ASSESSED", "MISSING", "COVERAGE %", "AVERAGE %", "PASS RATE % (>= ${String.format(Locale.US, "%.1f", passPercent)})", "HIGHEST %", "LOWEST %", "AMGP", "D", "C", "P", "F")
        subjectAnalytics.forEach { subject ->
            data += listOf(
                labels[subject.subjectCode] ?: subjectLabel(subject.subjectCode), subject.learnerCount, subject.missingCount,
                subject.coveragePercent, subject.averagePercent ?: "—", subject.passRatePercent ?: "—",
                subject.highestPercent ?: "—", subject.lowestPercent ?: "—", subject.amgp ?: "—",
                subject.distinctionCount, subject.creditCount, subject.passCount, subject.failureCount
            )
        }
        FileOutputStream(file).use { fos -> ZipOutputStream(fos).use { zip ->
            entry(zip, "[Content_Types].xml", CONTENT_TYPES)
            entry(zip, "_rels/.rels", RELS)
            entry(zip, "xl/workbook.xml", WORKBOOK)
            entry(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS)
            entry(zip, "xl/styles.xml", STYLES)
            entry(zip, "xl/worksheets/sheet1.xml", sheetXml(data))
        }}
    }

    private fun writePdf(file: File, schoolName: String, schoolClass: SchoolClass, year: Int, term: Term, section: TermSection, rows: List<ClassExportRow>, subjects: List<String>, labels: Map<String, String>, subjectAnalytics: List<SubjectAnalytics>, passPercent: Double) {
        val document = PdfDocument()
        val pageWidth = 842; val pageHeight = 595
        val navy = 0xFF17324D.toInt(); val teal = 0xFF147D82.toInt()
        val ink = 0xFF253746.toInt(); val muted = 0xFF647786.toInt()
        fun p(size: Float, color: Int = ink, bold: Boolean = false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size; this.color = color
            typeface = if (bold) android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD) else android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }
        fun clipped(value: String, paint: Paint, max: Float): String {
            var result = value
            while (result.isNotEmpty() && paint.measureText(result) > max) result = result.dropLast(1)
            return if (result == value) value else result.trimEnd() + "…"
        }
        val body = p(8.5f); val headerText = p(7.2f, 0xFFFFFFFF.toInt(), true)
        val title = p(17f, navy, true); val subtitle = p(9f, muted)
        val left = 28f; val right = 814f
        val isUpper = schoolClass.mode.name == "UPPER"
        val learnerWidth = 145f
        // Derive the width from the actual columns drawn below. A hard minimum width
        // can silently push the last subject/aggregate/division beyond the page when
        // schools configure many subjects. Clipping labels keeps narrow columns legible
        // enough for marks while ensuring the table remains inside the page bounds.
        val learnerEndX = left + 8f + 30f + (learnerWidth - 30f)
        val resultColumnsWidth = 47f + 42f + if (isUpper) 60f else 0f
        val subjectSpace = (right - learnerEndX - resultColumnsWidth - 8f).coerceAtLeast(1f)
        val actualSubjectWidth = (subjectSpace / subjects.size.coerceAtLeast(1)).coerceAtMost(54f)
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = 0f
        fun header() {
            canvas.drawColor(0xFFFFFFFF.toInt())
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 9f, Paint().apply { color = teal })
            y = 34f
            val school = clipped(schoolName.ifBlank { "SCHOOL NAME" }, title, 500f)
            canvas.drawText(school, left, y, title); y += 18f
            canvas.drawText("CLASS RESULTS REGISTER", left, y, p(9f, teal, true))
            canvas.drawText("${schoolClass.displayName}  •  $year  •  ${term.displayName}  •  ${section.displayName}", 330f, y, subtitle)
            y += 18f
            val completeCount = rows.count { it.complete }
            val avg = if (completeCount == 0) 0.0 else rows.filter { it.complete }.map { it.average }.average()
            val summary = "Learners: ${rows.size}     Complete: $completeCount     Incomplete: ${rows.size - completeCount}     Class average: ${String.format(Locale.US, "%.1f%%", avg)}"
            canvas.drawRoundRect(28f, y - 10f, 814f, y + 10f, 5f, 5f, Paint().apply { color = 0xFFEAF3F5.toInt() })
            canvas.drawText(summary, 38f, y + 3f, p(8f, navy, true)); y += 26f
            canvas.drawRoundRect(left, y - 13f, right, y + 6f, 4f, 4f, Paint().apply { color = navy })
            var x = left + 8f
            canvas.drawText("POS", x, y, headerText); x += 30f
            canvas.drawText("LEARNER", x, y, headerText); x += learnerWidth - 30f
            subjects.forEach { code ->
                val label = labels[code] ?: subjectLabel(code)
                canvas.drawText(clipped(label, headerText, actualSubjectWidth - 4f), x + 2f, y, headerText)
                x += actualSubjectWidth
            }
            canvas.drawText("TOTAL", x + 2f, y, headerText); x += 47f
            canvas.drawText("AVG %", x + 2f, y, headerText); x += 42f
            if (isUpper) { canvas.drawText("AGG", x + 2f, y, headerText); x += 30f; canvas.drawText("DIV", x + 2f, y, headerText) }
            y += 20f
        }
        header()
        rows.forEachIndexed { index, row ->
            // Incomplete rows use an extra disclosure line; reserve its space before
            // drawing so the disclosure never collides with the footer or gets clipped.
            val requiredRowSpace = if (row.complete) 32f else 42f
            if (y > pageHeight - requiredRowSpace) {
                canvas.drawText("RVH EDU  •  Class results", left, pageHeight - 12f, p(7f, muted))
                canvas.drawText("Page $pageNumber", 770f, pageHeight - 12f, p(7f, muted))
                document.finishPage(page); pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas; header()
            }
            if (index % 2 == 0) canvas.drawRect(left, y - 10f, right, y + 5f, Paint().apply { color = 0xFFF4F7F9.toInt() })
            var x = left + 8f
            canvas.drawText(row.position?.toString() ?: "—", x, y, p(8f, if (row.complete) ink else muted, row.position != null)); x += 30f
            canvas.drawText(clipped(row.learnerName, body, learnerWidth - 32f), x, y, body); x += learnerWidth - 30f
            subjects.forEach { code ->
                val mark = row.marks[code]?.toString() ?: "—"
                canvas.drawText(mark, x + 2f, y, p(8f, if (mark == "—") muted else ink))
                x += actualSubjectWidth
            }
            canvas.drawText("${row.total}/${row.maxTotal}", x + 2f, y, p(7.5f, ink, true)); x += 47f
            canvas.drawText(String.format(Locale.US, "%.1f", row.average), x + 2f, y, p(8f, teal, true)); x += 42f
            if (isUpper) { canvas.drawText(row.aggregate?.toString() ?: "—", x + 2f, y, body); x += 30f; canvas.drawText(row.division ?: "—", x + 2f, y, body) }
            if (!row.complete) {
                canvas.drawText("Incomplete: ${row.missingSubjects.joinToString(", ")}", left + 175f, y + 11f, p(6.5f, 0xFF9B5B20.toInt()))
                y += 10f
            }
            y += 16f
        }
        canvas.drawText("RVH EDU  •  OFFLINE SCHOOL ACADEMIC SYSTEM", left, pageHeight - 12f, p(7f, muted))
        canvas.drawText("Page $pageNumber", 770f, pageHeight - 12f, p(7f, muted))
        document.finishPage(page)
        // A dedicated appendix keeps the learner register readable even when schools
        // configure many subjects. Chunking also prevents subject tables from overflowing
        // the page when a school has an unusually large custom subject catalogue.
        val analysisPages: List<List<SubjectAnalytics>> = if (subjectAnalytics.isEmpty()) listOf(emptyList()) else subjectAnalytics.chunked(22)
        analysisPages.forEach { analysisChunk ->
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            canvas.drawColor(0xFFFFFFFF.toInt())
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 9f, Paint().apply { color = teal })
            var sy = 36f
            canvas.drawText(clipped(schoolName.ifBlank { "SCHOOL NAME" }, title, 500f), left, sy, title); sy += 19f
            canvas.drawText("SUBJECT PERFORMANCE ANALYSIS", left, sy, p(10f, teal, true))
            canvas.drawText("${schoolClass.displayName}  •  $year  •  ${term.displayName}  •  ${section.displayName}", 330f, sy, subtitle); sy += 18f
            canvas.drawRoundRect(left, sy - 10f, right, sy + 10f, 5f, 5f, Paint().apply { color = 0xFFEAF3F5.toInt() })
            canvas.drawText("Pass threshold: ${String.format(Locale.US, "%.1f", passPercent)}%  •  Pass rate uses assessed learners only  •  AMGP = (D×1 + C×2 + P×3 + F×4) / assessed grades", left + 9f, sy + 3f, p(7.5f, navy, true)); sy += 28f
            canvas.drawRoundRect(left, sy - 13f, right, sy + 6f, 4f, 4f, Paint().apply { color = navy })
            val cols = listOf("SUBJECT", "ASSESSED", "MISSING", "COVERAGE", "AVERAGE", "PASS RATE", "HIGH", "LOW", "AMGP", "D", "C", "P", "F")
            val widths = listOf(142f, 48f, 48f, 58f, 58f, 62f, 48f, 48f, 45f, 28f, 28f, 28f, 28f)
            var sx = left + 7f
            cols.forEachIndexed { i, label -> canvas.drawText(label, sx + 1f, sy, p(6.4f, 0xFFFFFFFF.toInt(), true)); sx += widths[i] }
            sy += 20f
            analysisChunk.forEachIndexed { index, subject ->
                if (index % 2 == 0) canvas.drawRect(left, sy - 10f, right, sy + 5f, Paint().apply { color = 0xFFF4F7F9.toInt() })
                val values = listOf(
                    labels[subject.subjectCode] ?: subjectLabel(subject.subjectCode), subject.learnerCount.toString(), subject.missingCount.toString(),
                    "${fmt(subject.coveragePercent)}%", subject.averagePercent?.let { "${fmt(it)}%" } ?: "—", subject.passRatePercent?.let { "${fmt(it)}%" } ?: "—",
                    subject.highestPercent?.let(::fmt) ?: "—", subject.lowestPercent?.let(::fmt) ?: "—", subject.amgp?.let { String.format(Locale.US, "%.2f", it) } ?: "—",
                    subject.distinctionCount.toString(), subject.creditCount.toString(), subject.passCount.toString(), subject.failureCount.toString()
                )
                sx = left + 7f
                values.forEachIndexed { i, value -> canvas.drawText(clipped(value, p(7f), widths[i] - 3f), sx + 1f, sy, p(7f, if (value == "—") muted else ink, i == 0)); sx += widths[i] }
                sy += 18f
            }
            sy += 12f
            canvas.drawText("Grade distribution: D = distinction, C = credit, P = pass, F = failure. Missing marks are excluded from averages, pass rates and AMGP.", left, sy, p(7.5f, muted))
            canvas.drawText("RVH EDU  •  OFFLINE SCHOOL ACADEMIC SYSTEM", left, pageHeight - 12f, p(7f, muted))
            canvas.drawText("Page $pageNumber", 770f, pageHeight - 12f, p(7f, muted))
            document.finishPage(page)
        }
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
    }

    private fun fmt(value: Double): String = String.format(Locale.US, "%.1f", value)
    private fun csv(value: String): String = CsvCellEncoder.encode(value)
    private fun entry(zip: ZipOutputStream, name: String, content: String) { zip.putNextEntry(ZipEntry(name)); zip.write(content.toByteArray()); zip.closeEntry() }
    private fun col(index: Int): String { var n = index + 1; val s = StringBuilder(); while (n > 0) { val r = (n - 1) % 26; s.insert(0, ('A'.code + r).toChar()); n = (n - 1) / 26 }; return s.toString() }
    private fun xml(v: String) = v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    private fun sheetXml(rows: List<List<Any>>): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>")
        rows.forEachIndexed { ri, row -> append("<row r=\"${ri + 1}\">"); row.forEachIndexed { ci, value -> val ref = "${col(ci)}${ri + 1}"; if (value is Number) append("<c r=\"$ref\"><v>$value</v></c>") else append("<c r=\"$ref\" t=\"inlineStr\"><is><t>${xml(value.toString())}</t></is></c>") }; append("</row>") }
        append("</sheetData></worksheet>")
    }
    private const val CONTENT_TYPES = "<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/><Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/></Types>"
    private const val RELS = "<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>"
    private const val WORKBOOK = "<?xml version=\"1.0\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Results\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>"
    private const val WORKBOOK_RELS = "<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/></Relationships>"
    private const val STYLES = "<?xml version=\"1.0\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts><fills count=\"1\"><fill><patternFill patternType=\"none\"/></fill></fills><borders count=\"1\"><border/></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs></styleSheet>"
}
