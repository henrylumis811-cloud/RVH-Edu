package com.henrylumis.rvhgrader.core.reports

import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.Learner
import com.henrylumis.rvhgrader.model.SchoolClass

/** Eligibility is explicit and conservative: incomplete results never earn a certificate. */
data class Certificate(
    val schoolName: String,
    val learner: Learner,
    val schoolClass: SchoolClass,
    val academicYear: Int,
    val title: String,
    val result: AcademicResult
)

object CertificateBuilder {
    fun build(
        schoolName: String,
        learner: Learner,
        schoolClass: SchoolClass,
        academicYear: Int,
        result: AcademicResult,
        title: String = "Academic Achievement Certificate"
    ): Certificate? {
        if (!result.complete) return null
        require(result.learnerId == learner.id) { "Result belongs to a different learner" }
        require(schoolName.isNotBlank()) { "School name is required for a certificate" }
        require(academicYear > 0) { "Academic year must be positive" }
        require(title.isNotBlank()) { "Certificate title is required" }
        return Certificate(schoolName, learner, schoolClass, academicYear, title, result)
    }
}
