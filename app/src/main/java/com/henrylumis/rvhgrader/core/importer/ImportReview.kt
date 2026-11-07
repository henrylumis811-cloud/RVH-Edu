package com.henrylumis.rvhgrader.core.importer

import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.model.Learner

/** Review-only representation. Nothing here is persisted until the teacher confirms import. */
data class ImportReviewRow(
    val id: Int,
    val name: String,
    val marks: Map<String, Int?>,
    val candidates: List<Learner>,
    val included: Boolean = true,
    val warning: String? = null
)

object ImportReviewBuilder {
    fun build(
        sheet: AcademicImportParser.ParsedSheet,
        learners: List<Learner>
    ): List<ImportReviewRow> {
        val byName = learners.groupBy { normalize(it.fullName) }
        return sheet.rows.mapIndexed { index, row ->
            val normalized = normalize(row.name)
            val candidates = byName[normalized].orEmpty()
            val nearMatches = if (candidates.isEmpty()) {
                learners.mapNotNull { learner ->
                    val distance = levenshtein(normalized, normalize(learner.fullName))
                    val limit = maxOf(2, normalized.length / 5)
                    learner.takeIf { distance in 1..limit }
                }.take(3)
            } else emptyList()
            val warning = when {
                candidates.size > 1 -> "Multiple learners have this name. Review before importing."
                row.marks.values.none { it != null } -> "No readable marks."
                nearMatches.isNotEmpty() -> "No exact match. Similar existing name: ${nearMatches.joinToString { it.fullName }}. Verify before creating a new learner."
                else -> null
            }
            ImportReviewRow(index, row.name, row.marks, candidates, warning = warning)
        }
    }

    fun normalize(value: String) = value.trim().replace(Regex("\\s+"), " ").uppercase()

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var previous = IntArray(b.length + 1) { it }
        for (i in a.indices) {
            val current = IntArray(b.length + 1)
            current[0] = i + 1
            for (j in b.indices) {
                current[j + 1] = minOf(
                    current[j] + 1,
                    previous[j + 1] + 1,
                    previous[j] + if (a[i] == b[j]) 0 else 1
                )
            }
            previous = current
        }
        return previous[b.length]
    }
}
