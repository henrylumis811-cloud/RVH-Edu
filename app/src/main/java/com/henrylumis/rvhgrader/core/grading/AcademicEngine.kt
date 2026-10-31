package com.henrylumis.rvhgrader.core.grading

import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.model.SystemMode
import kotlin.math.roundToInt

/** Pure, UI-independent academic calculation engine. */
object AcademicEngine {
    fun calculate(
        learnerId: String,
        assessmentId: String,
        mode: SystemMode,
        subjectCodes: List<String>,
        marks: List<Mark>,
        scale: GradingScale
    ): AcademicResult {
        require(learnerId.isNotBlank()) { "Learner ID is required" }
        require(assessmentId.isNotBlank()) { "Assessment ID is required" }
        require(subjectCodes.isNotEmpty()) { "At least one subject is required" }
        require(subjectCodes.all { it.isNotBlank() }) { "Subject codes cannot be blank" }
        require(subjectCodes.distinct().size == subjectCodes.size) { "Duplicate subject codes" }
        require(marks.map { it.subjectCode }.distinct().size == marks.size) {
            "Duplicate marks for a subject are not allowed"
        }
        require(marks.all { it.assessmentId == assessmentId }) {
            "Every mark must belong to the selected assessment"
        }
        require(marks.all { it.subjectCode in subjectCodes }) {
            "Marks contain a subject not assigned to this calculation"
        }
        require(marks.all { it.maxMark > 0 && it.mark in 0..it.maxMark }) {
            "Marks must be between zero and their positive maximum mark"
        }

        val bySubject = marks.associateBy { it.subjectCode }
        val missing = subjectCodes.filter { it !in bySubject }
        val total = subjectCodes.sumOf { bySubject[it]?.mark ?: 0 }
        val maxTotal = subjectCodes.sumOf { bySubject[it]?.maxMark ?: 100 }
        val average = if (maxTotal == 0) 0.0 else (total.toDouble() / maxTotal) * 100.0
        val complete = missing.isEmpty()
        val graded = mode == SystemMode.UPPER && complete
        val roundedAverage = (average * 100).roundToInt() / 100.0

        if (!graded) {
            return AcademicResult(learnerId, assessmentId, total, maxTotal, roundedAverage,
                null, null, false, complete, missing)
        }

        val aggregateSum = subjectCodes.sumOf { subject ->
            val mark = bySubject.getValue(subject)
            val normalized = if (mark.maxMark == 100) mark.mark
            else ((mark.mark.toDouble() / mark.maxMark) * 100.0).roundToInt()
            scale.aggregateFor(normalized)
        }

        return AcademicResult(learnerId, assessmentId, total, maxTotal, roundedAverage,
            aggregateSum, scale.divisionFor(aggregateSum, total), true, true, emptyList())
    }
}
