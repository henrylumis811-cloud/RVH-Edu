package com.henrylumis.rvhgrader.core.reports

import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.assessment.AssessmentPolicySnapshot
import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.core.model.Learner
import com.henrylumis.rvhgrader.core.model.AcademicAssessment
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.SystemMode
import kotlin.math.roundToInt

/** Immutable document model. Building a report never mutates academic data. */
data class ReportCardSubject(
    val code: String,
    val name: String,
    val mark: Int?,
    val maxMark: Int,
    val aggregate: Int?
)

data class ReportCard(
    val schoolName: String,
    val learner: Learner,
    val schoolClass: SchoolClass,
    val academicYear: Int,
    val assessment: AcademicAssessment,
    val subjects: List<ReportCardSubject>,
    val result: AcademicResult,
    val teacherComment: String? = null,
    val headTeacherComment: String? = null
)

object ReportCardBuilder {
    fun build(
        schoolName: String,
        learner: Learner,
        schoolClass: SchoolClass,
        academicYear: Int,
        assessment: AcademicAssessment,
        marks: List<Mark>,
        result: AcademicResult,
        teacherComment: String? = null,
        headTeacherComment: String? = null
    ): ReportCard {
        require(schoolName.isNotBlank()) { "School name is required for a report card" }
        require(academicYear > 0) { "Academic year must be positive" }
        require(result.learnerId == learner.id) { "Result belongs to a different learner" }
        require(result.assessmentId == assessment.id) { "Result belongs to a different assessment" }
        require(marks.all { it.assessmentId == assessment.id }) {
            "Report marks must belong to the selected assessment"
        }
        require(marks.map { it.subjectCode }.distinct().size == marks.size) {
            "Duplicate subject marks cannot be used to build a report card"
        }

        val savedPolicy = assessment.policySnapshotJson?.let {
            runCatching { AssessmentPolicySnapshot.parse(it) }.getOrNull()
        }
        val expectedSubjects = savedPolicy?.subjects?.map {
            SubjectCatalog.Subject(it.code, it.shortName, it.displayName)
        } ?: SubjectCatalog.forClass(schoolClass)
        val expectedCodes = expectedSubjects.map { it.code }
        require(marks.all { it.subjectCode in expectedCodes }) {
            "Report contains marks for a subject not assigned to this class"
        }
        require(marks.all { it.maxMark > 0 && it.mark in 0..it.maxMark }) {
            "Report contains a mark outside its valid range"
        }

        val byCode = marks.associateBy { it.subjectCode }
        val missingCodes = expectedCodes.filterNot { it in byCode }
        val expectedComplete = missingCodes.isEmpty()
        require(result.complete == expectedComplete) {
            "Result completeness does not match the marks supplied to the report"
        }
        require(result.missingSubjects.toSet() == missingCodes.toSet()) {
            "Result missing-subject list does not match the report marks"
        }

        // Missing subjects use the same 100-mark default maximum as AcademicEngine.
        val calculatedTotal = marks.sumOf { it.mark }
        val calculatedMax = marks.sumOf { it.maxMark } + missingCodes.size * 100
        val calculatedAverage = if (calculatedMax == 0) 0.0
            else calculatedTotal.toDouble() / calculatedMax * 100.0
        val roundedAverage = kotlin.math.round(calculatedAverage * 100.0) / 100.0
        require(result.total == calculatedTotal && result.maxTotal == calculatedMax &&
            kotlin.math.abs(result.average - roundedAverage) < 0.000001) {
            "Report result totals do not match the supplied marks"
        }

        val subjects = expectedSubjects.map { subject ->
            val mark = byCode[subject.code]
            // A complete saved policy is authoritative for upper-primary aggregates. Do not
            // trust a cached Mark.aggregate here: it may have been produced under newer rules.
            val aggregate = when {
                mark == null -> null
                savedPolicy?.hasCompleteScoringPolicy == true && schoolClass.mode == SystemMode.UPPER -> {
                    val normalized = if (mark.maxMark == 100) mark.mark
                        else ((mark.mark.toDouble() / mark.maxMark) * 100.0).roundToInt()
                    savedPolicy.gradingScale().aggregateFor(normalized)
                }
                else -> mark.aggregate
            }
            ReportCardSubject(subject.code, subject.displayName, mark?.mark, mark?.maxMark ?: 100, aggregate)
        }
        return ReportCard(schoolName, learner, schoolClass, academicYear, assessment, subjects, result, teacherComment, headTeacherComment)
    }
}
