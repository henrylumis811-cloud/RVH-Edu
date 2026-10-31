package com.henrylumis.rvhgrader.core.model

import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection
import java.util.UUID

data class Learner(
    val id: String = UUID.randomUUID().toString(),
    val admissionNumber: String? = null,
    val fullName: String,
    val ageYears: Int? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val photoUri: String? = null,
    val status: String = "ACTIVE",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class Enrollment(
    val id: String = UUID.randomUUID().toString(),
    val learnerId: String,
    val academicYear: Int,
    val schoolClass: SchoolClass,
    val stream: String? = null,
    val enrolledAtMillis: Long = System.currentTimeMillis()
)

enum class AssessmentStatus { DRAFT, FINAL }

data class AcademicAssessment(
    val id: String = UUID.randomUUID().toString(),
    val enrollmentId: String,
    val term: Term,
    val section: TermSection,
    val status: AssessmentStatus = AssessmentStatus.DRAFT,
    val recordedAtMillis: Long = System.currentTimeMillis(),
    val finalizedAtMillis: Long? = null,
    /** JSON snapshot of the subject policy at assessment creation; null means legacy/unpinned. */
    val policySnapshotJson: String? = null
)

data class Mark(
    val id: String = UUID.randomUUID().toString(),
    val assessmentId: String,
    val subjectCode: String,
    val mark: Int,
    val maxMark: Int = 100,
    val aggregate: Int? = null,
    val source: String = "MANUAL",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
