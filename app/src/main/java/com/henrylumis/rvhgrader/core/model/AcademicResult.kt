package com.henrylumis.rvhgrader.core.model

/** Pure academic result produced by the RVH Edu academic engine. */
data class AcademicResult(
    val learnerId: String,
    val assessmentId: String,
    val total: Int,
    val maxTotal: Int,
    val average: Double,
    val aggregateSum: Int?,
    val division: String?,
    val graded: Boolean,
    val complete: Boolean,
    val missingSubjects: List<String>
)
