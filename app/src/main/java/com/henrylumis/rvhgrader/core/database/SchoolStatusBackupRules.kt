package com.henrylumis.rvhgrader.core.database

/** Pure rules for keeping learner lifecycle status scoped to the correct school during backup validation. */
internal object SchoolStatusBackupRules {
    data class SchoolLearnerKey(val schoolId: String, val learnerId: String)

    fun key(schoolId: String, learnerId: String) = SchoolLearnerKey(schoolId, learnerId)

    fun hasStatusForEnrollment(
        schoolStatusKeys: Set<SchoolLearnerKey>,
        schoolId: String,
        learnerId: String
    ): Boolean = key(schoolId, learnerId) in schoolStatusKeys

    fun hasAnyStatusForLearner(
        schoolStatusKeys: Set<SchoolLearnerKey>,
        learnerId: String
    ): Boolean = schoolStatusKeys.any { it.learnerId == learnerId }
}
