package com.henrylumis.rvhgrader.core.analytics

import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.model.GradingScale
import kotlin.math.round
import kotlin.math.roundToInt

/** Immutable snapshot used by analytics. It is intentionally independent of Compose/UI/database. */
data class AssessmentSnapshot(
    val learnerId: String,
    val academicYear: Int,
    val termOrder: Int,
    val sectionOrder: Int,
    val assessmentLabel: String,
    val result: AcademicResult,
    val marks: List<Mark>
)

data class LearnerProgress(
    val learnerId: String,
    val assessments: List<AssessmentSnapshot>,
    val latestAverage: Double?,
    val previousAverage: Double?,
    val changePoints: Double?,
    val changePercent: Double?,
    val strongestSubject: String?,
    val weakestSubject: String?,
    val improving: Boolean,
    val declining: Boolean
)

data class SubjectAnalytics(
    val subjectCode: String,
    /** Number of learners with a recorded mark for this subject. */
    val learnerCount: Int,
    /** Number of learners in the class without a recorded mark for this subject. */
    val missingCount: Int,
    /** Percentage of the class with a recorded mark for this subject. */
    val coveragePercent: Double,
    /** Average among assessed learners only; missing marks are never treated as zero. */
    /** Null when no learner has a mark for this subject. */
    val averagePercent: Double?,
    /** Null when no learner has a mark for this subject. */
    val highestPercent: Double?,
    /** Null when no learner has a mark for this subject. */
    val lowestPercent: Double?,
    /** Pass rate among assessed learners only, not among the entire class; null when none assessed. */
    val passRatePercent: Double?,
    /** Average Mean Grade Point using Uganda primary D/C/P/F weights (1/2/3/4). */
    val amgp: Double?,
    val distinctionCount: Int,
    val creditCount: Int,
    val passCount: Int,
    val failureCount: Int
)

enum class RiskLevel { NONE, WATCH, HIGH }

data class RiskFlag(
    val learnerId: String,
    val level: RiskLevel,
    val reasons: List<String>
)

/**
 * Academic intelligence based only on persisted/calculated academic facts.
 * Thresholds are deliberately explicit so schools can later configure them.
 */
object AcademicAnalytics {
    data class Thresholds(
        val lowAveragePercent: Double = 50.0,
        val weakSubjectPercent: Double = 40.0,
        val declinePoints: Double = 10.0,
        val highDeclinePoints: Double = 20.0
    )

    fun learnerProgress(snapshots: List<AssessmentSnapshot>): List<LearnerProgress> =
        snapshots.groupBy { it.learnerId }.values.mapNotNull { history ->
            val ordered = history.sortedWith(compareBy<AssessmentSnapshot> { it.academicYear }
                .thenBy { it.termOrder }.thenBy { it.sectionOrder })
                .filter { it.result.complete }
            if (ordered.isEmpty()) return@mapNotNull null
            val latest = ordered.last()
            val previous = ordered.dropLast(1).lastOrNull()
            val delta = previous?.let { round2(latest.result.average - it.result.average) }
            val changePercent = previous?.result?.average?.takeIf { it != 0.0 }?.let {
                round2(((latest.result.average - it) / it) * 100.0)
            }
            val normalizedSubjects = latest.marks.associate { it.subjectCode to normalize(it) }
            val strongest = normalizedSubjects.maxByOrNull { it.value }?.key
            val weakest = normalizedSubjects.minByOrNull { it.value }?.key
            LearnerProgress(
                learnerId = latest.learnerId,
                assessments = ordered,
                latestAverage = round2(latest.result.average),
                previousAverage = previous?.result?.average?.let(::round2),
                changePoints = delta,
                changePercent = changePercent,
                strongestSubject = strongest,
                weakestSubject = weakest,
                improving = delta != null && delta > 0.0,
                declining = delta != null && delta < 0.0
            )
        }.sortedBy { it.learnerId }

