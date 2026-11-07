package com.henrylumis.rvhgrader.core.analytics

import com.henrylumis.rvhgrader.core.model.RankedResult
import com.henrylumis.rvhgrader.model.GradingScale

/** Aggregate view of one class assessment. No UI or database dependencies. */
data class ClassAnalyticsSummary(
    val learnerCount: Int,
    val completeCount: Int,
    val incompleteCount: Int,
    val averagePercent: Double?,
    val highestPercent: Double?,
    val lowestPercent: Double?,
    val passRatePercent: Double?,
    val subjectAnalytics: List<SubjectAnalytics>,
    val riskFlags: List<RiskFlag>
)

object ClassAnalyticsCalculator {
    fun summarize(
        results: List<RankedResult>,
        marksByLearner: Map<String, List<com.henrylumis.rvhgrader.core.model.Mark>> = emptyMap(),
        progress: List<LearnerProgress> = emptyList(),
        passPercent: Double = 50.0,
        thresholds: AcademicAnalytics.Thresholds = AcademicAnalytics.Thresholds(),
        expectedSubjectCodes: Collection<String> = emptyList(),
        gradingScale: GradingScale = GradingScale.default()
    ): ClassAnalyticsSummary {
        require(passPercent.isFinite() && passPercent in 0.0..100.0) {
            "Class pass threshold must be between 0 and 100 percent"
        }
        val complete = results.filter { it.result.complete }
        val averages = complete.map { it.result.average }
        val passed = averages.count { it >= passPercent }
        val subjects = AcademicAnalytics.subjectAnalytics(
            marksByLearner.values.toList(),
            passPercent = passPercent,
            expectedLearnerCount = results.size,
            expectedSubjectCodes = expectedSubjectCodes,
            gradingScale = gradingScale
        )
        val risks = AcademicAnalytics.riskFlags(progress, thresholds)
        return ClassAnalyticsSummary(
            learnerCount = results.size,
            completeCount = complete.size,
            incompleteCount = results.size - complete.size,
            averagePercent = averages.averageOrNull()?.round2(),
            highestPercent = averages.maxOrNull()?.round2(),
            lowestPercent = averages.minOrNull()?.round2(),
            passRatePercent = if (complete.isEmpty()) null else (passed * 100.0 / complete.size).round2(),
            subjectAnalytics = subjects,
            riskFlags = risks
        )
    }

    private fun Iterable<Double>.averageOrNull(): Double? {
        val values = toList()
        return if (values.isEmpty()) null else values.average()
    }

    private fun Double.round2(): Double = kotlin.math.round(this * 100.0) / 100.0
}
