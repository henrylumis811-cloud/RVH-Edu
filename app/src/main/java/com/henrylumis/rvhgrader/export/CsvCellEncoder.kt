package com.henrylumis.rvhgrader.export

/** CSV quoting plus spreadsheet-formula neutralization for untrusted text fields. */
object CsvCellEncoder {
    fun encode(value: String): String {
        val trimmed = value.trimStart()
        val first = trimmed.firstOrNull()
        val formulaCandidate = first == '=' || first == '+' || first == '@' || first == '\t' || first == '\r' ||
            (first == '-' && trimmed != "-" && trimmed.toDoubleOrNull() == null)
        val safeValue = if (formulaCandidate) "'$value" else value
        return if (safeValue.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + safeValue.replace("\"", "\"\"") + "\""
        } else safeValue
    }
}