    fun subjectAnalytics(
        marksByLearner: List<List<Mark>>,
        passPercent: Double = 50.0,
        expectedLearnerCount: Int = marksByLearner.size,
        expectedSubjectCodes: Collection<String> = emptyList(),
        gradingScale: GradingScale = GradingScale.default()
    ): List<SubjectAnalytics> {
        require(passPercent.isFinite() && passPercent in 0.0..100.0) {
            "Subject pass threshold must be between 0 and 100 percent"
        }
        require(expectedLearnerCount >= marksByLearner.size) {
            "Expected class size cannot be smaller than the number of learner mark lists"
        }
        marksByLearner.forEachIndexed { learnerIndex, learnerMarks ->
            require(learnerMarks.map { it.subjectCode }.distinct().size == learnerMarks.size) {
                "Learner mark list $learnerIndex contains duplicate subject marks"
            }
        }
        val marks = marksByLearner.flatten()
        val bySubject = marks.groupBy { it.subjectCode }
        // Include catalogued subjects even when nobody has entered a mark yet.
        val allSubjectCodes = (expectedSubjectCodes + bySubject.keys).distinct().sorted()
        return allSubjectCodes.map { subject ->
            val subjectMarks = bySubject[subject].orEmpty()
            val percentages = subjectMarks.map(::normalize)
            val passCount = percentages.count { it >= passPercent }
            val assessedCount = percentages.size
            // Ugandan primary grade bands are based on the configured mark-to-aggregate
            // settings: aggregates 1-2 = D, 3-6 = C, 7-8 = P, 9 = F.
            // A custom aggregate outside the standard 1..8 passing bands is treated as F.
            val categoryCounts = subjectMarks.map { mark ->
                val normalizedMark = normalize(mark).roundToInt().coerceIn(0, 100)
                when (gradingScale.aggregateFor(normalizedMark)) {
                    in 1..2 -> 1
                    in 3..6 -> 2
                    in 7..8 -> 3
                    else -> 4
                }
            }
            val dCount = categoryCounts.count { it == 1 }
            val cCount = categoryCounts.count { it == 2 }
            val pCount = categoryCounts.count { it == 3 }
            val fCount = categoryCounts.count { it == 4 }
            val categoryTotal = dCount + cCount + pCount + fCount
            val amgp = if (categoryTotal == 0) null else
                round2((dCount * 1.0 + cCount * 2.0 + pCount * 3.0 + fCount * 4.0) / categoryTotal)
            val missingCount = (expectedLearnerCount - assessedCount).coerceAtLeast(0)
            SubjectAnalytics(
                subjectCode = subject,
                learnerCount = assessedCount,
                missingCount = missingCount,
                coveragePercent = if (expectedLearnerCount == 0) 0.0
                    else round2(assessedCount * 100.0 / expectedLearnerCount),
                averagePercent = percentages.averageOrNull()?.let(::round2),
                highestPercent = percentages.maxOrNull()?.let(::round2),
                lowestPercent = percentages.minOrNull()?.let(::round2),
                passRatePercent = if (assessedCount == 0) null else round2(passCount * 100.0 / assessedCount),
                amgp = amgp,
                distinctionCount = dCount,
                creditCount = cCount,
                passCount = pCount,
                failureCount = fCount
            )
        }
    }

    fun riskFlags(progress: List<LearnerProgress>, thresholds: Thresholds = Thresholds()): List<RiskFlag> =
        progress.mapNotNull { p ->
            val latest = p.latestAverage ?: return@mapNotNull null
            val reasons = mutableListOf<String>()
            if (latest < thresholds.lowAveragePercent) reasons += "Latest average is below ${thresholds.lowAveragePercent.removeTrailingZero()}%"
            val weak = p.assessments.lastOrNull()?.marks?.filter { normalize(it) < thresholds.weakSubjectPercent }?.map { it.subjectCode }.orEmpty()
            if (weak.isNotEmpty()) reasons += "Weak subject(s): ${weak.joinToString(", ") }"
            val decline = p.changePoints
            if (decline != null && decline <= -thresholds.declinePoints) reasons += "Average declined by ${round2(-decline)} points"
            val level = when {
                decline != null && decline <= -thresholds.highDeclinePoints -> RiskLevel.HIGH
                reasons.isNotEmpty() -> RiskLevel.WATCH
                else -> RiskLevel.NONE
            }
            if (level == RiskLevel.NONE) null else RiskFlag(p.learnerId, level, reasons)
        }.sortedWith(compareByDescending<RiskFlag> { it.level }.thenBy { it.learnerId })

    private fun List<Double>.averageOrNull(): Double? = if (isEmpty()) null else average()

    private fun normalize(mark: Mark): Double =
        if (mark.maxMark <= 0) 0.0 else mark.mark.toDouble() * 100.0 / mark.maxMark

    private fun round2(value: Double): Double = round(value * 100.0) / 100.0
    private fun Double.removeTrailingZero(): String = if (this % 1.0 == 0.0) this.toInt().toString() else toString()
}
