package com.henrylumis.rvhgrader.core.importer

import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.model.SchoolClass
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object AcademicImportParser {
    data class Row(val name: String, val marks: Map<String, Int?>)
    data class ParsedSheet(val rows: List<Row>, val warnings: List<String>)

    fun parseCsv(text: String, schoolClass: SchoolClass): ParsedSheet {
        val records = csvRecords(text)
        if (records.isEmpty()) return ParsedSheet(emptyList(), listOf("The CSV file is empty."))
        val headerIndex = records.indexOfFirst { it.any { normalizeHeader(it) == "NAME" } }
        if (headerIndex < 0) return ParsedSheet(emptyList(), listOf("Could not find a NAME column."))
        return parseRows(records[headerIndex], records.drop(headerIndex + 1), schoolClass)
    }

    fun parseXlsx(input: InputStream, schoolClass: SchoolClass): ParsedSheet {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) entries[entry.name] = zip.readBytes()
            }
        }
        val workbook = entries["xl/workbook.xml"] ?: return ParsedSheet(emptyList(), listOf("Invalid XLSX: workbook.xml is missing."))
        val rels = entries["xl/_rels/workbook.xml.rels"]
        val sheetPath = findFirstSheetPath(workbook.toString(Charsets.UTF_8), rels?.toString(Charsets.UTF_8)) ?: "xl/worksheets/sheet1.xml"
        val sheet = entries[sheetPath] ?: return ParsedSheet(emptyList(), listOf("Invalid XLSX: worksheet is missing."))
        val sharedStrings = entries["xl/sharedStrings.xml"]?.let { parseSharedStrings(it.toString(Charsets.UTF_8)) }.orEmpty()
        val rows = parseSheetRows(sheet.toString(Charsets.UTF_8), sharedStrings)
        if (rows.isEmpty()) return ParsedSheet(emptyList(), listOf("The Excel sheet is empty."))
        val headerIndex = rows.indexOfFirst { it.values.any { normalizeHeader(it) == "NAME" } }
        if (headerIndex < 0) return ParsedSheet(emptyList(), listOf("Could not find a NAME column."))
        return parseRows(rows[headerIndex].values, rows.drop(headerIndex + 1).map { it.values }, schoolClass)
    }

    private fun parseRows(header: List<String>, data: List<List<String>>, schoolClass: SchoolClass): ParsedSheet {
        val subjectCodes = SubjectCatalog.forClass(schoolClass).map { it.code }
        val warnings = mutableListOf<String>()
        val rawColumns = header.mapIndexedNotNull { index, value -> mapHeader(value)?.let { index to it } }
        val headerCounts = rawColumns.groupingBy { it.second }.eachCount()
        headerCounts.filter { it.key != "__name__" && it.value > 1 }.forEach { (code, count) ->
            warnings += "Duplicate subject header '$code' appears $count times. Only the first column will be used."
        }
        val columns = rawColumns.distinctBy { it.second }
        val nameColumns = rawColumns.filter { it.second == "__name__" }
        if (nameColumns.size > 1) warnings += "Multiple NAME columns found. Only the first NAME column will be used."
        val nameColumn = columns.firstOrNull { it.second == "__name__" }?.first
            ?: return ParsedSheet(emptyList(), warnings + "Could not map the NAME column.")
        val rows = mutableListOf<Row>()
        data.forEachIndexed { rowIndex, cells ->
            val name = cells.getOrNull(nameColumn)?.trim().orEmpty()
            if (name.isBlank()) return@forEachIndexed
            val marks = linkedMapOf<String, Int?>()
            columns.filter { it.second != "__name__" }.forEach { (index, code) ->
                if (code !in subjectCodes) return@forEach
                val raw = cells.getOrNull(index)?.trim().orEmpty()
                if (raw.isBlank()) marks[code] = null
                else raw.toIntOrNull()?.let { value ->
                    if (value in 0..100) marks[code] = value
                    else { warnings += "Row ${rowIndex + 1}: '$name' has out-of-range $code mark '$value'."; marks[code] = null }
                } ?: run { warnings += "Row ${rowIndex + 1}: '$name' has invalid $code value '$raw'."; marks[code] = null }
            }
            if (marks.values.none { it != null }) warnings += "Row ${rowIndex + 1}: '$name' contains no readable subject marks."
            rows += Row(name, marks)
        }
        return ParsedSheet(rows, warnings)
    }

    private fun mapHeader(value: String): String? = when (normalizeHeader(value)) {
        "NAME" -> "__name__"
        "ENGLISH" -> "eng"
        "MATHEMATICS", "MATH" -> "mtc"
        "LITERACY I", "LITERACY 1" -> "liti"
        "READING" -> "read"
        "WRITING" -> "writ"
        "RELIGIOUS EDUCATION", "RE" -> "re"
        "LUGANDA" -> "lug"
        "SCIENCE" -> "sci"
        "SOCIAL STUDIES", "SST" -> "sst"
        else -> value.trim().lowercase().takeIf { it in setOf("eng","mtc","liti","read","writ","re","lug","sci","sst") }
    }

    private fun normalizeHeader(value: String): String = value.trim().replace(Regex("\\s+"), " ").uppercase()

    private fun csvRecords(text: String): List<List<String>> {
        val result = mutableListOf<List<String>>(); val row = mutableListOf<String>(); val field = StringBuilder(); var quoted = false; var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c == '"' && quoted && i + 1 < text.length && text[i + 1] == '"' -> { field.append('"'); i++ }
                c == '"' -> quoted = !quoted
                c == ',' && !quoted -> { row += field.toString(); field.setLength(0) }
                (c == '\n' || c == '\r') && !quoted -> { if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++; row += field.toString(); field.setLength(0); if (row.any { it.isNotBlank() }) result += row.toList(); row.clear() }
                else -> field.append(c)
            }; i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) { row += field.toString(); if (row.any { it.isNotBlank() }) result += row.toList() }
        return result
    }

    private data class SheetRow(val values: List<String>)

    private fun parseSheetRows(xml: String, shared: List<String>): List<SheetRow> {
        val doc = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder().parse(xml.byteInputStream())
        val rows = doc.getElementsByTagNameNS("*", "row")
        return buildList {
            for (r in 0 until rows.length) {
                val cells = (rows.item(r) as org.w3c.dom.Element).getElementsByTagNameNS("*", "c")
                val values = mutableListOf<String>(); var nextCol = 0
                for (i in 0 until cells.length) {
                    val cell = cells.item(i) as org.w3c.dom.Element; val index = columnIndex(cell.getAttribute("r").takeWhile { it.isLetter() })
                    while (nextCol < index) { values += ""; nextCol++ }
                    val type = cell.getAttribute("t")
                    val value = when (type) {
                        "inlineStr" -> cell.getElementsByTagNameNS("*", "t").item(0)?.textContent.orEmpty()
                        "s" -> cell.getElementsByTagNameNS("*", "v").item(0)?.textContent?.toIntOrNull()?.let { shared.getOrNull(it) }.orEmpty()
                        else -> cell.getElementsByTagNameNS("*", "v").item(0)?.textContent.orEmpty()
                    }
                    values += value; nextCol = index + 1
                }
                add(SheetRow(values))
            }
        }
    }

    private fun parseSharedStrings(xml: String): List<String> {
        val doc = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder().parse(xml.byteInputStream())
        val items = doc.getElementsByTagNameNS("*", "si")
        return buildList { for (i in 0 until items.length) { val nodes = (items.item(i) as org.w3c.dom.Element).getElementsByTagNameNS("*", "t"); add((0 until nodes.length).joinToString("") { j -> nodes.item(j).textContent }) } }
    }

    private fun findFirstSheetPath(workbook: String, rels: String?): String? {
        val relMap = Regex("<Relationship[^>]*Id=\"([^\"]+)\"[^>]*Target=\"([^\"]+)\"").findAll(rels.orEmpty()).associate { it.groupValues[1] to it.groupValues[2] }
        val rid = Regex("<sheet[^>]*r:id=\"([^\"]+)\"").find(workbook)?.groupValues?.get(1) ?: return null
        val target = relMap[rid] ?: return null
        return if (target.startsWith("/")) target.removePrefix("/") else "xl/$target".replace("xl/xl/", "xl/")
    }

    private fun columnIndex(col: String): Int { var n = 0; for (c in col.uppercase()) n = n * 26 + (c.code - 'A'.code + 1); return n - 1 }
}
