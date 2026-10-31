package com.henrylumis.rvhgrader.core.grading

import com.henrylumis.rvhgrader.core.model.AcademicAssessment
import com.henrylumis.rvhgrader.core.model.Enrollment
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

/**
 * UI-independent rules for turning a class/term selection into an assessment and safely
 * calculating its result. This keeps screens from duplicating academic rules.
 */
object AssessmentWorkflow {
    data class MarkInput(val subjectCode: String, val mark: Int?, val maxMark: Int = 100, val source: String = "MANUAL")

    fun subjectsFor(schoolClass: SchoolClass): List<String> =
        SubjectCatalog.forClass(schoolClass).map { it.code }

    fun createAssessment(enrollment: Enrollment, term: Term, section: TermSection): AcademicAssessment =
        AcademicAssessment(enrollmentId = enrollment.id, term = term, section = section)

    fun toMarks(assessmentId: String, inputs: List<MarkInput>): List<Mark> {
        val seen = mutableSetOf<String>()
        return inputs.mapNotNull { input ->
            require(seen.add(input.subjectCode)) { "Duplicate subject: ${input.subjectCode}" }
            val value = input.mark ?: return@mapNotNull null
            require(input.maxMark > 0) { "Maximum mark must be greater than zero" }
            require(value in 0..input.maxMark) { "${input.subjectCode}: mark outside allowed range" }
            Mark(
                assessmentId = assessmentId,
                subjectCode = input.subjectCode,
                mark = value,
                maxMark = input.maxMark,
                source = input.source
            )
        }
    }

    fun validateSubjects(schoolClass: SchoolClass, inputs: List<MarkInput>) {
        val expected = subjectsFor(schoolClass).toSet()
        inputs.forEach { require(it.subjectCode in expected) { "Unknown subject: ${it.subjectCode}" } }
    }

    fun calculate(
        learnerId: String,
        assessment: AcademicAssessment,
        schoolClass: SchoolClass,
        marks: List<Mark>,
        scale: GradingScale = GradingScale.default()
    ) = AcademicEngine.calculate(
        learnerId = learnerId,
        assessmentId = assessment.id,
        mode = schoolClass.mode,
        subjectCodes = subjectsFor(schoolClass),
        marks = marks,
        scale = scale
    )
}
