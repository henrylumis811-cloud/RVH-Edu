package com.henrylumis.rvhgrader.core.assessment

import com.henrylumis.rvhgrader.core.grading.RankingEngine
import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.RankedResult

/** Produces a complete class result set from already-calculated learner results. */
object ClassAssessmentEngine {
    data class Summary(
        val totalLearners: Int,
        val completeLearners: Int,
        val incompleteLearners: Int,
        val classAverage: Double,
        val rankedResults: List<RankedResult>
    )

    fun summarize(results: List<AcademicResult>): Summary {
        val complete = results.filter { it.complete }
        val average = if (complete.isEmpty()) 0.0 else complete.map { it.average }.average()
        return Summary(
            totalLearners = results.size,
            completeLearners = complete.size,
            incompleteLearners = results.size - complete.size,
            classAverage = ((average * 100.0).toInt() / 100.0),
            rankedResults = RankingEngine.rank(results)
        )
    }
}
