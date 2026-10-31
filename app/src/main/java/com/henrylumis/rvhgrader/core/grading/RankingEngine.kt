package com.henrylumis.rvhgrader.core.grading

import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.RankedResult

/**
 * Ranking rules are deliberately separate from persistence and UI.
 * Incomplete results are never assigned a class position: a missing mark must not silently
 * create a misleading ranking. Use [rankIncludingIncomplete] when a screen needs every learner.
 */
object RankingEngine {
    /** Ranks complete results only. Ties share competition positions (1, 1, 3). */
    fun rank(results: List<AcademicResult>): List<RankedResult> {
        validateRankingInput(results)
        return rankComplete(results.filter { it.complete })
    }

    /** Returns every learner, but leaves incomplete results explicitly unranked at position 0. */
    fun rankIncludingIncomplete(results: List<AcademicResult>): List<RankedResult> {
        validateRankingInput(results)
        val complete = results.filter { it.complete }
        val incomplete = results.filterNot { it.complete }
        return rankComplete(complete) + incomplete.map { RankedResult(it, 0) }
    }

    private fun validateRankingInput(results: List<AcademicResult>) {
        require(results.map { it.learnerId }.distinct().size == results.size) {
            "Ranking input contains duplicate learner results"
        }
        if (results.isEmpty()) return
        require(results.all { it.learnerId.isNotBlank() && it.assessmentId.isNotBlank() }) {
            "Ranking results must have learner and assessment IDs"
        }
        require(results.map { it.assessmentId }.distinct().size == 1) {
            "Ranking results must belong to the same assessment"
        }
        require(results.all { it.maxTotal > 0 && it.total in 0..it.maxTotal }) {
            "Ranking results must have valid totals and positive maximum totals"
        }
        require(results.all { it.average.isFinite() && it.average in 0.0..100.0 }) {
            "Ranking results must have a finite average between 0 and 100"
        }
        require(results.all { it.complete == it.missingSubjects.isEmpty() }) {
            "Ranking completeness must agree with the missing-subject list"
        }
    }

    private fun rankComplete(results: List<AcademicResult>): List<RankedResult> {
        // Compare normalized scores as exact fractions. Floating-point division can give
        // mathematically equal percentages slightly different representations, breaking ties.
        // Inputs are validated Int totals, so Long cross-products safely cover the Int range.
        val ordered = results.sortedWith { left, right ->
            val leftScaled = left.total.toLong() * right.maxTotal.toLong()
            val rightScaled = right.total.toLong() * left.maxTotal.toLong()
            val scoreOrder = rightScaled.compareTo(leftScaled) // descending percentage
            if (scoreOrder != 0) scoreOrder else left.learnerId.compareTo(right.learnerId)
        }

        var previous: AcademicResult? = null
        var position = 0
        return ordered.mapIndexed { index, result ->
            val prior = previous
            val tied = prior != null &&
                result.total.toLong() * prior.maxTotal.toLong() ==
                prior.total.toLong() * result.maxTotal.toLong()
            if (!tied) position = index + 1
            previous = result
            RankedResult(result, position)
        }
    }
}
