package com.henrylumis.rvhgrader.core.assessment

import com.henrylumis.rvhgrader.core.grading.AssessmentWorkflow
import com.henrylumis.rvhgrader.core.grading.AssessmentWorkflow.MarkInput
import com.henrylumis.rvhgrader.core.model.AcademicAssessment
import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.Enrollment
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

/**
 * UI-independent state for one learner's mark sheet.
 * Drafts may be incomplete; committing a sheet requires every expected subject.
 */
class MarkSheetSession private constructor(
    val enrollment: Enrollment,
    val assessment: AcademicAssessment,
    private val expectedSubjects: List<String>,
    private val inputs: LinkedHashMap<String, MarkInput>
) {
    val subjects: List<String> get() = expectedSubjects

    fun setMark(subjectCode: String, mark: Int?, maxMark: Int = 100, source: String = "MANUAL"): MarkSheetSession {
        require(subjectCode in expectedSubjects) { "Unknown subject: $subjectCode" }
        if (mark != null) {
            require(maxMark > 0) { "Maximum mark must be greater than zero" }
            require(mark in 0..maxMark) { "$subjectCode: mark outside allowed range" }
        }
        val next = LinkedHashMap(inputs)
        next[subjectCode] = MarkInput(subjectCode, mark, maxMark, source)
        return MarkSheetSession(enrollment, assessment, expectedSubjects, next)
    }

    fun mark(subjectCode: String): MarkInput? = inputs[subjectCode]

    fun missingSubjects(): List<String> = expectedSubjects.filter { inputs[it]?.mark == null }

    fun isComplete(): Boolean = missingSubjects().isEmpty()

    fun draftMarks(): List<Mark> = AssessmentWorkflow.toMarks(assessment.id, inputs.values.toList())

    fun commit(scale: GradingScale = GradingScale.default()): CommittedMarkSheet {
        require(isComplete()) { "Cannot commit: missing ${missingSubjects().joinToString(", ")}" }
        val marks = AssessmentWorkflow.toMarks(assessment.id, inputs.values.toList())
        val result = AssessmentWorkflow.calculate(
            learnerId = enrollment.learnerId,
            assessment = assessment,
            schoolClass = enrollment.schoolClass,
            marks = marks,
            scale = scale
        )
        return CommittedMarkSheet(marks, result)
    }

    data class CommittedMarkSheet(val marks: List<Mark>, val result: AcademicResult)

    companion object {
        fun create(enrollment: Enrollment, term: Term, section: TermSection): MarkSheetSession {
            val assessment = AssessmentWorkflow.createAssessment(enrollment, term, section)
            val subjects = AssessmentWorkflow.subjectsFor(enrollment.schoolClass)
            return MarkSheetSession(enrollment, assessment, subjects, linkedMapOf())
        }

        fun fromExisting(
            enrollment: Enrollment,
            assessment: AcademicAssessment,
            existingMarks: List<Mark>
        ): MarkSheetSession {
            val expected = AssessmentWorkflow.subjectsFor(enrollment.schoolClass)
            val values = linkedMapOf<String, MarkInput>()
            existingMarks.forEach { mark ->
                require(mark.subjectCode in expected) { "Unknown subject in stored assessment: ${mark.subjectCode}" }
                require(values.put(mark.subjectCode, MarkInput(mark.subjectCode, mark.mark, mark.maxMark, mark.source)) == null) {
                    "Duplicate stored subject: ${mark.subjectCode}"
                }
            }
            return MarkSheetSession(enrollment, assessment, expected, values)
        }
    }
}
