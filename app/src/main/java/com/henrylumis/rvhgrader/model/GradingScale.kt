package com.henrylumis.rvhgrader.model

/** One band of the mark->aggregate table, e.g. "90 and above -> aggregate 1". */
data class AggregateBand(val minMark: Int, val aggregate: Int)

/** One band of the aggregate-sum->division table, e.g. "up to 12 -> Division I". */
data class DivisionBand(val maxAggSum: Int, val label: String)

/**
 * Editable version of the scoring rules that used to be hardcoded. A teacher can adjust either
 * table from Settings; anything they don't touch keeps the original UNEB-style defaults this
 * app shipped with.
 */
data class GradingScale(
    val aggregateBands: List<AggregateBand>,
    val divisionBands: List<DivisionBand>
) {
    fun aggregateFor(score: Int): Int {
        val sorted = aggregateBands.sortedByDescending { it.minMark }
        return sorted.firstOrNull { score >= it.minMark }?.aggregate
            ?: sorted.lastOrNull()?.aggregate
            ?: 9
    }

    fun divisionFor(aggSum: Int, grossSum: Int): String {
        if (grossSum == 0) return "U"
        val sorted = divisionBands.sortedBy { it.maxAggSum }
        return sorted.firstOrNull { aggSum <= it.maxAggSum }?.label ?: "U"
    }

    /** Reject ambiguous school-defined grading tables before they are persisted. */
    fun validationError(): String? {
        if (aggregateBands.isEmpty()) return "Add at least one mark-to-aggregate band."
        if (divisionBands.isEmpty()) return "Add at least one aggregate-to-division band."
        if (aggregateBands.any { it.minMark !in 0..100 }) return "Minimum marks must be between 0 and 100."
        if (aggregateBands.any { it.aggregate !in 1..99 }) return "Aggregate values must be between 1 and 99."
        if (aggregateBands.map { it.minMark }.distinct().size != aggregateBands.size) {
            return "Each mark threshold must be unique."
        }
        if (aggregateBands.none { it.minMark == 0 }) return "Add a 0 minimum-mark band so every score has a defined aggregate."
        if (divisionBands.any { it.maxAggSum <= 0 }) return "Division limits must be greater than zero."
        if (divisionBands.any { it.label.isBlank() }) return "Every division needs a label."
        if (divisionBands.map { it.maxAggSum }.distinct().size != divisionBands.size) {
            return "Each division limit must be unique."
        }
        if (divisionBands.map { it.label.trim().uppercase() }.distinct().size != divisionBands.size) {
            return "Division labels must be unique."
        }
        return null
    }

    companion object {
        fun default(): GradingScale = GradingScale(
            aggregateBands = listOf(
                AggregateBand(90, 1),
                AggregateBand(80, 2),
                AggregateBand(70, 3),
                AggregateBand(60, 4),
                AggregateBand(50, 5),
                AggregateBand(45, 6),
                AggregateBand(40, 7),
                AggregateBand(35, 8),
                AggregateBand(0, 9)
            ),
            divisionBands = listOf(
                DivisionBand(12, "I"),
                DivisionBand(24, "II"),
                DivisionBand(28, "III"),
                DivisionBand(32, "IV")
            )
        )
    }
}
