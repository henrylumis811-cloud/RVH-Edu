package com.henrylumis.rvhgrader.core.history

import com.henrylumis.rvhgrader.core.analytics.AssessmentSnapshot
import com.henrylumis.rvhgrader.core.analytics.LearnerProgress
import com.henrylumis.rvhgrader.core.analytics.AcademicAnalytics

/** Immutable academic timeline for one learner. */
data class LearnerAcademicHistory(
    val learnerId: String,
    val learnerName: String,
    val enrollments: List<EnrollmentHistory>,
    val progress: LearnerProgress?
)

data class EnrollmentHistory(
    val enrollmentId: String,
    val academicYear: Int,
    val schoolClassName: String,
    val stream: String?,
    val assessments: List<AssessmentHistory>
)

data class AssessmentHistory(
    val assessmentId: String,
    val termName: String,
    val sectionName: String,
    val snapshot: AssessmentSnapshot
)

/** Builds a stable chronological learner timeline from assessment snapshots. */
object AcademicHistoryCalculator {
    fun build(
        learnerId: String,
        learnerName: String,
        enrollments: List<EnrollmentHistory>
    ): LearnerAcademicHistory {
        val orderedEnrollments = enrollments.sortedWith(
            compareBy<EnrollmentHistory> { it.academicYear }
                .thenBy { it.schoolClassName }
        ).map { enrollment ->
            enrollment.copy(
                assessments = enrollment.assessments.sortedWith(
                    compareBy<AssessmentHistory> { it.snapshot.termOrder }
                        .thenBy { it.snapshot.sectionOrder }
                        .thenBy { it.snapshot.assessmentLabel }
                )
            )
        }
        val snapshots = orderedEnrollments.flatMap { it.assessments }.map { it.snapshot }
        val progress = AcademicAnalytics.learnerProgress(snapshots).firstOrNull()
        return LearnerAcademicHistory(learnerId, learnerName, orderedEnrollments, progress)
    }
}
